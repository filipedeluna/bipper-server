package db;

import java.sql.*;
import java.util.ArrayList;

import db.error.DatabaseException;
import handlers.error.ClientException;
import handlers.locations.Locations;
import handlers.post.Post;
import handlers.post.PostPeriod;
import handlers.vote.VoteType;
import utils.CustomLogger;
import utils.net.HTTPStatus;

/**
 * Class responsible for communicating with the database
 */
public final class DatabaseDriver {
  private static final CustomLogger logger = new CustomLogger(DatabaseDriver.class);

  private static final int MAX_DB_CONNECT_TRIES = 6;

  private Connection connection;

  public DatabaseDriver(String dbUser, String dbPass, String dbName, String dbAddress) throws DatabaseException {
    // Load JDBC driver
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new DatabaseException("Failed to load jdbc driver.", e);
    }

    // Get a connection to the database
    int maxTries = MAX_DB_CONNECT_TRIES;

    while (maxTries-- > 0) {
      try {
        connection = DBUtils.getConnection(dbUser, dbPass, dbName, dbAddress);
      } catch (SQLException e2) {
        if (maxTries == 0)
          throw new DatabaseException("Failed to connect to database - too many attempts.");

        logger.fine("Failed to connect to database. Retrying...");

        try {
          Thread.sleep(10000);
        } catch (InterruptedException e) {
          throw new DatabaseException("Failed to connect to database - " + e.getMessage());
        }
      }
    }

    logger.fine("Successfully connected to database.");
  }

  // ------------------------------------
  // --- Queries ------------------------
  // ------------------------------------

  /**
   * Check if a user exists in the database, if not, create it
   *
   * @throws DatabaseException if check or create user
   */
  public void createUserIfNotExists(String userID) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "INSERT INTO users (user_id) VALUES (?) ON CONFLICT DO NOTHING"
      );

      ps.setString(1, userID);

      ps.execute();
      ps.close();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to check if user exists.", e);
    }
  }

  /**
   * @return all the locations and zones ordered alphabetically
   * @throws DatabaseException if fails
   */
  public Locations getLocations() throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM locations ORDER BY district, county, zone"
      );

      ResultSet rs = ps.executeQuery();
      Locations locations = new Locations();

      while (rs.next())
        locations.addZone(
            rs.getString("district"),
            rs.getString("county"),
            rs.getString("zone"),
            rs.getInt("location_id")
        );

      ps.close();

      return locations;
    } catch (SQLException e) {
      throw new DatabaseException("Failed to get locations.", e);
    }
  }

  /**
   * Check if a user exists in the database, if not, create it
   *
   * @throws DatabaseException if check or create user
   */
  public void insertPost(String userID, int locationID, String text, String image) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "INSERT INTO posts (user_id, post_location_id, post_text, post_image)" +
              " VALUES (?, ?, ?, ?)"
      );

      ps.setString(1, userID);
      ps.setInt(2, locationID);
      ps.setString(3, text);
      ps.setString(4, image);

      ps.execute();
      ps.close();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to insert post.", e);
    }
  }

  /**
   * @return get all unread posts for a user
   * @throws DatabaseException if fails
   */
  public ArrayList<Post> getNewPosts(String userID) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM posts" +
              " WHERE post_id NOT IN (SELECT post_id FROM votes WHERE user_id = ?)" +
              " ORDER BY post_date DESC" +
              " LIMIT 10"
      );

      ps.setString(1, userID);

      ResultSet rs = ps.executeQuery();
      ArrayList<Post> posts = new ArrayList<>();

      while (rs.next())
        posts.add(new Post(
                rs.getInt("post_id"),
                rs.getInt("post_score"),
                rs.getDate("post_date"),
                rs.getString("post_text"),
                rs.getString("post_image")
            )
        );

      ps.close();

      return posts;
    } catch (SQLException e) {
      throw new DatabaseException("Failed to get new posts.", e);
    }
  }

  /**
   * @return get all unread posts for a user
   * @throws DatabaseException if fails
   */
  public ArrayList<Post> getTopPosts(PostPeriod period) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM posts" +
              " WHERE post_date > NOW() - ?::INTERVAL" +
              " ORDER BY post_score DESC" +
              " LIMIT 10"
      );

      ps.setString(1, period.getDbPeriod());

      ResultSet rs = ps.executeQuery();
      ArrayList<Post> posts = new ArrayList<>();

      while (rs.next())
        posts.add(new Post(
                rs.getInt("post_id"),
                rs.getInt("post_score"),
                rs.getDate("post_date"),
                rs.getString("post_text"),
                rs.getString("post_image")
            )
        );

      ps.close();

      return posts;
    } catch (SQLException e) {
      throw new DatabaseException("Failed to get locations.", e);
    }
  }

  /**
   * Make a user vote for a post
   *
   * @throws DatabaseException if fails
   */
  public void votePost(String userID, int postID, VoteType voteType) throws DatabaseException, ClientException {
    try {
      // Check post exists and get original poster id
      PreparedStatement ps = connection.prepareStatement(
          "SELECT user_id FROM posts WHERE post_id = ?"
      );

      ps.setInt(1, postID);

      ResultSet rs = ps.executeQuery();

      if (!rs.next())
        throw new ClientException("Post does not exist.", HTTPStatus.HTTP_NOT_FOUND);

      String originalPoster = rs.getString("user_id");

      if (userID.equals(originalPoster))
        throw new ClientException("Original author cant vote for post.", HTTPStatus.HTTP_UNAUTHORIZED);

      // Add vote if vote does not exist
      ArrayList<PreparedStatement> transaction = new ArrayList<>();

      int voteValue = voteType == VoteType.UP ? 1 : -1;

      ps = connection.prepareStatement(
          "UPDATE posts" +
              " SET post_score = post_score + ?" +
              " WHERE post_id = ?" +
              " AND ? NOT IN" +
              "   (SELECT user_id FROM votes WHERE post_id = ?)"
      );

      ps.setInt(1, voteValue);

      ps.setInt(2, postID);

      ps.setString(3, userID);
      ps.setInt(4, postID);

      transaction.add(ps);

      ps = connection.prepareStatement(
          "UPDATE users" +
              " SET user_score = user_score + ?" +
              " WHERE user_id = ?" +
              " AND ? NOT IN" +
              "   (SELECT user_id FROM votes WHERE post_id = ?)"
      );

      ps.setInt(1, voteValue);

      ps.setString(2, originalPoster);

      ps.setString(3, userID);
      ps.setInt(4, postID);

      transaction.add(ps);

      ps = connection.prepareStatement(
          "INSERT INTO votes (user_id, post_id) values (?, ?) ON CONFLICT DO NOTHING"
      );

      ps.setString(1, userID);
      ps.setInt(2, postID);

      transaction.add(ps);

      DBUtils.executeTransaction(transaction);

      ps.close();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to insert post.", e);
    }
  }

  /**
   * @return get score for a user
   * @throws DatabaseException if fails
   */
  public int getUserScore(String userID) throws DatabaseException, ClientException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT user_score FROM users WHERE user_id = ?"
      );

      ps.setString(1, userID);

      ResultSet rs = ps.executeQuery();

      int score;

      if (rs.next())
        score = rs.getInt("user_score");
      else
        throw new ClientException("User does not exist.", HTTPStatus.HTTP_NOT_FOUND);

      return score;
    } catch (SQLException e) {
      throw new DatabaseException("Failed to get locations.", e);
    }
  }

}


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
  public void createUserIfNotExists(String userID, int locationID) throws DatabaseException, ClientException {
    try {
      // Check user exists
      PreparedStatement ps = connection.prepareStatement(
          "SELECT user_id FROM users WHERE user_id = ?"
      );

      ps.setString(1, userID);

      ResultSet rs = ps.executeQuery();

      // User does not exists
      if (!rs.next()) {
        if (locationID == -1)
          throw new ClientException("User id not found registered.", HTTPStatus.HTTP_NOT_FOUND);

        // Check location exists
        ps = connection.prepareStatement(
            "SELECT location_id FROM locations WHERE location_id = ?"
        );

        ps.setInt(1, locationID);

        rs = ps.executeQuery();

        if (!rs.next())
          throw new ClientException("Invalid location id.", HTTPStatus.HTTP_BAD_REQUEST);

        ps = connection.prepareStatement(
            "INSERT INTO users (user_id, user_location_id) VALUES (?, ?) ON CONFLICT DO NOTHING"
        );

        ps.setString(1, userID);
        ps.setInt(2, locationID);

        ps.execute();
      }

      ps.close();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to check if user exists.", e);
    }
  }

  /**
   * Set a new location for users posts
   *
   * @throws DatabaseException if check or create user
   */
  public void setUserLocation(String userID, int locationID) throws DatabaseException, ClientException {
    try {
      // Check location exists
      PreparedStatement ps = connection.prepareStatement(
          "SELECT location_id FROM locations WHERE location_id = ?"
      );

      ps.setInt(1, locationID);

      ResultSet rs = ps.executeQuery();

      if (!rs.next())
        throw new ClientException("Invalid location id.", HTTPStatus.HTTP_BAD_REQUEST);

      ps = connection.prepareStatement(
          "UPDATE users SET user_location_id = ? WHERE user_id = ?"
      );

      ps.setInt(1, locationID);
      ps.setString(2, userID);

      ps.execute();
      ps.close();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to set user location.", e);
    }
  }

  /**
   * Get the users current registered location
   *
   * @throws DatabaseException if check or create user
   */
  public int getUserLocation(String userID) throws DatabaseException, ClientException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT user_location_id FROM users WHERE user_id = ?"
      );

      ps.setString(1, userID);

      ResultSet rs = ps.executeQuery();

      if (!rs.next())
        throw new ClientException("Invalid location id.", HTTPStatus.HTTP_NOT_FOUND);

      int locationID = rs.getInt("user_location_id");

      ps.close();

      return locationID;
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
          "SELECT * FROM locations ORDER BY location_district, location_county, location_zone"
      );

      ResultSet rs = ps.executeQuery();
      Locations locations = new Locations();

      while (rs.next())
        locations.addZone(
            rs.getString("location_district"),
            rs.getString("location_county"),
            rs.getString("location_zone"),
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
  public void insertPost(String userID, int locationID, String text, String image, String image_type) throws DatabaseException, ClientException {
    try {
      // Check when was user last post
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM posts WHERE post_user_id = ? " +
              "AND post_date > now() - interval '5 minutes'"
      );

      ps.setString(1, userID);

      ResultSet rs = ps.executeQuery();

      if (rs.next())
        throw new ClientException("Posting too fast.", HTTPStatus.HTTP_FORBIDDEN);

      // Insert post
      ps = connection.prepareStatement(
          "INSERT INTO posts (post_user_id, post_location_id, post_text, post_image, post_image_type)" +
              " VALUES (?, ?, ?, ?, ?)"
      );

      ps.setString(1, userID);
      ps.setInt(2, locationID);
      ps.setString(3, text);
      ps.setString(4, image);
      ps.setString(5, image_type);

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
  public ArrayList<Post> getNewPosts(String userID, int locationID) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM posts" +
              " WHERE post_id NOT IN (SELECT vote_post_id FROM votes WHERE vote_user_id = ?)" +
              " AND post_location_id = ? AND post_user_id != ? " +
              " ORDER BY post_id DESC" +
              " LIMIT 10"
      );

      ps.setString(1, userID);
      ps.setInt(2, locationID);
      ps.setString(3, userID);

      ResultSet rs = ps.executeQuery();
      ArrayList<Post> posts = new ArrayList<>();

      while (rs.next())
        posts.add(new Post(
                rs.getInt("post_id"),
                rs.getInt("post_score"),
                rs.getDate("post_date"),
                rs.getString("post_text"),
                rs.getString("post_image"),
                rs.getString("post_image_type")
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
  public ArrayList<Post> getTopPosts(PostPeriod period, int locationID) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM posts" +
              " WHERE post_date > NOW() - ?::INTERVAL" +
              " AND post_location_id = ?" +
              " ORDER BY post_score DESC" +
              " LIMIT 20"
      );

      ps.setString(1, period.getDbPeriod());
      ps.setInt(2, locationID);

      ResultSet rs = ps.executeQuery();
      ArrayList<Post> posts = new ArrayList<>();

      while (rs.next())
        posts.add(new Post(
                rs.getInt("post_id"),
                rs.getInt("post_score"),
                rs.getDate("post_date"),
                rs.getString("post_text"),
                rs.getString("post_image"),
                rs.getString("post_image_type")
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
          "SELECT post_user_id FROM posts WHERE post_id = ?"
      );

      ps.setInt(1, postID);

      ResultSet rs = ps.executeQuery();

      if (!rs.next())
        throw new ClientException("Post does not exist.", HTTPStatus.HTTP_NOT_FOUND);

      String originalPoster = rs.getString("post_user_id");

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
              "   (SELECT vote_user_id FROM votes WHERE vote_post_id = ?)"
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
              "   (SELECT vote_user_id FROM votes WHERE vote_post_id = ?)"
      );

      ps.setInt(1, voteValue);

      ps.setString(2, originalPoster);

      ps.setString(3, userID);
      ps.setInt(4, postID);

      transaction.add(ps);

      ps = connection.prepareStatement(
          "INSERT INTO votes (vote_user_id, vote_post_id) values (?, ?) ON CONFLICT DO NOTHING"
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


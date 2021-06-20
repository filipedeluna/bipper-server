package db;

import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;

import db.error.DatabaseException;
import handlers.locations.Locations;
import handlers.post.Post;
import handlers.post.PostPeriod;
import utils.CustomLogger;

/**
 * Class responsible for communicating with the database
 */
public final class DatabaseDriver {
  private static final CustomLogger logger = new CustomLogger(DatabaseDriver.class);
  private static final Gson gson = new Gson();

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
   * @throws DatabaseException
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
   * @throws DatabaseException
   */
  public ArrayList<Post> getNewPosts(String userID) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM posts" +
              "WHERE post_id NOT IN(SELECT * FROM votes WHERE posts.user_id != ?)" +
              "ORDER BY post_date DESC" +
              "LIMIT 10"
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
      throw new DatabaseException("Failed to get locations.", e);
    }
  }

  /**
   * @return get all unread posts for a user
   * @throws DatabaseException
   */
  public ArrayList<Post> getTopPosts(PostPeriod period) throws DatabaseException {
    try {
      PreparedStatement ps = connection.prepareStatement(
          "SELECT * FROM posts" +
              "WHERE post_date > NOW() - INTERVAL '?'" +
              "ORDER BY post_score DESC" +
              "LIMIT 10"
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
}

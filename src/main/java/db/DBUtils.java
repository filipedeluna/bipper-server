package db;

import utils.Config;

import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

final class DBUtils {
  private static final Logger logger = Config.getLogger(DBUtils.class);

  static Connection getConnection(String dbUser, String dbPass, String dbName, String dbAddress) throws SQLException {
    // Build properties and URL
    String url = "jdbc:postgresql://" + dbAddress + "/" + dbName;

    Properties props = new Properties();
    props.setProperty("user", dbUser);
    props.setProperty("password", dbPass);

    Connection connection;

    // Attempt to connect by ssl
    try {
      props.setProperty("ssl", "true");
      connection = DriverManager.getConnection(url, props);
    } catch (SQLException e) {
      logger.fine("Failed to connect by SSL, trying without...");
      props.setProperty("ssl", "false");
      connection = DriverManager.getConnection(url, props);
    }

    return connection;
  }

  static void executeTransaction(List<PreparedStatement> preparedStatements) throws SQLException {
    if (preparedStatements.size() == 0)
      return;

    Connection connection = preparedStatements.get(0).getConnection();

    try {
      connection.setAutoCommit(false);

      for (PreparedStatement preparedStatement : preparedStatements) {
        preparedStatement.execute();
        preparedStatement.close();
      }

      connection.commit();
    } finally {
      connection.setAutoCommit(true);
    }
  }

  static String getQuestionMarks(int number) {
    String string = "?, ".repeat(number);

    return string.substring(0, string.length() - 2);
  }

  static String or(String... values) {
    return String.join(" OR ", values);
  }

  static String and(String... values) {
    return String.join(" AND ", values);
  }
}

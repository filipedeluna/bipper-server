package utils;

import db.DatabaseDriver;
import db.error.DatabaseException;
import utils.crypto.CryptoHelper;

import java.io.*;
import java.security.Key;

public final class Config {
  // System
  public static final String PROVIDER = "BC";
  public static String logLevel = "ALL"; // Default

  // Server
  public static int serverThreads;
  public static int serverPort;
  public static Key serverSeaKey;
  public static int maxImageSize;
  public static int maxPostLength;
  public static int maxRequestSize;

  // Database
  public static DatabaseDriver dbDriver;

  /**
   * Parse the properties file and extract the configuration to be used in the program
   *
   * @param propertiesFile the path to the properties file
   */
  public static void parse(String propertiesFile) {
    // System ---------------------------------------------------
    // Try to fetch the properties file
    java.util.Properties props = new java.util.Properties();
    try {
      props.load(new FileInputStream(propertiesFile));
    } catch (IOException e) {
      throw new RuntimeException("Failed to find properties file.");
    }

    logLevel = props.getProperty("log_level", "ALL");
    CustomLogger logger = new CustomLogger(Config.class);

    // Server ---------------------------------------------------
    serverThreads = Integer.parseInt(props.getProperty("server_threads", "4"));
    serverPort = Integer.parseInt(props.getProperty("server_port", "9000"));

    if (props.getProperty("server_sea_key") == null)
      throw new CustomRuntimeException(logger, "Undefined server sea key.");

    serverSeaKey = CryptoHelper.generateKey(props.getProperty("server_sea_key"));

    maxImageSize = Integer.parseInt(props.getProperty("max_image_size_mb", "4")) * 1000 * 1000; // MB
    maxPostLength = Integer.parseInt(props.getProperty("max_post_length", "350"));
    maxRequestSize = maxImageSize + maxPostLength + 10000;

    // Database ---------------------------------------------------
    String dbUser = props.getProperty("db_user");
    String dbPass = props.getProperty("db_pass");
    String dbName = props.getProperty("db_name");
    String dbAddress = props.getProperty("db_address");

    if (dbUser == null)
      throw new CustomRuntimeException(logger, "Undefined database user.");

    if (dbPass == null)
      throw new CustomRuntimeException(logger, "Undefined database password.");

    if (dbName == null)
      throw new CustomRuntimeException(logger, "Undefined database name.");

    if (dbAddress == null)
      throw new CustomRuntimeException(logger, "Undefined database address.");

    try {
      dbDriver = new DatabaseDriver(dbUser, dbPass, dbName, dbAddress);
    } catch (DatabaseException e) {
      e.printStackTrace();
    }
  }
}


package utils;

import java.io.*;
import java.util.Date;
import java.util.logging.*;

public abstract class Config {
  // System
  public static String logLevel;

  // Server
  public static int serverThreads;
  public static int serverPort;

  // Database
  public static String dbUser;
  public static String dbPass;
  public static String dbName;
  public static String dbAddress;

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

    // Server ---------------------------------------------------
    serverThreads = Integer.parseInt(props.getProperty("server_threads", "4"));
    serverPort = Integer.parseInt(props.getProperty("server_port", "9000"));

    // Database ---------------------------------------------------
    dbUser = props.getProperty("db_user");
    dbPass = props.getProperty("db_pass");
    dbName = props.getProperty("db_name");
    dbAddress = props.getProperty("db_address");

    if (dbUser == null)
      throw new RuntimeException("Undefined database user.");

    if (dbPass == null)
      throw new RuntimeException("Undefined database password.");

    if (dbName == null)
      throw new RuntimeException("Undefined database name.");

    if (dbAddress == null)
      throw new RuntimeException("Undefined database address.");
  }

  /**
   * Creates an instance of the programs custom logger for different classes
   *
   * @param clazz the class for the logger
   * @return new logger instance for a given class
   */
  public static Logger getLogger(Class<?> clazz) {
    // Get level from properties and configure the handler with its properties
    Logger logger = Logger.getLogger(clazz.getName());
    logger.setUseParentHandlers(false);
    logger.setLevel(Level.parse(logLevel));

    // Configure console handler
    StreamHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.parse(logLevel));
    consoleHandler.setFormatter(new CustomLoggeDateFormatter());
    logger.addHandler(consoleHandler);

    // Configure file handler
    try {
      FileHandler fileHandler = new FileHandler("log.txt", true);
      fileHandler.setLevel(Level.parse(logLevel));
      fileHandler.setFormatter(new CustomLoggeDateFormatter());
      logger.addHandler(fileHandler);
    } catch (IOException | SecurityException e) {
      throw new RuntimeException("Failed to write to log file location in client.properties file.");
    }

    return logger;
  }

  public static class CustomLoggeDateFormatter extends SimpleFormatter {
    private static final String format = "[%1$tF %1$tT][%2$s][%3$s]: %4$s %n";

    @Override
    public synchronized String format(LogRecord lr) {
      return String.format(format,
          new Date(lr.getMillis()),
          lr.getLoggerName(),
          lr.getLevel().getLocalizedName(),
          lr.getMessage()
      );
    }
  }
}


package utils;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class CustomLogger {
  private static final Logger log;
  private final String name;

  static {
    // Get level from properties and configure the handler with its properties
    log = Logger.getLogger("");
    log.setUseParentHandlers(false);
    log.setLevel(Level.parse(Config.logLevel));

    // Configure console handler
    StreamHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.parse(Config.logLevel));
    consoleHandler.setFormatter(new CustomLoggerDateFormatter());
    log.addHandler(consoleHandler);

    // Configure file handler
    try {
      FileHandler fileHandler = new FileHandler("log.txt", false);
      fileHandler.setLevel(Level.parse(Config.logLevel));
      fileHandler.setFormatter(new CustomLoggerDateFormatter());
      log.addHandler(fileHandler);
    } catch (IOException | SecurityException e) {
      throw new RuntimeException("Failed to write to log file location in client.properties file.");
    }

  }

  public CustomLogger(Class<?> clazz) {
    name = clazz.getName();
  }

  public void info(String message) {
    log.info("[" + name + "]" + message);
  }

  public void severe(String message) {
    log.severe("[" + name + "]" + message);
  }

  public void fine(String message) {
    log.fine("[" + name + "]" + message);
  }

  public void finer(String message) {
    log.finer("[" + name + "]" + message);
  }

  public void finest(String message) {
    log.finest("[" + name + "]" + message);
  }

  public String getName() {
    return log.getName();
  }

  public static class CustomLoggerDateFormatter extends SimpleFormatter {
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

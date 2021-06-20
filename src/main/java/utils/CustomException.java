package utils;

public final class CustomException extends Exception {
  public CustomException(CustomLogger logger, String text) {
    super(log(logger, text));
  }

  private static String log(CustomLogger logger, String text) {
    logger.finer("[" + logger.getName() + "]: " + text);

    return text;
  }
}

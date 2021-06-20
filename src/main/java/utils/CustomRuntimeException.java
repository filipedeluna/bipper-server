package utils;

public final class CustomRuntimeException extends RuntimeException {
  public CustomRuntimeException(CustomLogger logger, String text) {
    super(log(logger, text));
  }

  private static String log(CustomLogger logger, String text) {
    logger.finer("[" + logger.getName() + "]: " + text);

    return text;
  }
}

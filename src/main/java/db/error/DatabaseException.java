package db.error;


public final class DatabaseException extends Exception {
  public DatabaseException(String text, Exception e) {
    super("Database exception - " + text + " (" + e.getMessage() + ")");
  }

  public DatabaseException(String text) {
    super("Database exception - " + text);
  }
}

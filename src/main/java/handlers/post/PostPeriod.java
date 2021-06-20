package handlers.post;

public enum PostPeriod {
  ALL_TIME("100 years"),
  YEAR("1 year"),
  MONTH("1 month"),
  WEEK("1 week"),
  NULL("");

  private final String dbPeriod;

  PostPeriod(String dbPeriod) {
    this.dbPeriod = dbPeriod;
  }

  public String getDbPeriod() {
    return dbPeriod;
  }

  public static PostPeriod parse(String string) {
    switch (string.toUpperCase()) {
      case "all-time":
        return ALL_TIME;
      case "month":
        return MONTH;
      case "week":
        return WEEK;
      default:
        return NULL;
    }
  }
}

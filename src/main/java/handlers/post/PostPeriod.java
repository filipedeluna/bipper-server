package handlers.post;

public enum PostPeriod {
  ALL_TIME("100 years"),
  YEAR("1 year"),
  MONTH("1 month"),
  WEEK("8 days"),
  DAY("2 days"),
  NULL("");

  private final String dbPeriod;

  PostPeriod(String dbPeriod) {
    this.dbPeriod = dbPeriod;
  }

  public String getDbPeriod() {
    return dbPeriod;
  }

  public static PostPeriod parse(String string) {
    switch (string.toLowerCase()) {
      case "all-time":
        return ALL_TIME;
      case "year":
        return YEAR;
      case "month":
        return MONTH;
      case "week":
        return WEEK;
      case "day":
        return DAY;
      default:
        return NULL;
    }
  }
}

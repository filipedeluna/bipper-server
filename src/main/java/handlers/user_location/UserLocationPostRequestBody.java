package handlers.user_location;

public final class UserLocationPostRequestBody {
  private String token;
  private int locationID;

  public UserLocationPostRequestBody() {
  }

  public String getToken() {
    return token;
  }

  public int getLocationID() {
    return locationID;
  }
}

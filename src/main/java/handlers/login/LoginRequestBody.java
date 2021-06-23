package handlers.login;

public final class LoginRequestBody {
  private int userID;
  private int verification;
  private int locationID = -1;

  public LoginRequestBody() {
  }

  public int getLocationID() {
    return locationID;
  }

  public int getUserID() {
    return userID;
  }

  public int getVerification() {
    return verification;
  }
}

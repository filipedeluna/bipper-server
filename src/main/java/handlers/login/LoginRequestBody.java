package handlers.login;

public class LoginRequestBody {
  private int userID;
  private int verification;

  public LoginRequestBody() {
  }

  public int getUserID() {
    return userID;
  }

  public int getVerification() {
    return verification;
  }
}

package handlers.post;

public final class PostsGetRequestBody {
  private String token;
  private int locationID;

  public PostsGetRequestBody() {
  }

  public String getToken() {
    return token;
  }

  public int getLocationID() {
    return locationID;
  }
}

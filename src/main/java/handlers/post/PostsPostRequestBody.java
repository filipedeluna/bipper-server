package handlers.post;

public final class PostsPostRequestBody {
  private String token;
  private String text;
  private String image;
  private int locationID;

  public PostsPostRequestBody() {
  }

  public String getToken() {
    return token;
  }

  public String getText() {
    return text;
  }

  public String getImage() {
    return image;
  }

  public int getLocationID() {
    return locationID;
  }
}

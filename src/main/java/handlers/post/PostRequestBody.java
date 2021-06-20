package handlers.post;

public final class PostRequestBody {
  private String token;
  private String text;
  private String image;
  private String location_id;

  public PostRequestBody() {
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

  public String getLocation_id() {
    return location_id;
  }
}

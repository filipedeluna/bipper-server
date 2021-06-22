package handlers.post;

public final class PostsGetRequestBody {
  private String token;
  private int index = 0;

  public PostsGetRequestBody() {
  }

  public int getIndex() {
    return index;
  }

  public String getToken() {
    return token;
  }
}

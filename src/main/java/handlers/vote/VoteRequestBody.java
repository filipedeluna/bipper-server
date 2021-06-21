package handlers.vote;

public final class VoteRequestBody {
  private String token;
  private int postID;

  public VoteRequestBody() {
  }

  public String getToken() {
    return token;
  }

  public int getPostID() {
    return postID;
  }
}

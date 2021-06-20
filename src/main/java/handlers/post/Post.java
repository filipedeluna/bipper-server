package handlers.post;

import java.util.Date;

public final class Post {
  private int postID;
  private int score;
  private Date date;
  private String text;
  private String image;

  public Post() {
  }

  public Post(int postID, int score, Date date, String text, String image) {
    this.postID = postID;
    this.score = score;
    this.date = date;
    this.text = text;
    this.image = image;
  }

  public int getPostID() {
    return postID;
  }

  public int getScore() {
    return score;
  }

  public Date getDate() {
    return date;
  }

  public String getText() {
    return text;
  }

  public String getImage() {
    return image;
  }
}

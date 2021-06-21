package handlers.vote;

import handlers.post.PostPeriod;

public enum VoteType {
  UP,
  DOWN,
  NULL;

  public static VoteType parse(String string) {
    switch (string.toUpperCase()) {
      case "DOWN":
        return DOWN;
      case "UP":
        return UP;
      default:
        return NULL;
    }
  }
}

package handlers.error;

import utils.net.HTTPStatus;

public class ClientException extends Exception {
  private final HTTPStatus status;

  public ClientException(String text, HTTPStatus status) {
    super(text);
    this.status = status;
  }

  public HTTPStatus getStatus() {
    return status;
  }
}

package utils.net;

import com.sun.net.httpserver.HttpExchange;

public enum RequestMethod {
  GET,
  POST,
  PATCH,
  DELETE;

  public boolean equals(HttpExchange exchange) {
    return this.name().equals(exchange.getRequestMethod());
  }
}

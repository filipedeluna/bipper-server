package utils.net;

import com.sun.net.httpserver.HttpExchange;

public enum RequestMethod {
  GET,
  POST,
  PATCH,
  DELETE,
  NULL;

  public boolean equals(HttpExchange exchange) {
    return this.name().equals(exchange.getRequestMethod());
  }

  public static RequestMethod parse(HttpExchange exchange) {
    switch (exchange.getRequestMethod().toUpperCase()) {
      case "GET":
        return GET;
      case "POST":
        return POST;
      case "PATCH":
        return PATCH;
      case "DELETE":
        return DELETE;
      default:
        return NULL;
    }
  }
}

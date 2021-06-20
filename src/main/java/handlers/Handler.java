package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import utils.Config;
import utils.net.HTTPStatus;
import utils.net.SafeInputStreamReader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public abstract class Handler implements HttpHandler {
  protected static final Logger logger = Config.getLogger(Handler.class);
  protected static final Gson gson = new Gson();

  protected void respond(String body, HttpExchange exchange, HTTPStatus status) throws IOException {
    OutputStream outputStream = exchange.getResponseBody();
    exchange.sendResponseHeaders(status.getCode(), body.getBytes(StandardCharsets.UTF_8).length);
    outputStream.write(body.getBytes(StandardCharsets.UTF_8));

    logResponse(body, exchange, status);

    outputStream.flush();
    outputStream.close();
    exchange.close();
  }

  protected void respond(HttpExchange exchange, HTTPStatus status) throws IOException {
    logResponse("EMPTY", exchange, status);

    exchange.sendResponseHeaders(status.getCode(), 0);
    exchange.close();
  }

  private void logResponse(String body, HttpExchange httpExchange, HTTPStatus status) {
    logger.info(String.join("\t",
        "[RESPONSE] Status: " + status.getCode(),
        "Method: " + httpExchange.getResponseCode(),
        "URI: " + httpExchange.getRequestURI().getPath(),
        "To: " + httpExchange.getRemoteAddress().toString(),
        "Response Code: " + httpExchange.getResponseCode(),
        "Body: " + (body.length() < 100 ? body : "Too big!")
        )
    );
  }

  protected String getBodyAndLog(HttpExchange exchange) {
    String body = new SafeInputStreamReader(exchange.getRequestBody()).toString();

    logger.info(" " + String.join("\t",
        "[REQUEST]: ",
        "Method: " + exchange.getRequestMethod(),
        "URI: " + exchange.getRequestURI().getPath(),
        "From: " + exchange.getRemoteAddress().toString(),
        "Body: " + (body.length() < 100 ? body : "Too big!")
        )
    );

    return body;
  }
}

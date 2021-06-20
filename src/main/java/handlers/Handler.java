package handlers;

import auth.WebToken;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import handlers.error.ClientException;
import utils.Config;
import utils.CustomException;
import utils.CustomLogger;
import utils.net.HTTPStatus;
import utils.net.SafeInputStreamReader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public abstract class Handler implements HttpHandler {
  protected static final CustomLogger logger = new CustomLogger(Handler.class);
  protected static final Gson gson = new Gson();

  protected void respond(String body, HTTPStatus status, HttpExchange exchange) throws IOException {
    OutputStream outputStream = exchange.getResponseBody();
    exchange.sendResponseHeaders(status.getCode(), body.getBytes(StandardCharsets.UTF_8).length);
    outputStream.write(body.getBytes(StandardCharsets.UTF_8));

    logResponse(body, exchange, status);

    outputStream.flush();
    outputStream.close();
    exchange.close();
  }

  protected void respond(ClientException exception, HttpExchange exchange) throws IOException {
    respond(exception.getMessage(), exception.getStatus(), exchange);
  }

  protected void respond(HTTPStatus status, HttpExchange exchange) throws IOException {
    logResponse("", exchange, status);

    exchange.sendResponseHeaders(status.getCode(), 0);
    exchange.close();
  }

  /**
   * @param token - a base64 encrypted token
   * @return the token userID
   * @throws ClientException if the token is invalid
   */
  protected String validateToken(String token) throws ClientException {
    WebToken webToken;

    try {
      webToken = WebToken.decrypt(token);
    } catch (GeneralSecurityException | CustomException e) {
      throw new ClientException("Token is corrupted.", HTTPStatus.HTTP_BAD_REQUEST);
    }

    if (webToken.isExpired())
      throw new ClientException("Token is expired.", HTTPStatus.HTTP_UNAUTHORIZED);

    return webToken.getSubject();
  }

  // ----------------------------------------
  // UTILS ----------------------------------
  // ----------------------------------------

  private void logResponse(String body, HttpExchange httpExchange, HTTPStatus status) {
    logger.fine(String.join("\n",
        "\n[RESPONSE] ",
        "\tStatus: " + status.getCode(),
        "\tMethod: " + httpExchange.getRequestMethod(),
        "\tURI: " + httpExchange.getRequestURI().getPath(),
        "\tTo: " + httpExchange.getRemoteAddress().toString(),
        "\tResponse Code: " + status.getCode(),
        "\tBody: " + (body.length() < 100 ? body : "Too big!")
        )
    );
  }

  protected String getBodyAndLog(HttpExchange exchange) throws ClientException {
    try {
      String body = new SafeInputStreamReader(exchange.getRequestBody()).toString();

      logger.fine(" " + String.join("\n",
          "\n[REQUEST]: ",
          "\tMethod: " + exchange.getRequestMethod(),
          "\tURI: " + exchange.getRequestURI().getPath(),
          "\tFrom: " + exchange.getRemoteAddress().toString(),
          "\tBody: " + (body.length() < 100 ? body : "Too big!")
          )
      );

      return body;
    } catch (UncheckedIOException e) {
      throw new ClientException("Request body is too large.",HTTPStatus.HTTP_BAD_REQUEST);
    }

  }
}

package handlers.post;

import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import utils.Config;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;

public final class PostsHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      switch (RequestMethod.parse(exchange)) {
        case GET:
          respond(HTTPStatus.HTTP_BAD_METHOD, exchange);
          return;
        case POST:
          String body = getBodyAndLog(exchange);

          PostsPostRequestBody requestBody = gson.fromJson(body, PostsPostRequestBody.class);
          String userID = validateToken(requestBody.getToken());
          int locationID = requestBody.getLocationID();
          String text = requestBody.getText();
          String image = requestBody.getImage();

          Config.dbDriver.insertPost(userID, locationID, text, image);

          respond(HTTPStatus.HTTP_OK, exchange);
          return;
        default:
          respond(HTTPStatus.HTTP_BAD_METHOD, exchange);
      }
    } catch (ClientException e) {
      respond(e, exchange);
    } catch (DatabaseException e) {
      logger.severe(e.getClass() + ": " + e.getMessage());
      respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
    }
  }
}

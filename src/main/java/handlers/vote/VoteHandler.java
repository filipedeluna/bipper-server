package handlers.vote;

import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import handlers.post.PostsPostRequestBody;
import utils.Config;
import utils.ImageCompressor;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;

public final class VoteHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    // Validate REST method
    if (!RequestMethod.POST.equals(exchange)) {
      respond(HTTPStatus.HTTP_BAD_METHOD, exchange);
      return;
    }

    try {
      String body = getBodyAndLog(exchange);

      VoteRequestBody requestBody = gson.fromJson(body, VoteRequestBody.class);

      if (requestBody == null) {
        respond("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
        return;
      }

      String userID = validateToken(requestBody.getToken());
      int postID = requestBody.getPostID();

      Config.dbDriver.votePost(userID, postID);

      respond(HTTPStatus.HTTP_OK, exchange);
    } catch (ClientException e) {
      respond(e, exchange);
    } catch (DatabaseException e) {
      logger.severe(e.getClass() + ": " + e.getMessage());
      respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
    }
  }
}

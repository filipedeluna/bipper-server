package handlers.vote;

import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import handlers.post.PostPeriod;
import handlers.post.PostsPostRequestBody;
import utils.Config;
import utils.ImageCompressor;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;

public final class VoteHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      // Validate REST method
      if (!RequestMethod.POST.equals(exchange))
        throw new ClientException("Invalid method.", HTTPStatus.HTTP_BAD_METHOD);

      // Extract body
      String body = getBodyAndLog(exchange);
      VoteRequestBody requestBody = gson.fromJson(body, VoteRequestBody.class);

      if (requestBody == null)
        throw new ClientException("Invalid body.", HTTPStatus.HTTP_BAD_METHOD);

      String userID = validateToken(requestBody.getToken());
      int postID = requestBody.getPostID();

      // Validate path to extract vote type
      String path = exchange.getRequestURI().getPath();

      String voteTypeString = path.replaceFirst("/vote/", "");
      VoteType voteType = VoteType.parse(voteTypeString);

      if (voteType == VoteType.NULL)
        throw new ClientException("Invalid vote type.", HTTPStatus.HTTP_BAD_REQUEST);

      Config.dbDriver.votePost(userID, postID, voteType);

      respond(HTTPStatus.HTTP_OK, exchange);
    } catch (ClientException e) {
      respond(e, exchange);
    } catch (DatabaseException e) {
      logger.severe(e.getClass() + ": " + e.getMessage());
      respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
    }
  }
}

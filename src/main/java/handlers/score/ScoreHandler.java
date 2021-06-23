package handlers.score;

import auth.WebToken;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import handlers.vote.VoteRequestBody;
import handlers.vote.VoteType;
import utils.Config;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;

public final class ScoreHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      // Validate REST method
      if (!RequestMethod.GET.equals(exchange))
        throw new ClientException("Invalid method.", HTTPStatus.HTTP_BAD_METHOD);

      // Extract body
      String body = getBodyAndLog(exchange);
      VoteRequestBody requestBody = gson.fromJson(body, VoteRequestBody.class);

      if (requestBody == null)
        throw new ClientException("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST);

      String userID = validateToken(requestBody.getToken());

      // Validate path
      String path = exchange.getRequestURI().getPath();

      if (!path.equals("/score"))
        throw new ClientException("Not found", HTTPStatus.HTTP_NOT_FOUND);

      int score = Config.dbDriver.getUserScore(userID);

      // Create token
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("userScore", score);

      respond(jsonObject.toString(), HTTPStatus.HTTP_OK, exchange);
    } catch (ClientException e) {
      respond(e, exchange);
    } catch (DatabaseException e) {
      logger.severe(e.getClass() + ": " + e.getMessage());
      respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
    }
  }
}

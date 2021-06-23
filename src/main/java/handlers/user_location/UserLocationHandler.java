package handlers.user_location;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import utils.Config;
import utils.ImageCompressor;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;

public final class UserLocationHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      String body = getBodyAndLog(exchange);

      switch (RequestMethod.parse(exchange)) {
        // -------------------------------------
        // HANDLE GET --------------------------
        // -------------------------------------
        case GET: {
          UserLocationGetRequestBody requestBody = gson.fromJson(body, UserLocationGetRequestBody.class);

          if (requestBody == null)
            throw new ClientException("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST);

          String userID = validateToken(requestBody.getToken());
          int locationID = Config.dbDriver.getUserLocation(userID);

          // Respond with locationID
          JsonObject jsonObject = new JsonObject();
          jsonObject.addProperty("locationID", locationID);

          respond(jsonObject.toString(), HTTPStatus.HTTP_OK, exchange);
          return;
        }

        // -------------------------------------
        // HANDLE POST -------------------------
        // -------------------------------------
        case POST: {
          UserLocationPostRequestBody requestBody = gson.fromJson(body, UserLocationPostRequestBody.class);

          if (requestBody == null)
            throw new ClientException("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST);

          String userID = validateToken(requestBody.getToken());
          int locationID = requestBody.getLocationID();

          Config.dbDriver.setUserLocation(userID, locationID);

          respond(HTTPStatus.HTTP_OK, exchange);
          return;
        }
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

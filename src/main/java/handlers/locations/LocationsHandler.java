package handlers.locations;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import utils.Config;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;

public final class LocationsHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    // Validate REST method
    if (!RequestMethod.GET.equals(exchange)) {
      respond(HTTPStatus.HTTP_BAD_METHOD, exchange);
      return;
    }

    try {
      // Get locations from DB
      ArrayList<Location> locations = Config.dbDriver.getLocations();

      respond(gson.toJson(locations), HTTPStatus.HTTP_OK, exchange);
    } catch (JsonSyntaxException e) {
      logger.info("Invalid login request body.");
      respond("Invalid login request body.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
    } catch (DatabaseException e) {
      logger.severe(e.getClass().toString() + ": " + e.getMessage());
      respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
    }
  }
}

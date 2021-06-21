package handlers.locations;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import utils.Config;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;

public final class LocationsHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      // Validate PATH
      if (!exchange.getRequestURI().getPath().equals("/locations"))
        throw new ClientException("Not found.", HTTPStatus.HTTP_NOT_FOUND);

      // Validate REST method
      if (!RequestMethod.GET.equals(exchange))
        throw new ClientException("Bad method.", HTTPStatus.HTTP_BAD_METHOD);

      // Get locations from DB
      Locations locations = Config.dbDriver.getLocations();

      respond(gson.toJson(locations), HTTPStatus.HTTP_OK, exchange);
    } catch (JsonSyntaxException e) {
      logger.info("Invalid login request body.");
      respond("Invalid login request body.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
    } catch (DatabaseException e) {
      logger.severe(e.getClass() + ": " + e.getMessage());
      respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
    } catch (ClientException e) {
      respond(e, exchange);
    }
  }
}

package handlers.login;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import utils.Config;
import utils.crypto.CryptoHelper;
import utils.net.HTTPStatus;

import java.io.IOException;

public class LoginHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String body = getBodyAndLog(exchange);

    try {
      LoginRequestBody loginRequestBody = gson.fromJson(body, LoginRequestBody.class);

      int userID = loginRequestBody.getUserID();
      int verification = loginRequestBody.getVerification();

      if (userID < 900000000 || userID > 999999999) {
        respond("Invalid Phone number format.", exchange, HTTPStatus.HTTP_BAD_REQUEST);
        return;
      }

      if (verification < 100000 || verification > 999999) {
        respond("Invalid verification code format.", exchange, HTTPStatus.HTTP_BAD_REQUEST);
        return;
      }

      if (Integer.toString(userID).substring(3, 9).equals(Integer.toString(verification))) {
        String userHash = CryptoHelper.hashToHexString(Integer.toString(userID));

        Config.dbDriver.createUserIfNotExists(userHash);

        respond(exchange, HTTPStatus.HTTP_ACCEPTED);
      } else {
        respond("Invalid verification code.", exchange, HTTPStatus.HTTP_UNAUTHORIZED);
      }

    } catch (JsonSyntaxException e) {
      logger.info("Invalid login request body.");
      respond("Invalid login request body.", exchange, HTTPStatus.HTTP_BAD_REQUEST);
    } catch (DatabaseException e) {
      logger.severe("Failed to check if user exists: " + e.getMessage());
      respond("Database Error.", exchange, HTTPStatus.HTTP_SERVER_ERROR);
    }
  }
}

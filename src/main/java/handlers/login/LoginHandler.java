package handlers.login;

import auth.WebToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import utils.Config;
import utils.crypto.CryptoHelper;
import utils.net.HTTPStatus;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LoginHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String body = getBodyAndLog(exchange);

    try {
      LoginRequestBody loginRequestBody = gson.fromJson(body, LoginRequestBody.class);

      int userID = loginRequestBody.getUserID();
      int verification = loginRequestBody.getVerification();

      // Validate number and verification
      if (userID < 900000000 || userID > 999999999) {
        respond("Invalid Phone number format.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
        return;
      }

      if (verification < 100000 || verification > 999999) {
        respond("Invalid verification code format.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
        return;
      }

      if (!Integer.toString(userID).substring(3, 9).equals(Integer.toString(verification))) {
        respond("Invalid verification code.", HTTPStatus.HTTP_UNAUTHORIZED, exchange);
        return;
      }

      // Create the user if it does not exist yet
      String userHash = CryptoHelper.hashToHexString(Integer.toString(userID));
      Config.dbDriver.createUserIfNotExists(userHash);

      // Create token
      WebToken token = new WebToken(userHash);
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("token", token.encrypt());

      respond(jsonObject.toString(), HTTPStatus.HTTP_OK, exchange);
    } catch (JsonSyntaxException e) {
      logger.info("Invalid login request body.");
      respond("Invalid login request body.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
    } catch (DatabaseException | GeneralSecurityException e) {
      logger.severe(e.getClass().toString() + ": " + e.getMessage());
      respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
    }
  }
}

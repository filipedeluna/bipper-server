package handlers.login;

import auth.WebToken;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import utils.Config;
import utils.crypto.CryptoHelper;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class LoginHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      // Validate Route
      if (!exchange.getRequestURI().getPath().equals("/login"))
        throw new ClientException("Invalid method.", HTTPStatus.HTTP_NOT_FOUND);

      // Validate REST method
      if (!RequestMethod.GET.equals(exchange))
        throw new ClientException("Invalid method.", HTTPStatus.HTTP_BAD_METHOD);

      // Get body
      String body = getBodyAndLog(exchange);
      LoginRequestBody requestBody = gson.fromJson(body, LoginRequestBody.class);

      if (requestBody == null)
        throw new ClientException("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST);

      int userID = requestBody.getUserID();
      int verification = requestBody.getVerification();

      // Validate number and verification
      if (userID < 900000000 || userID > 999999999)
        throw new ClientException("Invalid Phone number format.", HTTPStatus.HTTP_BAD_REQUEST);

      if (verification < 100000 || verification > 999999)
        throw new ClientException("Invalid verification code format.", HTTPStatus.HTTP_BAD_REQUEST);

      if (!Integer.toString(userID).substring(3, 9).equals(Integer.toString(verification)))
        throw new ClientException("Invalid verification code.", HTTPStatus.HTTP_UNAUTHORIZED);

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
    } catch (ClientException e) {
      respond(e, exchange);
    }
  }
}

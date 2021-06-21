package handlers.post;

import auth.WebToken;
import com.sun.net.httpserver.HttpExchange;
import db.error.DatabaseException;
import handlers.Handler;
import handlers.error.ClientException;
import utils.Config;
import utils.ImageCompressor;
import utils.net.HTTPStatus;
import utils.net.RequestMethod;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.IllegalFormatException;

public final class PostsHandler extends Handler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      switch (RequestMethod.parse(exchange)) {
        // -------------------------------------
        // HANDLE GET --------------------------
        // -------------------------------------
        case GET: {
          String body = getBodyAndLog(exchange);
          String path = exchange.getRequestURI().getPath();

          // Handle new posts
          if (path.equals("/posts/new")) {
            PostsGetRequestBody requestBody = gson.fromJson(body, PostsGetRequestBody.class);

            if (requestBody == null) {
              respond("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
              return;
            }

            try {
              WebToken token = WebToken.decrypt(requestBody.getToken());
              ArrayList<Post> posts = Config.dbDriver.getNewPosts(token.getUserID());

              respond(gson.toJson(posts), HTTPStatus.HTTP_OK, exchange);
              return;
            } catch (GeneralSecurityException e) {
              logger.severe(e.getClass().toString() + ": " + e.getMessage());
              respond(HTTPStatus.HTTP_SERVER_ERROR, exchange);
              return;
            }
          }

          // Handle top posts
          if (path.startsWith("/posts/top/")) {
            String periodString = path.replaceFirst("/posts/top/", "");
            PostPeriod postPeriod = PostPeriod.parse(periodString);

            if (postPeriod.equals(PostPeriod.NULL)) {
              respond("Invalid time period.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
              return;
            }

            ArrayList<Post> posts = Config.dbDriver.getTopPosts(postPeriod);

            respond(gson.toJson(posts), HTTPStatus.HTTP_OK, exchange);
            return;
          }
        }

        // -------------------------------------
        // HANDLE POST -------------------------
        // -------------------------------------
        case POST: {
          String body = getBodyAndLog(exchange);

          PostsPostRequestBody requestBody = gson.fromJson(body, PostsPostRequestBody.class);

          if (requestBody == null) {
            respond("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST, exchange);
            return;
          }

          String userID = validateToken(requestBody.getToken());
          int locationID = requestBody.getLocationID();
          String text = requestBody.getText();
          String image = requestBody.getImage();

          try {
            if (image.length() > 0)
              image = ImageCompressor.compress(image);
          } catch (IOException | IllegalArgumentException | UnsupportedOperationException | IllegalStateException e) {
            throw new ClientException("Invalid image. Please use a jpg.", HTTPStatus.HTTP_BAD_REQUEST);
          }

          Config.dbDriver.insertPost(userID, locationID, text, image);

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

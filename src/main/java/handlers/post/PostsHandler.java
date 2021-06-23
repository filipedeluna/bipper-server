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

          PostsGetRequestBody requestBody = gson.fromJson(body, PostsGetRequestBody.class);

          // Handle new posts
          if (path.equals("/posts/new")) {
            if (requestBody == null)
              throw new ClientException("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST);

            String userID = validateToken(requestBody.getToken());
            int index = requestBody.getIndex();

            if (index < 0)
              throw new ClientException("Invalid index", HTTPStatus.HTTP_BAD_REQUEST);

            ArrayList<Post> posts = Config.dbDriver.getNewPosts(userID, index);

            respond(gson.toJson(posts), HTTPStatus.HTTP_OK, exchange);
            return;
          } else if (path.startsWith("/posts/top/")) {
            // Handle top posts
            String periodString = path.replaceFirst("/posts/top/", "");
            PostPeriod postPeriod = PostPeriod.parse(periodString);

            if (postPeriod.equals(PostPeriod.NULL))
              throw new ClientException("Invalid time period.", HTTPStatus.HTTP_BAD_REQUEST);

            int index = requestBody.getIndex();

            if (index < 0)
              throw new ClientException("Invalid index", HTTPStatus.HTTP_BAD_REQUEST);

            ArrayList<Post> posts = Config.dbDriver.getTopPosts(postPeriod, index);

            respond(gson.toJson(posts), HTTPStatus.HTTP_OK, exchange);
            return;
          } else
            throw new ClientException("Not found.", HTTPStatus.HTTP_NOT_FOUND);
        }

        // -------------------------------------
        // HANDLE POST -------------------------
        // -------------------------------------
        case POST: {
          if (!exchange.getRequestURI().getPath().equals("/posts"))
            throw new ClientException("Not found", HTTPStatus.HTTP_NOT_FOUND);

          String body = getBodyAndLog(exchange);

          PostsPostRequestBody requestBody = gson.fromJson(body, PostsPostRequestBody.class);

          if (requestBody == null)
            throw new ClientException("Invalid body.", HTTPStatus.HTTP_BAD_REQUEST);

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

          if (text.length() < 20)
            throw new ClientException("Post text too short.", HTTPStatus.HTTP_BAD_REQUEST);

          if (text.length() > 300)
            throw new ClientException("Post text too long.", HTTPStatus.HTTP_BAD_REQUEST);

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

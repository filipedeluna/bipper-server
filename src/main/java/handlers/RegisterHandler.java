package handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public abstract class RegisterHandler implements HttpHandler {
 @Override
  public void handle(HttpExchange httpExchange) throws IOException {
    httpExchange.close();
  }
}

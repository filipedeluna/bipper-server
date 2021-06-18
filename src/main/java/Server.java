import com.sun.net.httpserver.HttpServer;
import utils.Config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {
  private static final String PROPS_PATH = "server.properties";

  public static void main(String[] args) throws IOException {
    // Check number of arguments
    if (args.length != 1) {
      System.out.println("Invalid number of arguments. Use \"help\".");
      System.exit(0);
    }

    // Check if person requested help
    if (args[0].equals("help")) {
      System.out.println("java -jar bipper_server.jar <properties file location>");
      System.exit(0);
    }

    // Parse properties and get logger
    Config.parse(args[0]);
    Logger logger = Config.getLogger(Server.class);

    logger.info("Properties successfully parsed.");

    // Build server
    System.setProperty("java.net.preferIPv4Stack", "true");
    InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", Config.serverPort);
    HttpServer server = HttpServer.create(serverAddress, 0);

    server.setExecutor(Executors.newFixedThreadPool(Config.serverThreads));

    server.createContext("/register", response -> {
      response.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
      response.close();
    });

    logger.info("Server is ready to handle requests at " + serverAddress);
    server.start();
  }
}

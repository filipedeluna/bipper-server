import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import handlers.Handler;
import handlers.locations.LocationsHandler;
import handlers.login.LoginHandler;
import handlers.post.PostsHandler;
import handlers.score.ScoreHandler;
import handlers.user_location.UserLocationHandler;
import handlers.vote.VoteHandler;
import utils.Config;
import utils.CustomLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;

public final class Server {
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
    CustomLogger logger = new CustomLogger(Server.class);

    logger.info("Properties successfully parsed.");

    // Additional configs
    System.setProperty("java.net.preferIPv4Stack", "true");

    // Build server
    InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", Config.serverPort);
    HttpServer server = HttpServer.create(serverAddress, 0);

    server.setExecutor(Executors.newFixedThreadPool(Config.serverThreads));

    // Register routes
    HashMap<String, Handler> routes = new HashMap<>();
    routes.put("/login", new LoginHandler());
    routes.put("/posts", new PostsHandler());
    routes.put("/locations", new LocationsHandler());
    routes.put("/vote", new VoteHandler());
    routes.put("/score", new ScoreHandler());
    routes.put("/user_location", new UserLocationHandler());

    // Add filters
    HashSet<Filter> filters = new HashSet<>();

    routes.entrySet().stream()
        .map(route -> server.createContext(route.getKey(), route.getValue()))
        .map(HttpContext::getFilters)
        .forEach(filterList -> filterList.addAll(filters));

    // Start server
    logger.info("Server is ready to handle requests at " + serverAddress);
    server.start();
  }
}

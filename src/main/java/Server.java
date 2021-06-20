import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import handlers.Handler;
import handlers.filters.LoggingFilter;
import handlers.login.LoginHandler;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import utils.Config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.security.Security;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Server {
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

    // Additional configs
    System.setProperty("java.net.preferIPv4Stack", "true");

    // Build server
    InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", Config.serverPort);
    HttpServer server = HttpServer.create(serverAddress, 0);

    server.setExecutor(Executors.newFixedThreadPool(Config.serverThreads));

    // Register routes
    HashMap<String, Handler> routes = new HashMap<>();
    routes.put("/login", new LoginHandler());

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

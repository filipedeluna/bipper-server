package handlers.filters;


import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import utils.Config;
import utils.net.SafeInputStreamReader;

import java.io.IOException;
import java.util.logging.Logger;

public final class LoggingFilter extends Filter {
  private static final Logger logger = Config.getLogger(LoggingFilter.class);

  @Override
  public void doFilter(HttpExchange httpExchange, Chain chain) throws IOException {
    logger.info("[REQUEST]: " + String.join("\t",
        "Method: " + httpExchange.getRequestMethod(),
        "URI: " + httpExchange.getRequestURI().getPath(),
        "From: " + httpExchange.getRemoteAddress().toString(),
        "Size: " + new SafeInputStreamReader(httpExchange.getRequestBody()).toString().length()
        )
    );

    chain.doFilter(httpExchange);
  }

  @Override
  public String description() {
    return "Logging Filter";
  }
}

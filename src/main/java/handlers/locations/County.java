package handlers.locations;

import java.util.LinkedHashMap;

public final class County {
  public LinkedHashMap<String, Integer> zones;

  public County(String name) {
    zones = new LinkedHashMap<>();
  }
}

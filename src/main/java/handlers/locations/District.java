package handlers.locations;

import java.util.LinkedHashMap;

public final class District {
  public LinkedHashMap<String, County> counties;

  public District(String name) {
    this.counties = new LinkedHashMap<>();
  }
}

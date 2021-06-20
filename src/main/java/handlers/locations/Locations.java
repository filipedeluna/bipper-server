package handlers.locations;

import java.util.ArrayList;
import java.util.Optional;

public class Locations {
  private final ArrayList<District> districts;

  public Locations() {
    districts = new ArrayList<>();
  }

  public void addZone(String districtName, String countyName, String zoneName, int locationID) {
    Optional<District> districtOpt = districts.stream().filter(d -> d.name.equals(districtName)).findFirst();

    District district;

    if (districtOpt.isEmpty()) {
      district = new District(districtName);
      districts.add(district);
    } else
      district = districtOpt.get();

    district.addZone(countyName, zoneName, locationID);
  }


  // --------------------------------------
  // HELPER CLASSES -----------------------
  // --------------------------------------
  private final class District {
    private String name;
    private ArrayList<County> counties;

    District() {
    }

    District(String name) {
      this.name = name;
      this.counties = new ArrayList<>();
    }

    private void addZone(String countyName, String zoneName, int locationID) {
      Optional<County> countyOpt = counties.stream().filter(c -> c.name.equals(countyName)).findFirst();

      County county;

      if (countyOpt.isEmpty()) {
        county = new County(countyName);
        counties.add(county);
      } else
        county = countyOpt.get();

      county.addZone(zoneName, locationID);
    }
  }

  private final class County {
    private String name;
    private ArrayList<Zone> zones;

    County() {
    }

    County(String name) {
      this.name = name;
      zones = new ArrayList<>();
    }

    private void addZone(String zoneName, int locationID) {
      if (zones.stream().noneMatch(z -> z.name.equals(zoneName)))
        zones.add(new Zone(zoneName, locationID));
    }
  }

  private final class Zone {
    private String name;
    private int locationID;

    Zone() {
    }

    public Zone(String name, int locationID) {
      this.name = name;
      this.locationID = locationID;
    }
  }
}

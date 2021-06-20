package handlers.locations;

public final class Location {
  private int locationID;
  private String district;
  private String county;
  private String zone;

  public Location() {
  }

  public Location(int locationID, String district, String county, String zone) {
    this.locationID = locationID;
    this.district = district;
    this.county = county;
    this.zone = zone;
  }
}

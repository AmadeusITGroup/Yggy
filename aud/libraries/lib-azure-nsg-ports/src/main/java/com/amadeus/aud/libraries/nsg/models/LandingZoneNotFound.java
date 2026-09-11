package com.amadeus.aud.libraries.nsg.models;

public class LandingZoneNotFound extends RuntimeException {
  public LandingZoneNotFound(String landingZone) {
    super("Landing zone not found: " + landingZone);
  }
}

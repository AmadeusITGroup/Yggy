package com.amadeus.aud.libraries.nsg.ports;

import com.amadeus.aud.libraries.nsg.models.NetworkSecurity;
import io.smallrye.mutiny.Uni;

public interface LandingZoneNetworkSecurityPort {
  Uni<NetworkSecurity> getNetworkSecurity(String version, String landingZoneId);
}

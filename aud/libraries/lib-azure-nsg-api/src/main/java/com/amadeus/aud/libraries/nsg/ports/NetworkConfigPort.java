package com.amadeus.aud.libraries.nsg.ports;

import com.amadeus.aud.libraries.nsg.models.NetworkConfig;
import io.smallrye.mutiny.Uni;

public interface NetworkConfigPort {
  Uni<NetworkConfig> getNetworkConfig(String version, String networkName);
}

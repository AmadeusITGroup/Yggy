package com.amadeus.aud.libraries.nsg.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.nsg.models.NetworkConfig;
import com.amadeus.aud.libraries.nsg.models.NetworkSecurity;
import com.amadeus.aud.libraries.nsg.ports.LandingZoneNetworkSecurityPort;
import com.amadeus.aud.libraries.nsg.ports.NetworkConfigPort;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NsgResourcesTest {

  @Test
  void landingZoneResourceAddsResponseUrlsAtHttpBoundary() {
    LandingZoneNetworkSecurityPort port = mock(LandingZoneNetworkSecurityPort.class);
    UriInfo uriInfo = uriInfo("http://localhost/landingzone/lz/network-security");
    NetworkSecurity networkSecurity =
        new NetworkSecurity(null, null, "1.2.3", "api", Map.of(), Map.of(), null);
    when(port.getNetworkSecurity("latest", "lz"))
        .thenReturn(Uni.createFrom().item(networkSecurity));
    LandingZoneResource resource = new LandingZoneResource(port);
    resource.uriInfo = uriInfo;

    NetworkSecurity responseEntity =
        (NetworkSecurity) resource.getNetworkSecurity("lz").await().indefinitely().getEntity();

    assertThat(responseEntity.getUrl())
        .isEqualTo("http://localhost/landingzone/lz/network-security");
    assertThat(responseEntity.getUrlVersion())
        .isEqualTo("http://localhost/version/1.2.3/landingzone/lz/network-security");
  }

  @Test
  void networkResourceAddsResponseUrlsAtHttpBoundary() {
    NetworkConfigPort port = mock(NetworkConfigPort.class);
    UriInfo uriInfo = uriInfo("http://localhost/version/1.2.3/network/network-a/config");
    NetworkConfig networkConfig = new NetworkConfig(null, null, "1.2.3", "api", null, Map.of());
    when(port.getNetworkConfig("1.2.3", "network-a"))
        .thenReturn(Uni.createFrom().item(networkConfig));
    NetworkResource resource = new NetworkResource(port);
    resource.uriInfo = uriInfo;

    NetworkConfig responseEntity =
        (NetworkConfig)
            resource.getNetworkConfig("1.2.3", "network-a").await().indefinitely().getEntity();

    assertThat(responseEntity.getUrl())
        .isEqualTo("http://localhost/version/1.2.3/network/network-a/config");
    assertThat(responseEntity.getUrlVersion())
        .isEqualTo("http://localhost/version/1.2.3/version/1.2.3/network/network-a/config");
  }

  private UriInfo uriInfo(String requestUri) {
    UriInfo uriInfo = mock(UriInfo.class);
    URI uri = URI.create(requestUri);
    when(uriInfo.getAbsolutePath()).thenReturn(uri);
    when(uriInfo.getRequestUri()).thenReturn(uri);
    return uriInfo;
  }
}

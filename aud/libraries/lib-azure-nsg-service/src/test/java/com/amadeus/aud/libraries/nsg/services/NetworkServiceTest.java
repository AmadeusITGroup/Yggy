package com.amadeus.aud.libraries.nsg.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.nsg.models.LandingZoneNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NetworkServiceTest {

  private AudApiConfig audApiConfig;
  private AudService audService;
  private NetworkService service;

  @BeforeEach
  void setUp() {
    audApiConfig = mock(AudApiConfig.class);
    audService = mock(AudService.class);
    service = new NetworkService(audApiConfig, audService);
  }

  @Test
  void getNetworkConfig_throwsLandingZoneNotFound_whenLandingZoneIsNull() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();

    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    doReturn(true).when(landingZonesRepository).isReady();
    doReturn(null).when(landingZonesRepository).get("unknown-network");
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();

    assertThatThrownBy(() -> service.getNetworkConfig(version, "unknown-network"))
        .isInstanceOf(LandingZoneNotFound.class)
        .hasMessageContaining("unknown-network");
  }
}

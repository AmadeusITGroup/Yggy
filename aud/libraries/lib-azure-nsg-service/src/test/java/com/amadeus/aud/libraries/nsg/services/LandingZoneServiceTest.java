package com.amadeus.aud.libraries.nsg.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.nsg.models.LandingZoneNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LandingZoneServiceTest {

  private NsgService nsgService;
  private AudApiConfig audApiConfig;
  private AudService audService;
  private LandingZoneService service;

  @BeforeEach
  void setUp() {
    nsgService = mock(NsgService.class);
    audApiConfig = mock(AudApiConfig.class);
    audService = mock(AudService.class);
    service = new LandingZoneService(nsgService, audApiConfig, audService);
  }

  @Test
  void getNetworkSecurity_throwsLandingZoneNotFound_whenLandingZoneIsNull() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();

    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    doReturn(true).when(landingZonesRepository).isReady();
    doReturn(null).when(landingZonesRepository).get("unknown-lz");
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();

    assertThatThrownBy(() -> service.getNetworkSecurity(version, "unknown-lz"))
        .isInstanceOf(LandingZoneNotFound.class)
        .hasMessageContaining("unknown-lz");
  }

  @Test
  void getNetworkSecurity_throwsIllegalStateException_whenProviderIsNotAzure() {
    Version version = mock(Version.class);
    doReturn("1.0.0").when(version).getVersionNumber();

    LandingZonesRepository landingZonesRepository = mock(LandingZonesRepository.class);
    doReturn(true).when(landingZonesRepository).isReady();
    doReturn(landingZonesRepository).when(version).getLandingZonesRepository();

    LandingZone landingZone = mock(LandingZone.class);
    doReturn(landingZone).when(landingZonesRepository).get("aws-lz");

    Target target = mock(Target.class);
    doReturn("AWS").when(target).getProviderName();
    doReturn(target).when(landingZone).getTarget();

    assertThatThrownBy(() -> service.getNetworkSecurity(version, "aws-lz"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("not in Azure");
  }
}

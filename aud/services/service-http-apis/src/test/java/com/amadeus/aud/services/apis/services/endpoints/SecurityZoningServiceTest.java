package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.SecurityZoningResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("SecurityZoningService Tests")
class SecurityZoningServiceTest {

  @Mock private AudApiConfig audApiConfig;
  @Mock private AudService audService;
  @Mock private UriInfo uriInfo;
  @Mock private Version version;

  private SecurityZoningService service;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    service = new SecurityZoningService(audApiConfig, audService);

    objectMapper = EndpointTestObjectMapper.get();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(version.getVersionNumber()).thenReturn("1.0.0");
  }

  private void stubResponseMetadata() throws Exception {
    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath())
        .thenReturn(new URI("http://localhost:8080/api/security-zoning"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("http://localhost:8080/api/security-zoning"));
  }

  @Test
  @DisplayName("should return security zoning response with subnet instances")
  void getSecurityZoningWithValidSubnets() throws Exception {
    stubResponseMetadata();

    ZoneType zoneType1 = mock(ZoneType.class);
    when(zoneType1.getName()).thenReturn("zone-type-1");

    ZoneType zoneType2 = mock(ZoneType.class);
    when(zoneType2.getName()).thenReturn("zone-type-2");

    ZoneInstance zone1 = mock(ZoneInstance.class);
    when(zone1.getName()).thenReturn("zone-1");
    when(zone1.getZoneType()).thenReturn(zoneType1);

    ZoneInstance zone2 = mock(ZoneInstance.class);
    when(zone2.getName()).thenReturn("zone-2");
    when(zone2.getZoneType()).thenReturn(zoneType2);

    Target target1 = mock(Target.class);
    when(target1.getName()).thenReturn("target-1");

    Target target2 = mock(Target.class);
    when(target2.getName()).thenReturn("target-2");

    LandingZone lz1 = mock(LandingZone.class);
    when(lz1.getTarget()).thenReturn(target1);
    when(lz1.getName()).thenReturn("lz-prod");

    LandingZone lz2 = mock(LandingZone.class);
    when(lz2.getTarget()).thenReturn(target2);
    when(lz2.getName()).thenReturn("lz-staging");

    SubnetInstance subnet1 = mock(SubnetInstance.class);
    when(subnet1.getLandingZone()).thenReturn(lz1);
    when(subnet1.getZoneInstance()).thenReturn(zone1);
    when(subnet1.getName()).thenReturn("subnet-1");
    when(subnet1.getCidr()).thenReturn("10.0.1.0/24");
    when(subnet1.isGroup()).thenReturn(false);
    when(subnet1.getParent()).thenReturn(null);

    SubnetInstance subnet2 = mock(SubnetInstance.class);
    when(subnet2.getLandingZone()).thenReturn(lz2);
    when(subnet2.getZoneInstance()).thenReturn(zone2);
    when(subnet2.getName()).thenReturn("subnet-2");
    when(subnet2.getCidr()).thenReturn("10.1.1.0/24");
    when(subnet2.isGroup()).thenReturn(false);
    when(subnet2.getParent()).thenReturn(null);

    ConcurrentHashMap<String, SubnetInstance> subnetData = new ConcurrentHashMap<>();
    subnetData.put("subnet-1", subnet1);
    subnetData.put("subnet-2", subnet2);

    SubnetInstancesRepository subnetRepository = mock(SubnetInstancesRepository.class);
    when(subnetRepository.getData()).thenReturn(subnetData);
    when(subnetRepository.isReady()).thenReturn(true);
    when(version.getSubnetInstancesRepository()).thenReturn(subnetRepository);

    Response response = service.getSecurityZoning(version, uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SecurityZoningResponse body = objectMapper.readValue(bodyBytes, SecurityZoningResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getSubnets()).isNotNull();

    @SuppressWarnings("unchecked")
    Map<String, Object> subnets = (Map<String, Object>) body.getSubnets();
    assertThat(subnets.keySet()).hasSize(2);
  }

  @Test
  @DisplayName("should retrieve version from audService when using string version")
  void getSecurityZoningWithStringVersion() throws Exception {
    stubResponseMetadata();

    ZoneType zoneType = mock(ZoneType.class);
    when(zoneType.getName()).thenReturn("zone-type-1");

    ZoneInstance zone = mock(ZoneInstance.class);
    when(zone.getName()).thenReturn("zone-1");
    when(zone.getZoneType()).thenReturn(zoneType);

    Target target = mock(Target.class);
    when(target.getName()).thenReturn("target-1");

    LandingZone lz = mock(LandingZone.class);
    when(lz.getTarget()).thenReturn(target);
    when(lz.getName()).thenReturn("lz-prod");

    SubnetInstance subnet = mock(SubnetInstance.class);
    when(subnet.getLandingZone()).thenReturn(lz);
    when(subnet.getZoneInstance()).thenReturn(zone);
    when(subnet.getName()).thenReturn("subnet-1");
    when(subnet.getCidr()).thenReturn("10.0.1.0/24");
    when(subnet.isGroup()).thenReturn(false);
    when(subnet.getParent()).thenReturn(null);

    ConcurrentHashMap<String, SubnetInstance> subnetData = new ConcurrentHashMap<>();
    subnetData.put("subnet-1", subnet);

    SubnetInstancesRepository subnetRepository = mock(SubnetInstancesRepository.class);
    when(subnetRepository.getData()).thenReturn(subnetData);
    when(subnetRepository.isReady()).thenReturn(true);
    when(version.getSubnetInstancesRepository()).thenReturn(subnetRepository);

    when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(version));

    Response response = service.getSecurityZoning("1.0.0", uriInfo).await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    byte[] bodyBytes = (byte[]) response.getEntity();
    SecurityZoningResponse body = objectMapper.readValue(bodyBytes, SecurityZoningResponse.class);

    assertThat(body).isNotNull();
    assertThat(body.getVersion()).isEqualTo("1.0.0");
  }

  @Test
  @DisplayName("should throw ServiceUnavailableException when subnet data is null")
  void getSecurityZoningWithNullData() {
    SubnetInstancesRepository subnetRepository = mock(SubnetInstancesRepository.class);
    when(subnetRepository.getData()).thenReturn(null);
    when(subnetRepository.isReady()).thenReturn(true);
    when(version.getSubnetInstancesRepository()).thenReturn(subnetRepository);

    Throwable thrown =
        catchThrowable(() -> service.getSecurityZoning(version, uriInfo).await().indefinitely());

    assertThat(thrown).isInstanceOf(ServiceUnavailableException.class);
  }
}

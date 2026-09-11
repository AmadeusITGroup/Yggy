package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.IPAddressInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.IpResponse;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec.EndpointTypeEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IpReportServiceTest {
  private AudService audService;
  private AudApiConfig audApiConfig;
  private UriInfo uriInfo;
  private ObjectMapper objectMapper;
  private IpReportService service;

  @BeforeEach
  void setUp() throws Exception {
    audService = mock(AudService.class);
    audApiConfig = mock(AudApiConfig.class);
    uriInfo = mock(UriInfo.class);
    objectMapper = EndpointTestObjectMapper.get();
    service = new TestIpReportService(audService, audApiConfig, objectMapper);

    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("https://aud.example/ip-report"));
    when(uriInfo.getRequestUri()).thenReturn(new URI("https://aud.example/ip-report"));
  }

  @Test
  void validIpMatchesSubnetParentsCidrComponentsAndIpInstances() throws Exception {
    Version version = populatedVersion();

    Response response = service.getIpReport(version, "10.0.0.7", uriInfo).await().indefinitely();
    IpResponse body = body(response);

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(body.getSubnets()).extracting("name").containsExactlyInAnyOrder("child", "parent");
    assertThat(body.getCidrs()).extracting("name").containsExactly("matching-component");
    assertThat(body.getIps()).extracting("name").containsExactly("10.0.0.7");
    assertThat(body.getErrors()).isNullOrEmpty();
    assertThat(body.getVersion()).isEqualTo("1.2.3");
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getUrl()).isEqualTo("https://aud.example/ip-report");
    assertThat(body.getUrlVersion()).isEqualTo("https://aud.example/version/1.2.3/ip-report");
  }

  @Test
  void invalidIpReturnsSuccessfulResponseWithErrorAndEmptyMatches() throws Exception {
    Version version = populatedVersion();

    Response response = service.getIpReport(version, "not-an-ip", uriInfo).await().indefinitely();
    IpResponse body = body(response);

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(body.getErrors()).containsExactly("Invalid IP address: not-an-ip");
    assertThat(body.getSubnets()).isNullOrEmpty();
    assertThat(body.getCidrs()).isNullOrEmpty();
    assertThat(body.getIps()).isNullOrEmpty();
    assertThat(body.getUrlVersion()).isNullOrEmpty();
  }

  @Test
  void stringVersionWrapperResolvesVersion() throws Exception {
    Version version = populatedVersion();
    when(audService.getVersion("current")).thenReturn(Uni.createFrom().item(version));

    Response response = service.getIpReport("current", "10.0.0.7", uriInfo).await().indefinitely();

    assertThat(body(response).getIps()).hasSize(1);
    verify(audService).getVersion("current");
  }

  @Test
  void unavailableRepositoryFailsBeforeProcessingIp() {
    Version version = new Version();
    version.setVersionNumber("1.2.3");

    assertThatThrownBy(
            () -> service.getIpReport(version, "10.0.0.7", uriInfo).await().indefinitely())
        .isInstanceOf(ServiceUnavailableException.class)
        .hasMessage("Subnet Instances repository is not ready for version: 1.2.3");
  }

  private IpResponse body(Response response) throws Exception {
    return objectMapper.readValue((byte[]) response.getEntity(), IpResponse.class);
  }

  private Version populatedVersion() {
    Scope scope = mock(Scope.class);
    LandingZone landingZone = mock(LandingZone.class);
    Target target = mock(Target.class);
    Region region = mock(Region.class);
    stubReportContext(scope, landingZone, target, region);

    SubnetInstance parent = reportSubnet("parent", "10.0.0.0/16", null, scope, landingZone);
    SubnetInstance child = reportSubnet("child", "10.0.0.0/24", parent, scope, landingZone);
    SubnetInstance group = mock(SubnetInstance.class);
    when(group.isGroup()).thenReturn(true);
    SubnetInstance allAddresses = mock(SubnetInstance.class);
    when(allAddresses.isGroup()).thenReturn(false);
    when(allAddresses.getCidr()).thenReturn("0.0.0.0/0");
    SubnetInstance malformed = mock(SubnetInstance.class);
    when(malformed.isGroup()).thenReturn(false);
    when(malformed.getCidr()).thenReturn("invalid/cidr");
    SubnetInstance unrelated = mock(SubnetInstance.class);
    when(unrelated.isGroup()).thenReturn(false);
    when(unrelated.getCidr()).thenReturn("192.0.2.0/24");

    ComponentInstance matching = reportComponent(scope, landingZone);
    when(matching.getName()).thenReturn("matching-component");
    when(matching.getEndpointType()).thenReturn(EndpointTypeEnum.CID_RLIST);
    when(matching.getEndpoints()).thenReturn(List.of(42, "invalid/cidr", "10.0.0.7"));
    ComponentInstance wrongType = mock(ComponentInstance.class);
    when(wrongType.getEndpointType()).thenReturn(EndpointTypeEnum.UR_LLIST);
    ComponentInstance unrelatedComponent = mock(ComponentInstance.class);
    when(unrelatedComponent.getEndpointType()).thenReturn(EndpointTypeEnum.CID_RLIST);
    when(unrelatedComponent.getEndpoints()).thenReturn(List.of("192.0.2.1"));

    IPAddressInstance matchingIp = reportIp(scope, landingZone, matching, child);
    IPAddressInstance unrelatedIp = mock(IPAddressInstance.class);
    when(unrelatedIp.getName()).thenReturn("192.0.2.1");

    SubnetInstancesRepository subnetRepository = new SubnetInstancesRepository();
    subnetRepository.put("child", child);
    subnetRepository.put("group", group);
    subnetRepository.put("all", allAddresses);
    subnetRepository.put("malformed", malformed);
    subnetRepository.put("unrelated", unrelated);
    ComponentInstancesRepository componentRepository = new ComponentInstancesRepository();
    componentRepository.put("matching", matching);
    componentRepository.put("wrong-type", wrongType);
    componentRepository.put("unrelated", unrelatedComponent);
    IPAddressInstancesRepository ipRepository = new IPAddressInstancesRepository();
    ipRepository.put("matching", matchingIp);
    ipRepository.put("unrelated", unrelatedIp);

    Version version = new Version();
    version.setVersionNumber("1.2.3");
    version.setSubnetInstancesRepository(subnetRepository);
    version.setComponentInstancesRepository(componentRepository);
    version.setIpAddressInstancesRepository(ipRepository);
    return version;
  }

  private static void stubReportContext(
      Scope scope, LandingZone landingZone, Target target, Region region) {
    when(scope.getAssigneeGroup()).thenReturn("network-team");
    when(landingZone.getTarget()).thenReturn(target);
    when(landingZone.getRegion()).thenReturn(region);
    when(landingZone.getRegionName()).thenReturn("eu-west");
    when(landingZone.getTargetName()).thenReturn("cloud-target");
    when(landingZone.getLandingZoneTypeName()).thenReturn("spoke");
    when(landingZone.getMacroPhaseName()).thenReturn("production");
    when(target.getProviderName()).thenReturn("cloud-provider");
    when(target.isCloud()).thenReturn(false);
    when(region.getContinentName()).thenReturn("Europe");
    when(region.getURLHash()).thenReturn("region-hash");
  }

  private static SubnetInstance reportSubnet(
      String name, String cidr, SubnetInstance parent, Scope scope, LandingZone landingZone) {
    SubnetInstance subnet = mock(SubnetInstance.class);
    when(subnet.getName()).thenReturn(name);
    when(subnet.getCidr()).thenReturn(cidr);
    when(subnet.isGroup()).thenReturn(false);
    when(subnet.getParent()).thenReturn(parent);
    when(subnet.getScopeName()).thenReturn("network");
    when(subnet.getScope()).thenReturn(scope);
    when(subnet.getLandingZone()).thenReturn(landingZone);
    when(subnet.getLandingZoneName()).thenReturn("landing-zone");
    when(subnet.getSubnetTypeName()).thenReturn("workload");
    when(subnet.isShared()).thenReturn(false);
    when(subnet.getZoneInstance()).thenReturn(null);
    when(subnet.getURLHash()).thenReturn(name + "-hash");
    when(subnet.getComponentInstances()).thenReturn(null);
    return subnet;
  }

  private static ComponentInstance reportComponent(Scope scope, LandingZone landingZone) {
    ComponentInstance component = mock(ComponentInstance.class);
    when(component.getScopeName()).thenReturn("network");
    when(component.getScope()).thenReturn(scope);
    when(component.getLandingZone()).thenReturn(landingZone);
    when(component.getLandingZoneName()).thenReturn("landing-zone");
    when(component.getComponentName()).thenReturn("application");
    when(component.getApplicationPhaseName()).thenReturn("production");
    when(component.getURLHash()).thenReturn("component-hash");
    return component;
  }

  private static IPAddressInstance reportIp(
      Scope scope, LandingZone landingZone, ComponentInstance component, SubnetInstance subnet) {
    IPAddressInstance ip = mock(IPAddressInstance.class);
    when(ip.getName()).thenReturn("10.0.0.7");
    when(ip.getHost()).thenReturn("app.example");
    when(ip.getScopeName()).thenReturn("network");
    when(ip.getScope()).thenReturn(scope);
    when(ip.getLandingZone()).thenReturn(landingZone);
    when(ip.getLandingZoneName()).thenReturn("landing-zone");
    when(ip.getComponentInstance()).thenReturn(component);
    when(component.getInSubnetInstance()).thenReturn(subnet);
    when(ip.getURLHash()).thenReturn("ip-hash");
    return ip;
  }

  private static final class TestIpReportService extends IpReportService {
    private TestIpReportService(
        AudService audService, AudApiConfig audApiConfig, ObjectMapper objectMapper) {
      super(audService, audApiConfig);
      setObjectMapper(objectMapper);
    }
  }
}

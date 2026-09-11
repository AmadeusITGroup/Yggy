package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import com.amadeus.aud.libraries.yaml.models.ObjectGroupExtract;
import com.amadeus.aud.libraries.yaml.models.OpenResponse;
import com.amadeus.aud.libraries.yaml.models.SecurityRule;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec.EndpointTypeEnum;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import com.amadeus.aud.services.apis.utils.SecurityRuleUtils;
import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AudOpenReportServiceTest {

  private static final String TEST_URI = "http://localhost:8080/api/open-report";
  private static final String SOURCE_COMPONENT = "source-component";
  private static final String DESTINATION_COMPONENT = "dest-component";
  private static final String SOURCE_ID = "src-id";
  private static final String DESTINATION_ID = "dest-id";

  @Mock private AudService audService;

  @Mock private GraphEngineService graphEngineService;

  @Mock private AudApiConfig audApiConfig;

  @Mock private IpReportService ipReportService;

  @Mock private Version version;

  @Mock private ComponentInstancesRepository componentInstancesRepository;

  @Mock private DependencyInstancesRepository dependencyInstancesRepository;

  @Mock private UriInfo uriInfo;

  private AudOpenReportService service;
  private URI mockUri;

  @BeforeEach
  void setUp() throws Exception {
    service =
        spy(
            new AudOpenReportService(
                audService, graphEngineService, audApiConfig, ipReportService));

    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(version.getComponentInstancesRepository()).thenReturn(componentInstancesRepository);
    when(version.getDependencyInstancesRepository()).thenReturn(dependencyInstancesRepository);
    when(audApiConfig.version()).thenReturn("v1");

    doNothing().when(service).checkRepository(any(), anyString(), anyString());

    mockUri = new URI(TEST_URI);
    when(uriInfo.getAbsolutePath()).thenReturn(mockUri);
    when(uriInfo.getRequestUri()).thenReturn(mockUri);
  }

  private ComponentInstance createMockComponentInstance(String uniqueId) {
    ComponentInstance instance = mock(ComponentInstance.class);
    when(instance.id()).thenReturn(uniqueId);
    return instance;
  }

  // Use fully qualified name to avoid import conflict with
  // com.amadeus.aud.libraries.engine.data.models.core.internals.Endpoint
  private com.amadeus.aud.libraries.yaml.models.Endpoint createMockEndpoint(
      String typeEnum, String value, String description) {
    com.amadeus.aud.libraries.yaml.models.Endpoint endpoint =
        new com.amadeus.aud.libraries.yaml.models.Endpoint();
    endpoint.setType(typeEnum);
    endpoint.setValue(value);
    endpoint.setDescription(description);
    return endpoint;
  }

  private ObjectGroup createObjectGroup(String name, String uid, String typeValue) {
    ObjectGroup objectGroup = new ObjectGroup();
    objectGroup.setName(name);
    objectGroup.setUid(uid);
    objectGroup.setZoneType("internal");
    objectGroup.setType(ObjectGroup.TypeEnum.fromValue(typeValue));
    objectGroup.setEndpoints(List.of(createMockEndpoint("", "10.10.10.10/32", "some description")));
    return objectGroup;
  }

  private Map<Direction, ObjectGroup> createObjectGroupMap(
      Direction direction, ObjectGroup objectGroup) {
    Map<Direction, ObjectGroup> map = new HashMap<>();
    map.put(direction, objectGroup);
    return map;
  }

  @Nested
  class GetOpenReportResponseErrors {

    @Test
    @DisplayName("with empty source instances should return not found")
    void errorSourceNotFound() throws Exception {
      // Arrange
      when(componentInstancesRepository.get(SOURCE_COMPONENT)).thenReturn(null);

      ComponentInstance destInstance = createMockComponentInstance(DESTINATION_ID);
      when(componentInstancesRepository.get(DESTINATION_COMPONENT)).thenReturn(destInstance);

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response =
          service.getOpenReportResponse(version, SOURCE_COMPONENT, DESTINATION_COMPONENT, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors())
          .hasSize(1)
          .anyMatch(e -> e.contains("Source not found: " + SOURCE_COMPONENT));
    }

    @Test
    @DisplayName("with empty destination instances should return not found")
    void errorDestinationNotFound() throws Exception {
      // Arrange
      ComponentInstance srcInstance = createMockComponentInstance(SOURCE_ID);
      when(componentInstancesRepository.get(SOURCE_COMPONENT)).thenReturn(srcInstance);

      when(componentInstancesRepository.get(DESTINATION_COMPONENT)).thenReturn(null);

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response =
          service.getOpenReportResponse(version, SOURCE_COMPONENT, DESTINATION_COMPONENT, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors())
          .hasSize(1)
          .anyMatch(e -> e.contains("Destination not found: " + DESTINATION_COMPONENT));
    }

    @Test
    @DisplayName("with IP addresses and no components should return not found")
    void errorNoComponentsForIps() throws Exception {
      // Arrange
      String sourceIp = "192.168.1.1";
      String destIp = "10.0.0.1";

      when(componentInstancesRepository.getData()).thenReturn(new ConcurrentHashMap<>());

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response = service.getOpenReportResponse(version, sourceIp, destIp, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors()).hasSize(2);
      assertThat(actual.getErrors())
          .anyMatch(e -> e.contains("No components found for IP: " + sourceIp))
          .anyMatch(e -> e.contains("No components found for IP: " + destIp));
      assertThat(actual.getSrcIsIp()).isTrue();
      assertThat(actual.getDstIsIp()).isTrue();
    }

    @Test
    @DisplayName("with no graph edges should return error")
    void errorNoGraphEdges() throws Exception {
      // Arrange
      ComponentInstance srcInstance = createMockComponentInstance(SOURCE_ID);
      ComponentInstance destInstance = createMockComponentInstance(DESTINATION_ID);

      when(componentInstancesRepository.get(SOURCE_COMPONENT)).thenReturn(srcInstance);
      when(componentInstancesRepository.get(DESTINATION_COMPONENT)).thenReturn(destInstance);

      when(graphEngineService.getGraphEdgesByNodeIds(
              anyString(), anyInt(), anyInt(), anyList(), anyList()))
          .thenReturn(Uni.createFrom().nullItem());

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response =
          service.getOpenReportResponse(version, SOURCE_COMPONENT, DESTINATION_COMPONENT, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors())
          .hasSize(1)
          .anyMatch(e -> e.contains("No graph data or edges available"));
    }
  }

  @Nested
  class GetOpenReportResponseSuccess {

    @Test
    @DisplayName("with valid component source and destination should return security rule")
    void withComponentSourceAndDestination() throws Exception {
      // Arrange
      ComponentInstance srcInstance = createMockComponentInstance(SOURCE_ID);
      ComponentInstance destInstance = createMockComponentInstance(DESTINATION_ID);

      when(componentInstancesRepository.get(SOURCE_COMPONENT)).thenReturn(srcInstance);
      when(componentInstancesRepository.get(DESTINATION_COMPONENT)).thenReturn(destInstance);

      ObjectGroup srcObjectGroup = createObjectGroup("src-object-group", "src-uid", "AddressGroup");
      ObjectGroup dstObjectGroup = createObjectGroup("dst-object-group", "dst-uid", "AddressGroup");

      when(srcInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.OUT, srcObjectGroup));
      when(destInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.IN, dstObjectGroup));

      Edge edge = new Edge(null, "dep-id", "service-id", true, SOURCE_ID, DESTINATION_ID);

      when(graphEngineService.getGraphEdgesByNodeIds(
              anyString(), anyInt(), anyInt(), anyList(), anyList()))
          .thenReturn(Uni.createFrom().item(Collections.singletonList(edge)));

      DependencyInstance dependency = mock(DependencyInstance.class);
      Service serviceObj = mock(Service.class);
      Scope scope = mock(Scope.class);

      when(dependencyInstancesRepository.get("dep-id")).thenReturn(dependency);
      when(dependency.getServices()).thenReturn(Collections.singleton(serviceObj));
      when(dependency.getScopeName()).thenReturn("test-scope");
      when(dependency.getScope()).thenReturn(scope);
      when(dependency.getName()).thenReturn("test-dependency");
      when(dependency.getURLHash()).thenReturn("test-hash");
      when(scope.getAssigneeGroup()).thenReturn("test-group");

      when(serviceObj.id()).thenReturn("service-id");
      when(serviceObj.getName()).thenReturn("test-service");
      when(serviceObj.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
      TreeRangeSet<Integer> portsRange = TreeRangeSet.create();
      portsRange.add(Range.singleton(443));
      when(serviceObj.getPortsRange()).thenReturn(portsRange);

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response =
          service.getOpenReportResponse(version, SOURCE_COMPONENT, DESTINATION_COMPONENT, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors()).isEmpty();
      assertThat(actual.getSrcIsIp()).isFalse();
      assertThat(actual.getDstIsIp()).isFalse();

      assertThat(actual.getRules()).hasSize(1);

      SecurityRule rule = actual.getRules().getFirst();
      assertThat(rule.getName()).isEqualTo("ar-from-src-id-to-dest-id-test-service");
      assertThat(rule.getUid()).isEqualTo(SecurityRuleUtils.DEFAULT_UID);
      assertThat(rule.getScope()).isEqualTo("test-scope");
      assertThat(rule.getOwner()).isEqualTo("test-group");
      assertThat(rule.getDependencyInstanceName()).isEqualTo("test-dependency");
      assertThat(rule.getDependencyInstanceHash()).isEqualTo("test-hash");
      assertThat(rule.getProtocol()).isEqualTo(SecurityRule.ProtocolEnum.TCP);
      assertThat(rule.getPorts()).containsExactlyElementsOf(List.of("443"));

      assertThat(rule.getSrcObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract srcOg) -> {
                assertThat(srcOg.getName()).isEqualTo("src-object-group");
                assertThat(srcOg.getUid()).isEqualTo("src-uid");
                assertThat(srcOg.getZoneType()).isEqualTo("internal");
                assertThat(srcOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(srcOg.getEndpoints()).hasSize(1);
                assertThat(srcOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });

      assertThat(rule.getDstObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract dstOg) -> {
                assertThat(dstOg.getName()).isEqualTo("dst-object-group");
                assertThat(dstOg.getUid()).isEqualTo("dst-uid");
                assertThat(dstOg.getZoneType()).isEqualTo("internal");
                assertThat(dstOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(dstOg.getEndpoints()).hasSize(1);
                assertThat(dstOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });
    }

    @Test
    @DisplayName("with valid IP addresses should return security rule")
    void withIpSourceAndIpDestination() throws Exception {
      // Arrange
      String sourceIp = "192.168.1.1";
      String destIp = "10.0.0.1";

      ComponentInstance srcInstance = createMockComponentInstance(SOURCE_ID);
      ComponentInstance destInstance = createMockComponentInstance(DESTINATION_ID);

      when(srcInstance.getScopeName()).thenReturn("test-scope");
      when(destInstance.getScopeName()).thenReturn("test-scope");
      when(srcInstance.getEndpointType()).thenReturn(null);
      when(destInstance.getEndpointType()).thenReturn(null);

      Endpoint srcEndpoint = mock(Endpoint.class);
      when(srcEndpoint.getType()).thenReturn(Endpoint.TypeEnum.CIDR);
      when(srcEndpoint.getValue()).thenReturn("192.168.1.0/24");

      Endpoint dstEndpoint = mock(Endpoint.class);
      when(dstEndpoint.getType()).thenReturn(Endpoint.TypeEnum.CIDR);
      when(dstEndpoint.getValue()).thenReturn("10.0.0.0/24");

      when(srcInstance.getInternalEndpoints(Direction.OUT))
          .thenReturn(Collections.singleton(srcEndpoint));
      when(srcInstance.getInternalEndpoints(Direction.IN)).thenReturn(Collections.emptySet());
      when(destInstance.getInternalEndpoints(Direction.OUT)).thenReturn(Collections.emptySet());
      when(destInstance.getInternalEndpoints(Direction.IN))
          .thenReturn(Collections.singleton(dstEndpoint));

      ConcurrentHashMap<String, ComponentInstance> componentsMap = new ConcurrentHashMap<>();
      componentsMap.put(SOURCE_ID, srcInstance);
      componentsMap.put(DESTINATION_ID, destInstance);

      when(componentInstancesRepository.getData()).thenReturn(componentsMap);

      ObjectGroup srcObjectGroup = createObjectGroup("src-object-group", "src-uid", "AddressGroup");
      ObjectGroup dstObjectGroup = createObjectGroup("dst-object-group", "dst-uid", "AddressGroup");

      when(srcInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.OUT, srcObjectGroup));
      when(destInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.IN, dstObjectGroup));

      Edge edge = new Edge(null, "dep-id", "service-id", true, SOURCE_ID, DESTINATION_ID);

      when(graphEngineService.getGraphEdgesByNodeIds(
              anyString(), anyInt(), anyInt(), anyList(), anyList()))
          .thenReturn(Uni.createFrom().item(Collections.singletonList(edge)));

      DependencyInstance dependency = mock(DependencyInstance.class);
      Service serviceObj = mock(Service.class);
      Scope scope = mock(Scope.class);

      when(dependencyInstancesRepository.get("dep-id")).thenReturn(dependency);
      when(dependency.getServices()).thenReturn(Collections.singleton(serviceObj));
      when(dependency.getScopeName()).thenReturn("test-scope");
      when(dependency.getScope()).thenReturn(scope);
      when(dependency.getName()).thenReturn("test-dependency");
      when(dependency.getURLHash()).thenReturn("test-hash");
      when(scope.getAssigneeGroup()).thenReturn("test-group");

      when(serviceObj.id()).thenReturn("service-id");
      when(serviceObj.getName()).thenReturn("test-service");
      when(serviceObj.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
      TreeRangeSet<Integer> portsRange = TreeRangeSet.create();
      portsRange.add(Range.singleton(443));
      when(serviceObj.getPortsRange()).thenReturn(portsRange);

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response = service.getOpenReportResponse(version, sourceIp, destIp, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors()).isEmpty();
      assertThat(actual.getSrcIsIp()).isTrue();
      assertThat(actual.getDstIsIp()).isTrue();

      assertThat(actual.getRules()).hasSize(1);

      SecurityRule rule = actual.getRules().getFirst();
      assertThat(rule.getName()).isEqualTo("ar-from-src-id-to-dest-id-test-service");
      assertThat(rule.getUid()).isEqualTo(SecurityRuleUtils.DEFAULT_UID);
      assertThat(rule.getScope()).isEqualTo("test-scope");
      assertThat(rule.getOwner()).isEqualTo("test-group");
      assertThat(rule.getDependencyInstanceName()).isEqualTo("test-dependency");
      assertThat(rule.getDependencyInstanceHash()).isEqualTo("test-hash");
      assertThat(rule.getProtocol()).isEqualTo(SecurityRule.ProtocolEnum.TCP);
      assertThat(rule.getPorts()).containsExactlyElementsOf(List.of("443"));

      assertThat(rule.getSrcObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract srcOg) -> {
                assertThat(srcOg.getName()).isEqualTo("src-object-group");
                assertThat(srcOg.getUid()).isEqualTo("src-uid");
                assertThat(srcOg.getZoneType()).isEqualTo("internal");
                assertThat(srcOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(srcOg.getEndpoints()).hasSize(1);
                assertThat(srcOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });

      assertThat(rule.getDstObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract dstOg) -> {
                assertThat(dstOg.getName()).isEqualTo("dst-object-group");
                assertThat(dstOg.getUid()).isEqualTo("dst-uid");
                assertThat(dstOg.getZoneType()).isEqualTo("internal");
                assertThat(dstOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(dstOg.getEndpoints()).hasSize(1);
                assertThat(dstOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });
    }

    @Test
    @DisplayName("with IP source and component destination should return security rule")
    void withIpSourceAndComponentDestination() throws Exception {
      // Arrange
      String sourceIp = "192.168.1.1";
      ComponentInstance destInstance = createMockComponentInstance(DESTINATION_ID);
      when(destInstance.getScopeName()).thenReturn("test-scope");

      // Source component for IP matching
      ComponentInstance srcInstance = createMockComponentInstance(SOURCE_ID);
      when(srcInstance.getScopeName()).thenReturn("test-scope");
      when(srcInstance.getEndpointType()).thenReturn(null);

      Endpoint srcEndpoint = mock(Endpoint.class);
      when(srcEndpoint.getType()).thenReturn(Endpoint.TypeEnum.CIDR);
      when(srcEndpoint.getValue()).thenReturn("192.168.1.0/24");

      when(srcInstance.getInternalEndpoints(Direction.OUT))
          .thenReturn(Collections.singleton(srcEndpoint));

      when(componentInstancesRepository.get(DESTINATION_COMPONENT)).thenReturn(destInstance);

      ConcurrentHashMap<String, ComponentInstance> componentsMap = new ConcurrentHashMap<>();
      componentsMap.put(SOURCE_ID, srcInstance);
      componentsMap.put(DESTINATION_COMPONENT, destInstance);

      when(componentInstancesRepository.getData()).thenReturn(componentsMap);

      ObjectGroup srcObjectGroup = createObjectGroup("src-object-group", "src-uid", "AddressGroup");
      ObjectGroup dstObjectGroup = createObjectGroup("dst-object-group", "dst-uid", "AddressGroup");

      when(srcInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.OUT, srcObjectGroup));
      when(destInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.IN, dstObjectGroup));

      Edge edge = new Edge(null, "dep-id", "service-id", true, SOURCE_ID, DESTINATION_ID);

      when(graphEngineService.getGraphEdgesByNodeIds(
              anyString(), anyInt(), anyInt(), anyList(), anyList()))
          .thenReturn(Uni.createFrom().item(Collections.singletonList(edge)));

      DependencyInstance dependency = mock(DependencyInstance.class);
      Service serviceObj = mock(Service.class);
      Scope scope = mock(Scope.class);

      when(dependencyInstancesRepository.get("dep-id")).thenReturn(dependency);
      when(dependency.getServices()).thenReturn(Collections.singleton(serviceObj));
      when(dependency.getScopeName()).thenReturn("test-scope");
      when(dependency.getScope()).thenReturn(scope);
      when(dependency.getName()).thenReturn("test-dependency");
      when(dependency.getURLHash()).thenReturn("test-hash");
      when(scope.getAssigneeGroup()).thenReturn("test-group");

      when(serviceObj.id()).thenReturn("service-id");
      when(serviceObj.getName()).thenReturn("test-service");
      when(serviceObj.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
      TreeRangeSet<Integer> portsRange = TreeRangeSet.create();
      portsRange.add(Range.singleton(443));
      when(serviceObj.getPortsRange()).thenReturn(portsRange);

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response =
          service.getOpenReportResponse(version, sourceIp, DESTINATION_COMPONENT, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors()).isEmpty();
      assertThat(actual.getSrcIsIp()).isTrue();
      assertThat(actual.getDstIsIp()).isFalse();

      assertThat(actual.getRules()).hasSize(1);

      SecurityRule rule = actual.getRules().getFirst();
      assertThat(rule.getName()).isEqualTo("ar-from-src-id-to-dest-id-test-service");
      assertThat(rule.getUid()).isEqualTo(SecurityRuleUtils.DEFAULT_UID);
      assertThat(rule.getScope()).isEqualTo("test-scope");
      assertThat(rule.getOwner()).isEqualTo("test-group");
      assertThat(rule.getDependencyInstanceName()).isEqualTo("test-dependency");
      assertThat(rule.getDependencyInstanceHash()).isEqualTo("test-hash");
      assertThat(rule.getProtocol()).isEqualTo(SecurityRule.ProtocolEnum.TCP);
      assertThat(rule.getPorts()).containsExactlyElementsOf(List.of("443"));

      assertThat(rule.getSrcObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract srcOg) -> {
                assertThat(srcOg.getName()).isEqualTo("src-object-group");
                assertThat(srcOg.getUid()).isEqualTo("src-uid");
                assertThat(srcOg.getZoneType()).isEqualTo("internal");
                assertThat(srcOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(srcOg.getEndpoints()).hasSize(1);
                assertThat(srcOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });

      assertThat(rule.getDstObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract dstOg) -> {
                assertThat(dstOg.getName()).isEqualTo("dst-object-group");
                assertThat(dstOg.getUid()).isEqualTo("dst-uid");
                assertThat(dstOg.getZoneType()).isEqualTo("internal");
                assertThat(dstOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(dstOg.getEndpoints()).hasSize(1);
                assertThat(dstOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });
    }

    @Test
    @DisplayName("with CID_RLIST endpoint type should return security rule")
    void withCidRlistEndpointType() throws Exception {
      // Arrange
      String sourceIp = "192.168.1.1";
      String destIp = "10.0.0.1";

      ComponentInstance srcInstance = createMockComponentInstance(SOURCE_ID);
      ComponentInstance destInstance = createMockComponentInstance(DESTINATION_ID);

      when(srcInstance.getScopeName()).thenReturn("test-scope");
      when(destInstance.getScopeName()).thenReturn("test-scope");
      when(srcInstance.getEndpointType()).thenReturn(EndpointTypeEnum.CID_RLIST);
      when(destInstance.getEndpointType()).thenReturn(EndpointTypeEnum.CID_RLIST);
      when(srcInstance.getEndpoints()).thenReturn(List.of("192.168.1.0/24"));
      when(destInstance.getEndpoints()).thenReturn(List.of("10.0.0.0/24"));

      ConcurrentHashMap<String, ComponentInstance> componentsMap = new ConcurrentHashMap<>();
      componentsMap.put(SOURCE_ID, srcInstance);
      componentsMap.put(DESTINATION_ID, destInstance);

      when(componentInstancesRepository.getData()).thenReturn(componentsMap);

      ObjectGroup srcObjectGroup = createObjectGroup("src-object-group", "src-uid", "AddressGroup");
      ObjectGroup dstObjectGroup = createObjectGroup("dst-object-group", "dst-uid", "AddressGroup");

      when(srcInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.OUT, srcObjectGroup));
      when(destInstance.getObjectGroups(false))
          .thenReturn(createObjectGroupMap(Direction.IN, dstObjectGroup));

      Edge edge = new Edge(null, "dep-id", "service-id", true, SOURCE_ID, DESTINATION_ID);

      when(graphEngineService.getGraphEdgesByNodeIds(
              anyString(), anyInt(), anyInt(), anyList(), anyList()))
          .thenReturn(Uni.createFrom().item(Collections.singletonList(edge)));

      DependencyInstance dependency = mock(DependencyInstance.class);
      Service serviceObj = mock(Service.class);
      Scope scope = mock(Scope.class);

      when(dependencyInstancesRepository.get("dep-id")).thenReturn(dependency);
      when(dependency.getServices()).thenReturn(Collections.singleton(serviceObj));
      when(dependency.getScopeName()).thenReturn("test-scope");
      when(dependency.getScope()).thenReturn(scope);
      when(dependency.getName()).thenReturn("test-dependency");
      when(dependency.getURLHash()).thenReturn("test-hash");
      when(scope.getAssigneeGroup()).thenReturn("test-group");

      when(serviceObj.id()).thenReturn("service-id");
      when(serviceObj.getName()).thenReturn("test-service");
      when(serviceObj.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
      TreeRangeSet<Integer> portsRange = TreeRangeSet.create();
      portsRange.add(Range.singleton(443));
      when(serviceObj.getPortsRange()).thenReturn(portsRange);

      doReturn(mock(Response.class)).when(service).getOkResponse(any(OpenResponse.class));

      // Act
      Response response = service.getOpenReportResponse(version, sourceIp, destIp, uriInfo);

      // Assert
      ArgumentCaptor<OpenResponse> captor = ArgumentCaptor.forClass(OpenResponse.class);
      verify(service).getOkResponse(captor.capture());

      OpenResponse actual = captor.getValue();
      assertThat(response).isNotNull();
      assertThat(actual.getErrors()).isEmpty();
      assertThat(actual.getSrcIsIp()).isTrue();
      assertThat(actual.getDstIsIp()).isTrue();

      assertThat(actual.getRules()).hasSize(1);

      var rule = actual.getRules().getFirst();
      assertThat(rule.getName()).isEqualTo("ar-from-src-id-to-dest-id-test-service");
      assertThat(rule.getUid()).isEqualTo(SecurityRuleUtils.DEFAULT_UID);
      assertThat(rule.getScope()).isEqualTo("test-scope");
      assertThat(rule.getOwner()).isEqualTo("test-group");
      assertThat(rule.getDependencyInstanceName()).isEqualTo("test-dependency");
      assertThat(rule.getDependencyInstanceHash()).isEqualTo("test-hash");
      assertThat(rule.getProtocol()).isEqualTo(SecurityRule.ProtocolEnum.TCP);
      assertThat(rule.getPorts()).containsExactlyElementsOf(List.of("443"));

      assertThat(rule.getSrcObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract srcOg) -> {
                assertThat(srcOg.getName()).isEqualTo("src-object-group");
                assertThat(srcOg.getUid()).isEqualTo("src-uid");
                assertThat(srcOg.getZoneType()).isEqualTo("internal");
                assertThat(srcOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(srcOg.getEndpoints()).hasSize(1);
                assertThat(srcOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });

      assertThat(rule.getDstObjectGroup())
          .isNotNull()
          .satisfies(
              (ObjectGroupExtract dstOg) -> {
                assertThat(dstOg.getName()).isEqualTo("dst-object-group");
                assertThat(dstOg.getUid()).isEqualTo("dst-uid");
                assertThat(dstOg.getZoneType()).isEqualTo("internal");
                assertThat(dstOg.getType()).isEqualTo(ObjectGroupExtract.TypeEnum.ADDRESS_GROUP);
                assertThat(dstOg.getEndpoints()).hasSize(1);
                assertThat(dstOg.getEndpoints().getFirst().getValue()).isEqualTo("10.10.10.10/32");
              });
    }
  }
}

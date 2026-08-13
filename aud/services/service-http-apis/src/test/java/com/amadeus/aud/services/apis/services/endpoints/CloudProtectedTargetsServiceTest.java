package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.CloudProtectedTargetsResponse;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroup;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroupSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1Target;
import com.amadeus.aud.libraries.yaml.models.V1TargetSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CloudProtectedTargetsService Tests")
@Execution(ExecutionMode.SAME_THREAD)
class CloudProtectedTargetsServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private UriInfo uriInfo;
  @Mock private Version version;
  @Mock private TargetsRepository targetsRepository;

  private CloudProtectedTargetsService service;

  @BeforeEach
  void setUp() throws Exception {
    service = spy(new CloudProtectedTargetsService(audService, audApiConfig));

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(version.getTargetsRepository()).thenReturn(targetsRepository);
    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath())
        .thenReturn(new URI("http://localhost:8080/api/cloud-protected-targets"));
    when(uriInfo.getRequestUri())
        .thenReturn(new URI("http://localhost:8080/api/cloud-protected-targets"));
  }

  private CloudProtectedTargetsResponse extractBody(Response response) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    byte[] bodyBytes = (byte[]) response.getEntity();
    return objectMapper.readValue(bodyBytes, CloudProtectedTargetsResponse.class);
  }

  private Provider createProvider(String name) {
    Provider provider = mock(Provider.class);
    when(provider.getName()).thenReturn(name);
    return provider;
  }

  private FirewallGroup createFirewallGroup(String name) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);

    V1FirewallGroupSpec spec = new V1FirewallGroupSpec();
    spec.setExternalIps(new ArrayList<>());

    V1FirewallGroup v1FirewallGroup = new V1FirewallGroup();
    v1FirewallGroup.setMetadata(metadata);
    v1FirewallGroup.setSpec(spec);

    return new FirewallGroup(v1FirewallGroup);
  }

  private Target createTarget(
      String name,
      boolean isCloud,
      Provider provider,
      List<String> cidrs,
      Set<FirewallGroup> firewallGroups) {

    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);

    V1TargetSpec spec = new V1TargetSpec();
    spec.setIsCloud(isCloud);
    spec.setCidrs(cidrs);

    V1Target v1Target = new V1Target();
    v1Target.setMetadata(metadata);
    v1Target.setSpec(spec);

    Target target = new Target(v1Target);
    target.setProvider(provider);
    target.setFirewallGroups(firewallGroups);

    return target;
  }

  @Nested
  @DisplayName("Filtering Logic Tests")
  class FilteringLogicTests {

    @Test
    @DisplayName("should include cloud targets with non-Internal provider and firewall groups")
    void shouldIncludeValidCloudTargets() throws Exception {
      Provider awsProvider = createProvider("AWS");
      Set<FirewallGroup> fwGroups = new HashSet<>();
      fwGroups.add(createFirewallGroup("fw-group-1"));

      Target cloudTarget =
          createTarget("cloud-prod-dc", true, awsProvider, List.of("10.0.0.0/8"), fwGroups);

      ConcurrentHashMap<String, Target> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("cloud-prod-dc", cloudTarget);
      when(targetsRepository.getData()).thenReturn(repositoryData);
      when(targetsRepository.isReady()).thenReturn(true);

      Response response = service.getCloudProtectedTargets(version, uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getCloudProtectedTargets()).hasSize(1);

      @SuppressWarnings("unchecked")
      Map<String, Object> entry = (Map<String, Object>) body.getCloudProtectedTargets().get(0);
      assertThat(entry.get("name")).isEqualTo("cloud-prod-dc");
      @SuppressWarnings("unchecked")
      List<String> cidr = (List<String>) entry.get("cidrs");
      assertThat(cidr).contains("10.0.0.0/8");
      assertThat((List<?>) entry.get("firewallgroups")).hasSize(1);
    }

    @Test
    @DisplayName("should exclude internal provider targets")
    void shouldExcludeInternalProviderTargets() throws Exception {
      Provider internalProvider = createProvider("Amadeus");
      Set<FirewallGroup> fwGroups = new HashSet<>();
      fwGroups.add(createFirewallGroup("fw-internal"));

      Target internalTarget =
          createTarget("internal-dc", true, internalProvider, List.of("10.0.0.0/8"), fwGroups);

      ConcurrentHashMap<String, Target> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("internal-dc", internalTarget);
      when(targetsRepository.getData()).thenReturn(repositoryData);
      when(targetsRepository.isReady()).thenReturn(true);

      Response response = service.getCloudProtectedTargets(version, uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getCloudProtectedTargets()).isEmpty();
    }

    @Test
    @DisplayName("should exclude non-cloud targets")
    void shouldExcludeNonCloudTargets() throws Exception {
      Provider gcpProvider = createProvider("GCP");
      Set<FirewallGroup> fwGroups = new HashSet<>();
      fwGroups.add(createFirewallGroup("fw-on-prem"));

      Target onPremTarget =
          createTarget(
              "datacenter-on-prem", false, gcpProvider, List.of("192.168.0.0/16"), fwGroups);

      ConcurrentHashMap<String, Target> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("datacenter-on-prem", onPremTarget);
      when(targetsRepository.getData()).thenReturn(repositoryData);
      when(targetsRepository.isReady()).thenReturn(true);

      Response response = service.getCloudProtectedTargets(version, uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getCloudProtectedTargets()).isEmpty();
    }

    @Test
    @DisplayName("should exclude targets without firewall groups")
    void shouldExcludeTargetsWithoutFirewallGroups() throws Exception {
      Provider azureProvider = createProvider("AZURE");

      Target cloudTargetNoFW =
          createTarget("cloud-azure-dc", true, azureProvider, List.of("172.16.0.0/12"), null);

      ConcurrentHashMap<String, Target> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("cloud-azure-dc", cloudTargetNoFW);
      when(targetsRepository.getData()).thenReturn(repositoryData);
      when(targetsRepository.isReady()).thenReturn(true);

      Response response = service.getCloudProtectedTargets(version, uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getCloudProtectedTargets()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Multiple Targets Mixed Scenarios")
  class MixedScenariosTests {

    @Test
    @DisplayName("should correctly filter mixed targets with multiple valid and invalid entries")
    void shouldFilterMultipleMixedTargets() throws Exception {
      Provider awsProvider = createProvider("AWS");
      Provider gcpProvider = createProvider("GCP");
      Provider internalProvider = createProvider("Amadeus");

      Set<FirewallGroup> fwGroup1 = new HashSet<>();
      fwGroup1.add(createFirewallGroup("fw-cloud-aws"));
      Target validTarget1 =
          createTarget("cloud-prod-aws", true, awsProvider, List.of("10.0.0.0/8"), fwGroup1);

      Set<FirewallGroup> fwGroup2 = new HashSet<>();
      fwGroup2.add(createFirewallGroup("fw-cloud-gcp-prod"));
      fwGroup2.add(createFirewallGroup("fw-cloud-gcp-backup"));
      Target validTarget2 =
          createTarget("cloud-gcp-asia", true, gcpProvider, List.of("10.128.0.0/9"), fwGroup2);

      Set<FirewallGroup> fwGroup3 = new HashSet<>();
      fwGroup3.add(createFirewallGroup("fw-internal"));
      Target invalidTarget1 =
          createTarget(
              "internal-data-center", true, internalProvider, List.of("172.16.0.0/12"), fwGroup3);

      Target invalidTarget2 =
          createTarget("cloud-test-aws", true, awsProvider, List.of("10.50.0.0/16"), null);

      Set<FirewallGroup> fwGroup5 = new HashSet<>();
      fwGroup5.add(createFirewallGroup("fw-on-premises"));
      Target invalidTarget3 =
          createTarget(
              "datacenter-physical", false, awsProvider, List.of("192.168.0.0/16"), fwGroup5);

      ConcurrentHashMap<String, Target> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("cloud-prod-aws", validTarget1);
      repositoryData.put("cloud-gcp-asia", validTarget2);
      repositoryData.put("internal-data-center", invalidTarget1);
      repositoryData.put("cloud-test-aws", invalidTarget2);
      repositoryData.put("datacenter-physical", invalidTarget3);
      when(targetsRepository.getData()).thenReturn(repositoryData);
      when(targetsRepository.isReady()).thenReturn(true);

      Response response = service.getCloudProtectedTargets(version, uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getCloudProtectedTargets()).hasSize(2);

      Set<String> returnedNames =
          new HashSet<>(
              body.getCloudProtectedTargets().stream()
                  .map(
                      (Object t) -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> entry = (Map<String, Object>) t;
                        return (String) entry.get("name");
                      })
                  .toList());

      assertThat(returnedNames).contains("cloud-prod-aws", "cloud-gcp-asia");
      assertThat(returnedNames)
          .doesNotContain("internal-data-center", "cloud-test-aws", "datacenter-physical");
    }
  }

  @Nested
  @DisplayName("Response Structure Tests")
  class ResponseStructureTests {

    @Test
    @DisplayName("should include metadata in response (version, apiVersion, url)")
    void shouldIncludeMetadataInResponse() throws Exception {
      Provider awsProvider = createProvider("AWS");
      Set<FirewallGroup> fwGroups = new HashSet<>();
      fwGroups.add(createFirewallGroup("fw-prod"));

      Target cloudTarget =
          createTarget("cloud-prod-dc", true, awsProvider, List.of("10.0.0.0/8"), fwGroups);

      ConcurrentHashMap<String, Target> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("cloud-prod-dc", cloudTarget);
      when(targetsRepository.getData()).thenReturn(repositoryData);
      when(targetsRepository.isReady()).thenReturn(true);

      Response response = service.getCloudProtectedTargets(version, uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getVersion()).isEqualTo("1.0.0");
      assertThat(body.getApiVersion()).isEqualTo("v1");
      assertThat(body.getUrl()).isNotNull();
      assertThat(body.getUrlVersion()).isNotNull();
    }

    @Test
    @DisplayName("should return empty list when no targets match criteria")
    void shouldReturnEmptyListWhenNoTargetsMatch() throws Exception {
      ConcurrentHashMap<String, Target> emptyRepository = new ConcurrentHashMap<>();
      when(targetsRepository.getData()).thenReturn(emptyRepository);
      when(targetsRepository.isReady()).thenReturn(true);

      Response response = service.getCloudProtectedTargets(version, uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getCloudProtectedTargets()).isEmpty();
      assertThat(body.getVersion()).isEqualTo("1.0.0");
    }
  }

  @Nested
  @DisplayName("String Version Parameter Tests")
  class StringVersionParameterTests {

    @Test
    @DisplayName("should handle String version parameter and resolve it via AudService")
    void shouldHandleStringVersionParameter() throws Exception {
      Provider awsProvider = createProvider("AWS");
      Set<FirewallGroup> fwGroups = new HashSet<>();
      fwGroups.add(createFirewallGroup("fw-prod"));

      Target cloudTarget =
          createTarget("cloud-prod-dc", true, awsProvider, List.of("10.0.0.0/8"), fwGroups);

      ConcurrentHashMap<String, Target> repositoryData = new ConcurrentHashMap<>();
      repositoryData.put("cloud-prod-dc", cloudTarget);
      when(targetsRepository.getData()).thenReturn(repositoryData);
      when(targetsRepository.isReady()).thenReturn(true);

      when(audService.getVersion("1.0.0")).thenReturn(Uni.createFrom().item(version));

      Response response = service.getCloudProtectedTargets("1.0.0", uriInfo).await().indefinitely();
      CloudProtectedTargetsResponse body = extractBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getCloudProtectedTargets()).hasSize(1);
    }
  }
}

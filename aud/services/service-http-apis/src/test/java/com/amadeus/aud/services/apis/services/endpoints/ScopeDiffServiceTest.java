package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.internals.Requirements;
import com.amadeus.aud.libraries.yaml.models.ScopeDiffResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("ScopeDiffService Tests")
class ScopeDiffServiceTest {

  @Mock private AudService audService;
  @Mock private AudApiConfig audApiConfig;
  @Mock private AudModelConfig audModelConfig;
  @Mock private UriInfo uriInfo;

  private ScopeDiffService service;
  private ObjectMapper objectMapper;
  private Version oldVersion;
  private Version newVersion;

  @BeforeEach
  void setUp() throws Exception {
    objectMapper = EndpointTestObjectMapper.get();

    service = new ScopeDiffService(audService, audApiConfig, audModelConfig);

    Field objectMapperField = BaseService.class.getDeclaredField("objectMapper");
    objectMapperField.setAccessible(true);
    objectMapperField.set(service, objectMapper);

    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/api/scope-diff"));
  }

  private ScopeDiffResponse extractResponseBody(Response response) throws Exception {
    byte[] bodyBytes = (byte[]) response.getEntity();
    return objectMapper.readValue(bodyBytes, ScopeDiffResponse.class);
  }

  private Requirement createRequirement(String scope, String version) {
    Requirement req = mock(Requirement.class);
    when(req.getScope()).thenReturn(scope);
    when(req.getVersion()).thenReturn(version);
    return req;
  }

  private Scope createScope() {
    return createScope(new HashSet<>());
  }

  private Scope createScope(Set<ScopeReleaseNote> releaseNotes) {
    Scope scope = mock(Scope.class);
    when(scope.getScopeReleaseNotes()).thenReturn(releaseNotes);
    return scope;
  }

  private ScopeReleaseNote createReleaseNote(String scopeVersion, List<String> prNumbers) {
    ScopeReleaseNote note = mock(ScopeReleaseNote.class);
    when(note.getScopeVersion()).thenReturn(scopeVersion);
    when(note.getPrs()).thenReturn(prNumbers);
    when(note.getCommits()).thenReturn(List.of());
    return note;
  }

  @Nested
  @DisplayName("Scope Diff Detection Tests")
  class ScopeDiffDetectionTests {

    @Test
    @DisplayName("should detect scope added")
    void shouldDetectScopeAdded() throws Exception {
      Requirement oldReq = createRequirement("firewall", "1.0.0");
      Requirement newReq1 = createRequirement("firewall", "1.0.0");
      Requirement newReq2 = createRequirement("network", "1.0.0");

      setupVersionMocks(List.of(oldReq), List.of(newReq1, newReq2), createScopesMap("network"));

      Response response =
          service.getScopeVersionsDiff(oldVersion, newVersion, uriInfo).await().indefinitely();
      ScopeDiffResponse body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getScopes().getDiff()).hasSize(1);
      assertThat(body.getScopes().getDiff().get(0).getScope()).isEqualTo("network");
      assertThat(body.getScopes().getDiff().get(0).getOldVersion()).isEqualTo("N/A");
      assertThat(body.getScopes().getDiff().get(0).getNewVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("should detect scope removed")
    void shouldDetectScopeRemoved() throws Exception {
      Requirement oldReq1 = createRequirement("firewall", "1.0.0");
      Requirement oldReq2 = createRequirement("network", "1.0.0");
      Requirement newReq = createRequirement("firewall", "1.0.0");

      setupVersionMocks(List.of(oldReq1, oldReq2), List.of(newReq), createScopesMap("network"));

      Response response =
          service.getScopeVersionsDiff(oldVersion, newVersion, uriInfo).await().indefinitely();
      ScopeDiffResponse body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getScopes().getDiff()).hasSize(1);
      assertThat(body.getScopes().getDiff().get(0).getScope()).isEqualTo("network");
      assertThat(body.getScopes().getDiff().get(0).getOldVersion()).isEqualTo("1.0.0");
      assertThat(body.getScopes().getDiff().get(0).getNewVersion()).isEqualTo("N/A");
    }

    @Test
    @DisplayName("should detect scope version changed")
    void shouldDetectScopeVersionChanged() throws Exception {
      Requirement oldReq = createRequirement("firewall", "1.0.0");
      Requirement newReq = createRequirement("firewall", "2.0.0");

      setupVersionMocks(List.of(oldReq), List.of(newReq), createScopesMap("firewall"));

      Response response =
          service.getScopeVersionsDiff(oldVersion, newVersion, uriInfo).await().indefinitely();
      ScopeDiffResponse body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getScopes().getDiff()).hasSize(1);
      assertThat(body.getScopes().getDiff().get(0).getScope()).isEqualTo("firewall");
      assertThat(body.getScopes().getDiff().get(0).getOldVersion()).isEqualTo("1.0.0");
      assertThat(body.getScopes().getDiff().get(0).getNewVersion()).isEqualTo("2.0.0");
    }

    @Test
    @DisplayName("should not include unchanged scopes")
    void shouldNotIncludeUnchangedScopes() throws Exception {
      Requirement oldReq = createRequirement("firewall", "1.0.0");
      Requirement newReq = createRequirement("firewall", "1.0.0");

      setupVersionMocks(List.of(oldReq), List.of(newReq));

      Response response =
          service.getScopeVersionsDiff(oldVersion, newVersion, uriInfo).await().indefinitely();
      ScopeDiffResponse body = extractResponseBody(response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(body.getScopes().getDiff()).isNull();
    }
  }

  @Nested
  @DisplayName("Release Notes Filtering Tests")
  class ReleaseNotesFilteringTests {

    @Test
    @DisplayName("should include PRs between version range")
    void shouldIncludePRsBetweenVersionRange() throws Exception {
      ScopeReleaseNote note15 = createReleaseNote("1.5.0", List.of("101"));
      ScopeReleaseNote note20 = createReleaseNote("2.0.0", List.of("102"));

      Scope firewall = createScope(new HashSet<>(List.of(note15, note20)));
      when(audModelConfig.defaultScopesBitbucketUrlPrefix())
          .thenReturn("https://bitbucket.example.com/projects/AUD/repos/");

      Requirement oldReq = createRequirement("firewall", "1.0.0");
      Requirement newReq = createRequirement("firewall", "2.5.0");

      ConcurrentHashMap<String, Scope> scopesMap = new ConcurrentHashMap<>();
      scopesMap.put("firewall", firewall);

      setupVersionMocks(List.of(oldReq), List.of(newReq), scopesMap);

      Response response =
          service.getScopeVersionsDiff(oldVersion, newVersion, uriInfo).await().indefinitely();
      ScopeDiffResponse body = extractResponseBody(response);

      assertThat(body.getScopes().getDiff()).hasSize(1);
      assertThat(body.getScopes().getDiff().get(0).getPrs()).hasSize(2);
    }
  }

  private void setupVersionMocks(
      List<Requirement> oldRequirements,
      List<Requirement> newRequirements,
      ConcurrentMap<String, Scope> scopes) {
    ScopesRepository scopesRepo = setupVersionMocks(oldRequirements, newRequirements);
    when(scopesRepo.getData()).thenReturn(scopes);
  }

  private ScopesRepository setupVersionMocks(
      List<Requirement> oldRequirements, List<Requirement> newRequirements) {
    oldVersion = mock(Version.class);
    newVersion = mock(Version.class);

    AudRequirementsRepository oldRepo = mock(AudRequirementsRepository.class);
    when(oldRepo.getData()).thenReturn(new Requirements(oldRequirements));
    when(oldRepo.isReady()).thenReturn(true);
    when(oldVersion.getAudRequirementsRepository()).thenReturn(oldRepo);
    when(oldVersion.getVersionNumber()).thenReturn("1.0.0");

    AudRequirementsRepository newRepo = mock(AudRequirementsRepository.class);
    when(newRepo.getData()).thenReturn(new Requirements(newRequirements));
    when(newRepo.isReady()).thenReturn(true);
    when(newVersion.getAudRequirementsRepository()).thenReturn(newRepo);
    when(newVersion.getVersionNumber()).thenReturn("2.0.0");

    ScopesRepository scopesRepo = mock(ScopesRepository.class);
    when(scopesRepo.isReady()).thenReturn(true);
    when(newVersion.getScopesRepository()).thenReturn(scopesRepo);
    return scopesRepo;
  }

  private ConcurrentHashMap<String, Scope> createScopesMap(String... scopeNames) {
    ConcurrentHashMap<String, Scope> map = new ConcurrentHashMap<>();
    for (String name : scopeNames) {
      map.put(name, createScope());
    }
    return map;
  }
}

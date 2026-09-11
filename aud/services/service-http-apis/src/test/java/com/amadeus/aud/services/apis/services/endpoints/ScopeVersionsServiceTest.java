package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.services.apis.models.http.responses.scopeversions.ScopeVersionsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScopeVersionsServiceTest {
  private AudService audService;
  private AudApiConfig audApiConfig;
  private UriInfo uriInfo;
  private ObjectMapper objectMapper;
  private ScopeVersionsService service;

  @BeforeEach
  void setUp() throws Exception {
    audService = mock(AudService.class);
    audApiConfig = mock(AudApiConfig.class);
    uriInfo = mock(UriInfo.class);
    objectMapper = EndpointTestObjectMapper.get();
    service = new TestScopeVersionsService(audService, audApiConfig, objectMapper);

    when(audApiConfig.version()).thenReturn("v1");
    when(uriInfo.getAbsolutePath()).thenReturn(new URI("https://aud.example/scope-versions"));
  }

  @Test
  void namedScopeWithRequirementReturnsItsVersion() throws Exception {
    Version version = versionWithScopes(scope("network", "3.4.5"));

    Response response =
        service.getScopeVersions(version, "network", uriInfo).await().indefinitely();
    ScopeVersionsResponse body = body(response);

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(body.getScopesVersion()).containsExactlyEntriesOf(Map.of("network", "3.4.5"));
    assertThat(body.getVersion()).isEqualTo("1.2.3");
    assertThat(body.getApiVersion()).isEqualTo("v1");
    assertThat(body.getUrl()).isEqualTo("https://aud.example/scope-versions");
  }

  @Test
  void namedScopeWithoutRequirementReturnsEmptyMap() throws Exception {
    Version version = versionWithScopes(scope("network", null));

    Response response =
        service.getScopeVersions(version, "network", uriInfo).await().indefinitely();

    assertThat(body(response).getScopesVersion()).isNullOrEmpty();
  }

  @Test
  void unknownNamedScopeReturnsEmptyMap() throws Exception {
    Version version = versionWithScopes(scope("network", "1.0.0"));

    Response response =
        service.getScopeVersions(version, "unknown", uriInfo).await().indefinitely();

    assertThat(body(response).getScopesVersion()).isNullOrEmpty();
  }

  @Test
  void nullScopeIteratesAllScopesAndSkipsMissingRequirements() throws Exception {
    Version version =
        versionWithScopes(
            scope("zoning", "2.0.0"), scope("network", "1.0.0"), scope("unversioned", null));

    Response response = service.getScopeVersions(version, null, uriInfo).await().indefinitely();

    assertThat(body(response).getScopesVersion())
        .containsOnly(Map.entry("network", "1.0.0"), Map.entry("zoning", "2.0.0"));
    assertThat(body(response).getScopesVersion().keySet()).containsExactly("network", "zoning");
  }

  @Test
  void stringVersionWrapperResolvesVersionAsynchronously() throws Exception {
    Version version = versionWithScopes(scope("network", "1.0.0"));
    when(audService.getVersion("current")).thenReturn(Uni.createFrom().item(version));

    Response response =
        service.getScopeVersions("current", "network", uriInfo).await().indefinitely();

    assertThat(body(response).getScopesVersion()).containsEntry("network", "1.0.0");
    verify(audService).getVersion("current");
  }

  @Test
  void unavailableScopesRepositoryFails() {
    Version version = new Version();
    version.setVersionNumber("1.2.3");

    assertThatThrownBy(
            () -> service.getScopeVersions(version, null, uriInfo).await().indefinitely())
        .isInstanceOf(ServiceUnavailableException.class)
        .hasMessage("Scopes repository is not ready for version: 1.2.3");
  }

  private ScopeVersionsResponse body(Response response) throws Exception {
    return objectMapper.readValue((byte[]) response.getEntity(), ScopeVersionsResponse.class);
  }

  private static Scope scope(String name, String requirementVersion) {
    Scope scope = new Scope();
    scope.setName(name);
    if (requirementVersion != null) {
      Requirement requirement = new Requirement(name, name, "", "", List.of(), requirementVersion);
      scope.setRequirement(requirement);
    }
    return scope;
  }

  private static Version versionWithScopes(Scope... scopes) {
    ScopesRepository repository = new ScopesRepository();
    for (Scope scope : scopes) {
      repository.put(scope.getName(), scope);
    }
    Version version = new Version();
    version.setVersionNumber("1.2.3");
    version.setScopesRepository(repository);
    return version;
  }

  private static final class TestScopeVersionsService extends ScopeVersionsService {
    private TestScopeVersionsService(
        AudService audService, AudApiConfig audApiConfig, ObjectMapper objectMapper) {
      super(audService, audApiConfig);
      setObjectMapper(objectMapper);
    }
  }
}

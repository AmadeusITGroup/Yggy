package com.amadeus.aud.services.apis.services.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem.ChangeType;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.AudVersionsService;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.internals.Requirements;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScopeVersionDiffServiceTest {

  @Mock private AudService audService;
  @Mock private AudVersionsService audVersionsService;
  @Mock private AudModelConfig audModelConfig;

  private ScopeVersionDiffService service;

  @BeforeEach
  void setUp() {
    service = new ScopeVersionDiffService(audService, audVersionsService, audModelConfig);
  }

  @Test
  @DisplayName("should throw BadRequestException when page size exceeds maximum")
  void testGetScopeVersionDiffResponseWithExceedingPageSize() {
    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getScopeVersionDiffResponse("v1.0.0", "v2.0.0", 1, 10000)
                    .await()
                    .indefinitely());
    assertThat(thrown).isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("should filter release notes within scope version range for changed scope")
  void testScopeVersionDiffFilterReleaseNotesInRange() {
    Version oldVer = mock(Version.class);
    Version newVer = mock(Version.class);
    AudRequirementsRepository oldReqRepo = mock(AudRequirementsRepository.class);
    AudRequirementsRepository newReqRepo = mock(AudRequirementsRepository.class);
    ScopesRepository oldScopesRepo = mock(ScopesRepository.class);
    ScopesRepository newScopesRepo = mock(ScopesRepository.class);
    Requirements oldReqs = mock(Requirements.class);
    Requirements newReqs = mock(Requirements.class);

    when(oldVer.getVersionNumber()).thenReturn("1.0.0");
    when(newVer.getVersionNumber()).thenReturn("2.0.0");
    when(oldVer.getAudRequirementsRepository()).thenReturn(oldReqRepo);
    when(newVer.getAudRequirementsRepository()).thenReturn(newReqRepo);
    when(oldVer.getScopesRepository()).thenReturn(oldScopesRepo);
    when(newVer.getScopesRepository()).thenReturn(newScopesRepo);
    when(oldReqRepo.isReady()).thenReturn(true);
    when(newReqRepo.isReady()).thenReturn(true);
    when(oldScopesRepo.isReady()).thenReturn(true);
    when(newScopesRepo.isReady()).thenReturn(true);
    when(oldReqRepo.getData()).thenReturn(oldReqs);
    when(newReqRepo.getData()).thenReturn(newReqs);

    Requirement reqOld = createRequirement("test-scope", "1.0.0");
    Requirement reqNew = createRequirement("test-scope", "1.0.2");
    when(oldReqs.requirements()).thenReturn(List.of(reqOld));
    when(newReqs.requirements()).thenReturn(List.of(reqNew));

    Scope scope = mock(Scope.class);
    when(newScopesRepo.getData()).thenReturn(new ConcurrentHashMap<>(Map.of("test-scope", scope)));

    ScopeReleaseNote note100 = createReleaseNote("1.0.0", "100", "Old commit");
    ScopeReleaseNote note101 = createReleaseNote("1.0.1", "101", "Commit for 1.0.1");
    ScopeReleaseNote note102 = createReleaseNote("1.0.2", "102", "Commit for 1.0.2");
    ScopeReleaseNote note103 = createReleaseNote("1.0.3", "103", "Future commit");

    when(scope.getScopeReleaseNotes()).thenReturn(Set.of(note100, note101, note102, note103));
    when(audModelConfig.defaultScopesBitbucketUrlPrefix()).thenReturn("http://bitbucket/");

    Response<List<ScopeVersionDiffItem>> response =
        service.getScopeVersionDiffResponse(oldVer, newVer, 1, 20).await().indefinitely();

    assertThat(response.data()).hasSize(1);
    ScopeVersionDiffItem item = response.data().get(0);
    assertThat(item.scope()).isEqualTo("test-scope");
    assertThat(item.changeType()).isEqualTo(ChangeType.CHANGED);
    assertThat(item.fromVersion()).isEqualTo("1.0.0");
    assertThat(item.toVersion()).isEqualTo("1.0.2");
    assertThat(item.pullRequests()).extracting("number").containsExactlyInAnyOrder("101", "102");
    assertThat(item.messages())
        .extracting("message")
        .containsExactlyInAnyOrder("Commit for 1.0.1", "Commit for 1.0.2");
  }

  private Requirement createRequirement(String scope, String version) {
    Requirement req = mock(Requirement.class);
    when(req.getScope()).thenReturn(scope);
    when(req.getVersion()).thenReturn(version);
    return req;
  }

  private ScopeReleaseNote createReleaseNote(String version, String pr, String commitMsg) {
    ScopeReleaseNote note = mock(ScopeReleaseNote.class);
    lenient().when(note.getScopeVersion()).thenReturn(version);
    lenient().when(note.getPrs()).thenReturn(List.of(pr));

    V1Commit commit = mock(V1Commit.class);
    lenient().when(commit.getMessage()).thenReturn(commitMsg);
    lenient().when(commit.getCommitid()).thenReturn("sha-" + version);
    lenient().when(note.getCommits()).thenReturn(List.of(commit));

    return note;
  }
}

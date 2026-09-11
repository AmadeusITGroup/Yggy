package com.amadeus.aud.services.apis.services.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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
import com.amadeus.aud.libraries.engine.data.services.external.storage.SingleVersion;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.internals.Requirements;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ServiceUnavailableException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
  void getScopeVersionDiffResponseWithExceedingPageSize() {
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
  void scopeVersionDiffFilterReleaseNotesInRange() {
    Version oldVer = mock(Version.class);
    Version newVer = mock(Version.class);
    AudRequirementsRepository oldReqRepo = mock(AudRequirementsRepository.class);
    AudRequirementsRepository newReqRepo = mock(AudRequirementsRepository.class);
    ScopesRepository oldScopesRepo = mock(ScopesRepository.class);
    ScopesRepository newScopesRepo = mock(ScopesRepository.class);
    Requirement reqOld = createRequirement("test-scope", "1.0.0");
    Requirement reqNew = createRequirement("test-scope", "1.0.2");
    Requirements oldReqs = new Requirements(List.of(reqOld));
    Requirements newReqs = new Requirements(List.of(reqNew));

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

    Scope scope = mock(Scope.class);
    when(newScopesRepo.getData()).thenReturn(new ConcurrentHashMap<>(Map.of("test-scope", scope)));

    ScopeReleaseNote note100 = createReleaseNote("1.0.0");
    ScopeReleaseNote note101 = createReleaseNote("1.0.1", "101", "Commit for 1.0.1");
    ScopeReleaseNote note102 = createReleaseNote("1.0.2", "102", "Commit for 1.0.2");
    ScopeReleaseNote note103 = createReleaseNote("1.0.3");

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

  @Test
  void getScopeVersionDiffItemsRejectsMissingVersionIds() {
    Throwable nullOld = catchThrowable(() -> service.getScopeVersionDiffItems(null, "2.0.0"));
    Throwable blankOld = catchThrowable(() -> service.getScopeVersionDiffItems(" ", "2.0.0"));
    Throwable nullNew = catchThrowable(() -> service.getScopeVersionDiffItems("1.0.0", null));
    Throwable blankNew = catchThrowable(() -> service.getScopeVersionDiffItems("1.0.0", "\t"));

    assertThat(List.of(nullOld, blankOld))
        .allSatisfy(
            thrown ->
                assertThat(thrown)
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("oldVersion query parameter is required"));
    assertThat(List.of(nullNew, blankNew))
        .allSatisfy(
            thrown ->
                assertThat(thrown)
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("newVersion query parameter is required"));
  }

  @Test
  void getScopeVersionDiffItemsLoadsListedAndLatestVersionsInTimestampOrder() {
    OffsetDateTime oldTimestamp = OffsetDateTime.parse("2025-01-01T00:00:00Z");
    OffsetDateTime newTimestamp = OffsetDateTime.parse("2025-02-01T00:00:00Z");
    List<SingleVersion> versions =
        List.of(
            new SingleVersion("1.0.0", oldTimestamp, 0),
            new SingleVersion("2.0.0", newTimestamp, 0),
            new SingleVersion("ignored", null, 0));
    Version oldVersion = readyVersion("1.0.0", 20, List.of(), Map.of());
    Version newVersion = readyVersion("latest", 1, List.of(), Map.of());
    Version exactLatestVersion = readyVersion("2.0.0", 30, List.of(), Map.of());
    when(audVersionsService.listAllVersions())
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(versions));
    when(audVersionsService.getLatestVersion())
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item("2.0.0"));
    when(audService.getVersionData("1.0.0"))
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(oldVersion));
    when(audService.getVersionData("latest"))
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(newVersion));
    when(audService.getVersionData("2.0.0"))
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(exactLatestVersion));

    List<ScopeVersionDiffItem> items =
        service.getScopeVersionDiffItems("1.0.0", "latest").await().indefinitely();
    List<ScopeVersionDiffItem> exactLatestItems =
        service.getScopeVersionDiffItems("1.0.0", "2.0.0").await().indefinitely();

    assertThat(items).isEmpty();
    assertThat(exactLatestItems).isEmpty();
  }

  @Test
  void getScopeVersionDiffItemsAcceptsLatestVersionIdAndRejectsMissingVersions() {
    List<SingleVersion> versions =
        List.of(new SingleVersion("1.0.0", null, 0), new SingleVersion("2.0.0", null, 0));
    when(audVersionsService.listAllVersions())
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(versions));
    when(audVersionsService.getLatestVersion())
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item("2.0.0"));

    Throwable missingOld =
        catchThrowable(
            () -> service.getScopeVersionDiffItems("missing", "latest").await().indefinitely());
    Throwable missingNew =
        catchThrowable(
            () -> service.getScopeVersionDiffItems("latest", "missing").await().indefinitely());

    assertThat(missingOld)
        .isInstanceOf(BadRequestException.class)
        .hasMessage("oldVersion must reference an existing version");
    assertThat(missingNew)
        .isInstanceOf(BadRequestException.class)
        .hasMessage("newVersion must reference an existing version");
  }

  @Test
  void getScopeVersionDiffItemsRejectsReverseTimestampAndNumericOrders() {
    OffsetDateTime earlier = OffsetDateTime.parse("2025-01-01T00:00:00Z");
    OffsetDateTime later = OffsetDateTime.parse("2025-02-01T00:00:00Z");
    List<SingleVersion> timestamped =
        List.of(new SingleVersion("old", later, 0), new SingleVersion("new", earlier, 0));
    Version oldTimestampVersion = mock(Version.class);
    when(oldTimestampVersion.getVersionNumber()).thenReturn("old");
    Version newTimestampVersion = mock(Version.class);
    when(newTimestampVersion.getVersionNumber()).thenReturn("new");
    stubVersionLoading(timestamped, "new", oldTimestampVersion, newTimestampVersion, "old", "new");

    Throwable timestampOrder =
        catchThrowable(() -> service.getScopeVersionDiffItems("old", "new").await().indefinitely());

    List<SingleVersion> withoutTimestamps =
        List.of(new SingleVersion("old-long", null, 0), new SingleVersion("new-long", null, 0));
    Version oldLongVersion = mock(Version.class);
    when(oldLongVersion.getVersionNumber()).thenReturn("old-long");
    when(oldLongVersion.getVersionLong()).thenReturn(20L);
    Version newLongVersion = mock(Version.class);
    when(newLongVersion.getVersionNumber()).thenReturn("new-long");
    when(newLongVersion.getVersionLong()).thenReturn(10L);
    stubVersionLoading(
        withoutTimestamps, "new-long", oldLongVersion, newLongVersion, "old-long", "new-long");

    Throwable numericOrder =
        catchThrowable(
            () -> service.getScopeVersionDiffItems("old-long", "new-long").await().indefinitely());

    assertThat(timestampOrder)
        .isInstanceOf(BadRequestException.class)
        .hasMessage("newVersion must be greater than or equal to oldVersion");
    assertThat(numericOrder)
        .isInstanceOf(BadRequestException.class)
        .hasMessage("newVersion must be greater than or equal to oldVersion");
  }

  @Test
  void getScopeVersionDiffResponseReportsAddedRemovedChangedAndSkipsUnchangedScopes() {
    Requirement removed = createRequirement("removed", "1.0.0");
    Requirement changedOld = createRequirement("changed", "1.0.0");
    Requirement unchangedOld = createRequirement("unchanged", "1.0.0");
    Requirement added = createRequirement("added", "1.0.0");
    Requirement changedNew = createRequirement("changed", "1.1.0");
    Requirement unchangedNew = createRequirement("unchanged", "1.0.0");
    Scope removedScope = mock(Scope.class);
    Scope addedScope = mock(Scope.class);
    when(removedScope.getScopeReleaseNotes()).thenReturn(null);
    when(addedScope.getScopeReleaseNotes()).thenReturn(Set.of());
    Version oldVersion =
        readyVersion(
            "old", 1, List.of(removed, changedOld, unchangedOld), Map.of("removed", removedScope));
    Version newVersion =
        readyVersion(
            "new", 2, List.of(added, changedNew, unchangedNew), Map.of("added", addedScope));

    Response<List<ScopeVersionDiffItem>> response =
        service
            .getScopeVersionDiffResponse(oldVersion, newVersion, null, null)
            .await()
            .indefinitely();

    assertThat(response.data())
        .extracting(ScopeVersionDiffItem::changeType)
        .containsExactlyInAnyOrder(ChangeType.ADDED, ChangeType.REMOVED, ChangeType.CHANGED);
    assertThat(response.metadata().pagination().count()).isEqualTo(3);
  }

  @Test
  void getScopeVersionDiffResponsePaginatesAndValidatesBounds() {
    Version oldVersion = readyVersion("old", 1, List.of(), Map.of());
    List<Requirement> added = new ArrayList<>();
    for (int index = 0; index < 3; index++) {
      added.add(createRequirement("scope-" + index, "1.0.0"));
    }
    Version newVersion = readyVersion("new", 2, added, Map.of());

    Response<List<ScopeVersionDiffItem>> page =
        service.getScopeVersionDiffResponse(oldVersion, newVersion, 2, 2).await().indefinitely();
    Throwable zeroSize =
        catchThrowable(() -> service.getScopeVersionDiffResponse(oldVersion, newVersion, 1, 0));
    Throwable zeroPage =
        catchThrowable(() -> service.getScopeVersionDiffResponse(oldVersion, newVersion, 0, 1));
    Throwable overflow =
        catchThrowable(
            () ->
                service
                    .getScopeVersionDiffResponse(oldVersion, newVersion, 3, 2)
                    .await()
                    .indefinitely());

    assertThat(page.data()).hasSize(1);
    assertThat(page.metadata().pagination().pageNum()).isEqualTo(2);
    assertThat(zeroSize).isInstanceOf(BadRequestException.class);
    assertThat(zeroPage).isInstanceOf(BadRequestException.class);
    assertThat(overflow).isInstanceOf(BadRequestException.class).hasMessageContaining("last page");
  }

  @Test
  void getScopeVersionDiffItemsRejectsMissingRepositories() {
    Version missingRequirements = mock(Version.class);
    when(missingRequirements.getVersionNumber()).thenReturn("missing");
    Version valid = mock(Version.class);

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getScopeVersionDiffItems(missingRequirements, valid)
                    .await()
                    .indefinitely());

    assertThat(thrown)
        .isInstanceOf(ServiceUnavailableException.class)
        .hasMessageContaining("Requirements repository is not ready");
  }

  @Test
  void addedScopeIncludesUnboundedReleaseNotesWithNullMessageAndOptionalDate() {
    Requirement added = createRequirement("added", null);
    Scope scope = mock(Scope.class);
    ScopeReleaseNote undated = mock(ScopeReleaseNote.class);
    ScopeReleaseNote dated = mock(ScopeReleaseNote.class);
    ScopeReleaseNote empty = mock(ScopeReleaseNote.class);
    when(undated.getPrs()).thenReturn(List.of("10"));
    V1Commit undatedCommit = mock(V1Commit.class);
    when(undatedCommit.getMessage()).thenReturn(null);
    when(undatedCommit.getCommitid()).thenReturn("sha-1.0.0");
    when(undated.getCommits()).thenReturn(List.of(undatedCommit));
    when(dated.getPrs()).thenReturn(List.of("20"));
    V1Commit datedCommit = mock(V1Commit.class);
    when(datedCommit.getMessage()).thenReturn("message");
    when(datedCommit.getCommitid()).thenReturn("sha-2.0.0");
    when(dated.getCommits()).thenReturn(List.of(datedCommit));
    when(undated.getVersionDateTime()).thenReturn(null);
    when(dated.getVersionDateTime()).thenReturn("2025-03-01T00:00:00Z");
    when(empty.getPrs()).thenReturn(List.of());
    when(empty.getCommits()).thenReturn(List.of());
    when(scope.getScopeReleaseNotes()).thenReturn(Set.of(undated, dated, empty));
    when(audModelConfig.defaultScopesBitbucketUrlPrefix()).thenReturn("https://example/");
    Version oldVersion = readyVersion("old", 1, List.of(), Map.of());
    Version newVersion = readyVersion("new", 2, List.of(added), Map.of("added", scope));

    List<ScopeVersionDiffItem> items =
        service.getScopeVersionDiffItems(oldVersion, newVersion).await().indefinitely();

    assertThat(items)
        .singleElement()
        .satisfies(
            (ScopeVersionDiffItem item) -> {
              assertThat(item.pullRequests()).hasSize(2);
              assertThat(item.messages())
                  .extracting(ScopeVersionDiffItem.Message::message)
                  .containsExactlyInAnyOrder("", "message");
              assertThat(item.messages())
                  .extracting(ScopeVersionDiffItem.Message::timestamp)
                  .contains(null, OffsetDateTime.parse("2025-03-01T00:00:00Z"));
            });
  }

  private Requirement createRequirement(String scope, String version) {
    Requirement req = mock(Requirement.class);
    when(req.getScope()).thenReturn(scope);
    when(req.getVersion()).thenReturn(version);
    return req;
  }

  private ScopeReleaseNote createReleaseNote(String version, String pr, String commitMsg) {
    ScopeReleaseNote note = createReleaseNote(version);
    when(note.getPrs()).thenReturn(List.of(pr));

    V1Commit commit = mock(V1Commit.class);
    when(commit.getMessage()).thenReturn(commitMsg);
    when(commit.getCommitid()).thenReturn("sha-" + version);
    when(note.getCommits()).thenReturn(List.of(commit));

    return note;
  }

  private ScopeReleaseNote createReleaseNote(String version) {
    ScopeReleaseNote note = mock(ScopeReleaseNote.class);
    when(note.getScopeVersion()).thenReturn(version);
    return note;
  }

  private Version readyVersion(
      String versionNumber,
      long versionLong,
      List<Requirement> requirements,
      Map<String, Scope> scopes) {
    Version version = mock(Version.class);
    AudRequirementsRepository requirementsRepository = mock(AudRequirementsRepository.class);
    ScopesRepository scopesRepository = mock(ScopesRepository.class);
    when(version.getVersionNumber()).thenReturn(versionNumber);
    when(version.getAudRequirementsRepository()).thenReturn(requirementsRepository);
    when(version.getScopesRepository()).thenReturn(scopesRepository);
    when(requirementsRepository.isReady()).thenReturn(true);
    when(scopesRepository.isReady()).thenReturn(true);
    when(requirementsRepository.getData()).thenReturn(new Requirements(requirements));
    if (!scopes.isEmpty() || !requirements.isEmpty()) {
      when(scopesRepository.getData()).thenReturn(new ConcurrentHashMap<>(scopes));
    }
    return version;
  }

  private void stubVersionLoading(
      List<SingleVersion> versions,
      String latestVersion,
      Version oldVersion,
      Version newVersion,
      String oldId,
      String newId) {
    when(audVersionsService.listAllVersions())
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(versions));
    when(audVersionsService.getLatestVersion())
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(latestVersion));
    when(audService.getVersionData(oldId))
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(oldVersion));
    when(audService.getVersionData(newId))
        .thenReturn(io.smallrye.mutiny.Uni.createFrom().item(newVersion));
  }
}

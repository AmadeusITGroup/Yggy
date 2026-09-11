package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudScopesConfig;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.JsonService;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.YamlService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.internals.Requirements;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("mutiny-infrastructure")
class ScopeBlobServiceTest {
  @TempDir Path temporaryDirectory;

  private AudScopesConfig scopesConfig;
  private AudDataConfig dataConfig;
  private AudRequirementsStorageAccountService requirementsService;
  private ScopeBlobService sut;

  @BeforeAll
  void setUpService() {
    scopesConfig = ExternalServiceTestFixture.scopesConfig(true, false);
    dataConfig = ExternalServiceTestFixture.dataConfig(Path.of("."));
    requirementsService = mock(AudRequirementsStorageAccountService.class);
    sut =
        new ScopeBlobService(
            scopesConfig,
            dataConfig,
            ExternalServiceTestFixture.modelConfig(),
            new YamlService(),
            new JsonService(),
            requirementsService);
    sut.postConstruct();
  }

  @BeforeEach
  void resetConfiguration() {
    reset(requirementsService);
    when(scopesConfig.enabled()).thenReturn(true);
    when(scopesConfig.associateScopeWithRequirements()).thenReturn(false);
  }

  @AfterAll
  void closeService() {
    sut.preDestroy();
  }

  @Nested
  class ProcessResponses {
    @Test
    void yamlScopesAreConvertedGroupedAndHashed() throws IOException {
      ScopesRepository repository =
          sut.processResponses(
              "1.2.3", new String[] {ExternalServiceTestFixture.SCOPE_YAML}, false);

      assertThat(repository.getDataVersion()).isEqualTo("1.2.3");
      assertThat(repository.getDataMD5Hash()).hasSize(32);
      assertThat(repository.getDataSHA256Hash()).hasSize(64);
      assertThat(repository.getData()).hasSize(2);
      Scope governance = findScope(repository, "Governance");
      Scope application = findScope(repository, "Application");
      assertThat(governance.getOwners()).containsExactly("platform");
      assertThat(application.getOwners()).containsExactly("application-owner");
      assertThat(application.getScope()).isSameAs(governance);
    }

    @Test
    void jsonScopeIsConvertedUsingJsonParser() throws IOException {
      ScopesRepository repository =
          sut.processResponses("2.0.0", new String[] {ExternalServiceTestFixture.SCOPE_JSON}, true);

      assertThat(repository.getData()).hasSize(1);
      Scope scope = findScope(repository, "JsonScope");
      assertThat(scope.getOwners()).containsExactly("json-owner");
    }

    @Test
    void duplicateScopeNamesCollapseToOnePublicScope() throws IOException {
      ScopesRepository repository =
          sut.processResponses(
              "1.2.3", new String[] {ExternalServiceTestFixture.DUPLICATE_SCOPE_YAML}, false);

      assertThat(repository.getData()).hasSize(1);
      assertThat(findScope(repository, "Application").getOwners())
          .isIn(java.util.Set.of("first"), java.util.Set.of("second"));
    }

    @Test
    void malformedScopeContentProducesEmptyRepositoryAndRetainsHash() throws IOException {
      ScopesRepository repository =
          sut.processResponses("1.0.0", new String[] {"---\nkind: ["}, false);

      assertThat(repository.getData()).isEmpty();
      assertThat(repository.getDataMD5Hash()).hasSize(32);
      assertThat(repository.getDataSHA256Hash()).hasSize(64);
    }

    @Test
    void emptyResponseArrayProducesEmptyUnhashedRepository() throws IOException {
      ScopesRepository repository = sut.processResponses("empty", new String[0], false);

      assertThat(repository.getDataVersion()).isEqualTo("empty");
      assertThat(repository.getData()).isEmpty();
      assertThat(repository.getDataMD5Hash()).isNull();
      assertThat(repository.getDataSHA256Hash()).isNull();
    }

    @Test
    void emptyBlobProducesEmptyHashedRepository() throws IOException {
      ScopesRepository repository = sut.processResponses("empty-blob", new String[] {""}, false);

      assertThat(repository.getData()).isEmpty();
      assertThat(repository.getDataMD5Hash()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
    }

    @Test
    void nullBlobIsRejectedByResponseSet() {
      assertThatThrownBy(() -> sut.processResponses("null", new String[] {null}, false))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  class Load {
    @Test
    void disabledScopeLoadingReturnsEmptyRepository() {
      when(scopesConfig.enabled()).thenReturn(false);
      BlobService blobService = mock(BlobService.class);

      ScopesRepository repository =
          sut.load("1.2.3", temporaryDirectory.toString(), blobService, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData()).isEmpty();
      verify(requirementsService, never())
          .load("1.2.3", "requirements-container", blobService, false);
    }

    @Test
    void yamlLoadReadsMatchingLocalFilesAndIgnoresOtherEntries() throws IOException {
      Path scopeDirectory = Files.createDirectory(temporaryDirectory.resolve("yaml-scopes"));
      Files.writeString(
          scopeDirectory.resolve("scopes.yml"), ExternalServiceTestFixture.SCOPE_YAML);
      Files.writeString(
          scopeDirectory.resolve("ignored.json"), ExternalServiceTestFixture.SCOPE_JSON);
      Files.writeString(scopeDirectory.resolve("ignored.txt"), "ignored");
      Files.createDirectory(scopeDirectory.resolve("nested.yml"));

      ScopesRepository repository =
          sut.load("1.2.3", scopeDirectory.toString(), null, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData()).hasSize(2);
      assertThat(findScope(repository, "Application")).isNotNull();
    }

    @Test
    void jsonLoadReadsOnlyJsonFiles() throws IOException {
      Path scopeDirectory = Files.createDirectory(temporaryDirectory.resolve("json-scopes"));
      Files.writeString(
          scopeDirectory.resolve("scopes.json"), ExternalServiceTestFixture.SCOPE_JSON);
      Files.writeString(
          scopeDirectory.resolve("ignored.yaml"), ExternalServiceTestFixture.SCOPE_YAML);

      ScopesRepository repository =
          sut.load("2.0.0", scopeDirectory.toString(), null, true)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData()).hasSize(1);
      assertThat(findScope(repository, "JsonScope")).isNotNull();
    }

    @Test
    void missingScopeDirectoryFailureIsPropagated() {
      Path missingDirectory = temporaryDirectory.resolve("missing");

      assertThatThrownBy(
              () ->
                  sut.load("1.2.3", missingDirectory.toString(), null, false)
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(java.nio.file.NoSuchFileException.class);
    }

    @Test
    void suppliedBlobServiceEnablesRequirementAssociation() throws IOException {
      when(scopesConfig.associateScopeWithRequirements()).thenReturn(true);
      Path scopeDirectory = Files.createDirectory(temporaryDirectory.resolve("associated"));
      Files.writeString(
          scopeDirectory.resolve("scopes.yml"), ExternalServiceTestFixture.SCOPE_YAML);
      BlobService blobService = mock(BlobService.class);
      Requirement requirement =
          new Requirement(
              "Application",
              "Application",
              "requirement",
              "projects/AUD/repos/application/raw/",
              List.of(),
              "1.2.3");
      AudRequirementsRepository requirementsRepository = new AudRequirementsRepository();
      requirementsRepository.setData(new Requirements(List.of(requirement)));
      when(requirementsService.load("1.2.3", "requirements-container", blobService, false))
          .thenReturn(Uni.createFrom().item(requirementsRepository));

      ScopesRepository repository =
          sut.load("1.2.3", scopeDirectory.toString(), blobService, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(findScope(repository, "Application").getRequirement()).isSameAs(requirement);
      verify(requirementsService).load("1.2.3", "requirements-container", blobService, false);
    }

    @Test
    void nullBlobServiceSkipsOptionalRequirementAssociation() throws IOException {
      when(scopesConfig.associateScopeWithRequirements()).thenReturn(true);
      Path scopeDirectory = Files.createDirectory(temporaryDirectory.resolve("no-blob"));
      Files.writeString(
          scopeDirectory.resolve("scopes.yml"), ExternalServiceTestFixture.SCOPE_YAML);

      ScopesRepository repository =
          sut.load("1.2.3", scopeDirectory.toString(), null, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(findScope(repository, "Application").getRequirement()).isNull();
      verify(requirementsService, never()).load("1.2.3", "requirements-container", null, false);
    }

    @Test
    void missingRequirementDataLeavesScopesUnassociated() throws IOException {
      when(scopesConfig.associateScopeWithRequirements()).thenReturn(true);
      Path scopeDirectory = Files.createDirectory(temporaryDirectory.resolve("empty-requirements"));
      Files.writeString(
          scopeDirectory.resolve("scopes.yml"), ExternalServiceTestFixture.SCOPE_YAML);
      BlobService blobService = mock(BlobService.class);
      AudRequirementsRepository requirementsRepository = new AudRequirementsRepository();
      when(requirementsService.load("1.2.3", "requirements-container", blobService, false))
          .thenReturn(Uni.createFrom().item(requirementsRepository));

      ScopesRepository repository =
          sut.load("1.2.3", scopeDirectory.toString(), blobService, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(findScope(repository, "Application").getRequirement()).isNull();
      assertThat(requirementsRepository.getData().requirements()).isEmpty();
    }

    @Test
    void unmatchedRequirementLeavesLoadedScopesUnchanged() throws IOException {
      when(scopesConfig.associateScopeWithRequirements()).thenReturn(true);
      Path scopeDirectory = Files.createDirectory(temporaryDirectory.resolve("unmatched"));
      Files.writeString(
          scopeDirectory.resolve("scopes.yml"), ExternalServiceTestFixture.SCOPE_YAML);
      BlobService blobService = mock(BlobService.class);
      Requirement requirement =
          new Requirement("Missing", "Missing", null, null, List.of(), "1.0.0");
      AudRequirementsRepository requirementsRepository = new AudRequirementsRepository();
      requirementsRepository.setData(new Requirements(List.of(requirement)));
      when(requirementsService.load("1.2.3", "requirements-container", blobService, false))
          .thenReturn(Uni.createFrom().item(requirementsRepository));

      ScopesRepository repository =
          sut.load("1.2.3", scopeDirectory.toString(), blobService, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData()).hasSize(2);
      assertThat(findScope(repository, "Application").getRequirement()).isNull();
    }

    @Test
    void requirementLoadFailureIsPropagated() throws IOException {
      when(scopesConfig.associateScopeWithRequirements()).thenReturn(true);
      Path scopeDirectory =
          Files.createDirectory(temporaryDirectory.resolve("requirement-failure"));
      Files.writeString(
          scopeDirectory.resolve("scopes.yml"), ExternalServiceTestFixture.SCOPE_YAML);
      BlobService blobService = mock(BlobService.class);
      when(requirementsService.load("1.2.3", "requirements-container", blobService, false))
          .thenReturn(Uni.createFrom().failure(new IOException("requirements failed")));

      assertThatThrownBy(
              () ->
                  sut.load("1.2.3", scopeDirectory.toString(), blobService, false)
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("requirements failed");
    }
  }

  private static Scope findScope(ScopesRepository repository, String name) {
    for (Scope scope : repository.getData().values()) {
      if (name.equals(scope.getName())) {
        return scope;
      }
    }
    throw new AssertionError("Scope not found: " + name);
  }
}

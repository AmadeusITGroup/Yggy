package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;

import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileService;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;

@ResourceLock("mutiny-infrastructure")
class AudRequirementsLocalFileServiceTest {
  private LocalFileService localFileService;
  private AudRequirementsLocalFileService sut;

  @BeforeEach
  void setUp() {
    localFileService = mock(LocalFileService.class);
    sut =
        new AudRequirementsLocalFileService(
            localFileService, ExternalServiceTestFixture.modelConfig());
  }

  @Nested
  class InitObjects {
    @Test
    void yamlPreferenceRequestsYamlRequirements() {
      AudRequirementsRepository expected = new AudRequirementsRepository();
      doReturn(Uni.createFrom().item(expected))
          .when(localFileService)
          .initObjects(eq("1.2.3"), any(String[].class), same(sut), eq(false));
      ArgumentCaptor<String[]> namesCaptor = ArgumentCaptor.forClass(String[].class);

      AudRequirementsRepository result =
          sut.initObjects("1.2.3", false).await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(result).isSameAs(expected);
      verify(localFileService)
          .initObjects(eq("1.2.3"), namesCaptor.capture(), same(sut), eq(false));
      assertThat(namesCaptor.getValue()).containsExactly("requirements.yml");
    }

    @Test
    void jsonPreferenceRequestsJsonRequirements() {
      AudRequirementsRepository expected = new AudRequirementsRepository();
      doReturn(Uni.createFrom().item(expected))
          .when(localFileService)
          .initObjects(eq("2.0.0"), any(String[].class), same(sut), eq(true));
      ArgumentCaptor<String[]> namesCaptor = ArgumentCaptor.forClass(String[].class);

      AudRequirementsRepository result =
          sut.initObjects("2.0.0", true).await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(result).isSameAs(expected);
      verify(localFileService).initObjects(eq("2.0.0"), namesCaptor.capture(), same(sut), eq(true));
      assertThat(namesCaptor.getValue()).containsExactly("requirements.json");
    }

    @Test
    void localFailureIsPropagated() {
      doReturn(Uni.createFrom().failure(new IOException("requirements missing")))
          .when(localFileService)
          .initObjects(eq("1.2.3"), any(String[].class), same(sut), eq(false));

      assertThatThrownBy(
              () ->
                  sut.initObjects("1.2.3", false)
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("requirements missing");
    }
  }

  @Nested
  class ProcessResponses {
    @Test
    void yamlContentProducesRepositoryMetadataAndRequirement() throws IOException {
      AudRequirementsRepository repository =
          sut.processResponses(
              "1.2.3", new String[] {ExternalServiceTestFixture.REQUIREMENTS_YAML}, false);

      assertThat(repository.getDataVersion()).isEqualTo("1.2.3");
      assertThat(repository.getDataMD5Hash()).hasSize(32);
      assertThat(repository.getDataSHA256Hash()).hasSize(64);
      assertThat(repository.getData().requirements())
          .singleElement()
          .satisfies(
              (Requirement requirement) -> {
                assertThat(requirement.getName()).isEqualTo("Application");
                assertThat(requirement.getScope()).isEqualTo("Application");
                assertThat(requirement.getRepo()).isEqualTo("projects/AUD/repos/application/raw/");
                assertThat(requirement.getFiles())
                    .extracting(java.io.File::getPath)
                    .containsExactly("Application.yml");
                assertThat(requirement.getVersion()).isEqualTo("1.2.3");
              });
    }

    @Test
    void jsonContentProducesRequirement() throws IOException {
      AudRequirementsRepository repository =
          sut.processResponses(
              "2.0.0", new String[] {ExternalServiceTestFixture.REQUIREMENTS_JSON}, true);

      assertThat(repository.getData().requirements())
          .singleElement()
          .satisfies(
              (Requirement requirement) -> {
                assertThat(requirement.getName()).isEqualTo("JsonScope");
                assertThat(requirement.getVersion()).isEqualTo("2.0.0");
              });
    }

    @Test
    void emptyResponseArrayLeavesDataAndHashesUnset() throws IOException {
      AudRequirementsRepository repository = sut.processResponses("empty", new String[0], false);

      assertThat(repository.getDataVersion()).isEqualTo("empty");
      assertThat(repository.getData()).isNull();
      assertThat(repository.getDataMD5Hash()).isNull();
      assertThat(repository.getDataSHA256Hash()).isNull();
    }

    @Test
    void nullResponseIsHashedButNotParsed() throws IOException {
      AudRequirementsRepository repository =
          sut.processResponses("null", new String[] {null}, false);

      assertThat(repository.getData()).isNull();
      assertThat(repository.getDataMD5Hash()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
      assertThat(repository.getDataSHA256Hash())
          .isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    void malformedYamlIsPropagated() {
      assertThatThrownBy(
              () -> sut.processResponses("1.0.0", new String[] {"requirements: ["}, false))
          .isInstanceOf(IOException.class);
    }

    @Test
    void malformedJsonIsPropagated() {
      assertThatThrownBy(
              () -> sut.processResponses("1.0.0", new String[] {"{\"requirements\": ["}, true))
          .isInstanceOf(IOException.class);
    }
  }
}

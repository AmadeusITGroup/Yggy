package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;

import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileService;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;

@ResourceLock("mutiny-infrastructure")
class AudDataLocalFileServiceTest {
  private LocalFileService localFileService;
  private AudDataLocalFileService sut;

  @BeforeEach
  void setUp() {
    localFileService = mock(LocalFileService.class);
    sut = new AudDataLocalFileService(localFileService, ExternalServiceTestFixture.modelConfig());
  }

  @Nested
  class InitObjects {
    @Test
    void yamlPreferenceRequestsThreeYamlFiles() {
      AudDataRepository expected = new AudDataRepository();
      doReturn(Uni.createFrom().item(expected))
          .when(localFileService)
          .initObjects(eq("1.2.3"), any(String[].class), same(sut), eq(false));
      ArgumentCaptor<String[]> namesCaptor = ArgumentCaptor.forClass(String[].class);

      AudDataRepository result =
          sut.initObjects("1.2.3", false).await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(result).isSameAs(expected);
      verify(localFileService)
          .initObjects(eq("1.2.3"), namesCaptor.capture(), same(sut), eq(false));
      assertThat(namesCaptor.getValue()).containsExactly("api.yml", "release.yml", "metadata.yml");
    }

    @Test
    void jsonPreferenceRequestsThreeJsonFiles() {
      AudDataRepository expected = new AudDataRepository();
      doReturn(Uni.createFrom().item(expected))
          .when(localFileService)
          .initObjects(eq("2.0.0"), any(String[].class), same(sut), eq(true));
      ArgumentCaptor<String[]> namesCaptor = ArgumentCaptor.forClass(String[].class);

      AudDataRepository result =
          sut.initObjects("2.0.0", true).await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(result).isSameAs(expected);
      verify(localFileService).initObjects(eq("2.0.0"), namesCaptor.capture(), same(sut), eq(true));
      assertThat(namesCaptor.getValue())
          .containsExactly("api.json", "release.json", "metadata.json");
    }

    @Test
    void localFileFailureIsPropagated() {
      doReturn(Uni.createFrom().failure(new IOException("local load failed")))
          .when(localFileService)
          .initObjects(eq("1.2.3"), any(String[].class), same(sut), eq(false));

      assertThatThrownBy(
              () ->
                  sut.initObjects("1.2.3", false)
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("local load failed");
    }
  }

  @Nested
  class ProcessResponses {
    @Test
    void yamlFragmentsAreGroupedAndNullOrEmptyFragmentsAreIgnored() throws IOException {
      String[] fragments = {
        ExternalServiceTestFixture.CORE_MAPPING_YAML,
        null,
        ExternalServiceTestFixture.CORE_RELEASE_YAML,
        ""
      };

      AudDataRepository repository = sut.processResponses("1.2.3", fragments, false);

      assertThat(repository.getDataVersion()).isEqualTo("1.2.3");
      ConcurrentMap<Class<? extends ModelElement>, List<? extends ModelElement>> data =
          repository.getData();
      assertThat(data.get(V1ScopeVersionMapping.class)).hasSize(1);
      assertThat(data.get(V1ScopeReleaseNote.class)).hasSize(1);
      assertThat(data.values()).allSatisfy(elements -> assertThat(elements).isNotNull());
    }

    @Test
    void jsonFragmentsAreGroupedByModelClass() throws IOException {
      String[] fragments = {
        ExternalServiceTestFixture.CORE_MAPPING_JSON, ExternalServiceTestFixture.CORE_RELEASE_JSON
      };

      AudDataRepository repository = sut.processResponses("2.0.0", fragments, true);

      assertThat(repository.getDataVersion()).isEqualTo("2.0.0");
      assertThat(repository.getData().get(V1ScopeVersionMapping.class)).hasSize(1);
      assertThat(repository.getData().get(V1ScopeReleaseNote.class)).hasSize(1);
    }

    @Test
    void jsonPreferenceFallsBackToYamlForYamlContent() throws IOException {
      AudDataRepository repository =
          sut.processResponses(
              "1.2.3", new String[] {ExternalServiceTestFixture.CORE_MAPPING_YAML}, true);

      assertThat(repository.getData().get(V1ScopeVersionMapping.class)).hasSize(1);
    }

    @Test
    void malformedYamlIsPropagated() {
      String malformed = "---\nkind: [";

      assertThatThrownBy(() -> sut.processResponses("1.0.0", new String[] {malformed}, false))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("while parsing a flow node");
    }

    @Test
    void noResponsesCreatesRepositoryWithKnownEmptyGroups() throws IOException {
      AudDataRepository repository = sut.processResponses("empty", new String[0], false);

      assertThat(repository.getDataVersion()).isEqualTo("empty");
      assertThat(repository.getData()).isNotEmpty();
      assertThat(repository.getData().values())
          .allSatisfy(elements -> assertThat(elements).isEmpty());
    }
  }
}

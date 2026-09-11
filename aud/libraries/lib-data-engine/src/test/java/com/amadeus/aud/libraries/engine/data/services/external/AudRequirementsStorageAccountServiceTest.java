package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.configs.AudRequirementsConfig;
import com.amadeus.aud.libraries.engine.data.configs.DumpConfig;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.BlockBlobItem;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

@ResourceLock("mutiny-infrastructure")
class AudRequirementsStorageAccountServiceTest {
  @TempDir Path temporaryDirectory;

  private AudRequirementsConfig requirementsConfig;
  private BlobServiceAsyncClient serviceClient;
  private BlobContainerAsyncClient containerClient;
  private BlobAsyncClient blobClient;
  private AudRequirementsStorageAccountService sut;

  @BeforeEach
  void setUp() {
    requirementsConfig = mock(AudRequirementsConfig.class);
    when(requirementsConfig.dump()).thenReturn(Optional.empty());
    serviceClient = mock(BlobServiceAsyncClient.class);
    containerClient = mock(BlobContainerAsyncClient.class);
    blobClient = mock(BlobAsyncClient.class);
    when(serviceClient.getBlobContainerAsyncClient("requirements-container"))
        .thenReturn(containerClient);
    when(containerClient.getBlobAsyncClient(anyString())).thenReturn(blobClient);
    sut =
        new AudRequirementsStorageAccountService(
            ExternalServiceTestFixture.dataConfig(temporaryDirectory),
            ExternalServiceTestFixture.modelConfig(),
            requirementsConfig,
            serviceClient);
  }

  @Nested
  class Load {
    @Test
    void convenienceOverloadLoadsYamlWithoutDumping() {
      BlobService blobService = mock(BlobService.class);
      when(blobService.getBlobUni("requirements", "1.2.3/requirements.yml", false))
          .thenReturn(Uni.createFrom().item(ExternalServiceTestFixture.REQUIREMENTS_YAML));

      AudRequirementsRepository repository =
          sut.load("1.2.3", "requirements", blobService)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getDataVersion()).isEqualTo("1.2.3");
      assertThat(repository.getData().requirements())
          .singleElement()
          .satisfies(
              (Requirement requirement) ->
                  assertThat(requirement.getName()).isEqualTo("Application"));
      assertThat(repository.getDataMD5Hash()).hasSize(32);
      verify(blobService).getBlobUni("requirements", "1.2.3/requirements.yml", false);
    }

    @Test
    void enabledDumpWritesBothJsonAndYamlResponses() throws IOException {
      DumpConfig dumpConfig = mock(DumpConfig.class);
      when(requirementsConfig.dump()).thenReturn(Optional.of(dumpConfig));
      when(dumpConfig.enabled()).thenReturn(true);
      when(dumpConfig.basePath()).thenReturn(Optional.of(temporaryDirectory.toString()));
      BlobService blobService = mock(BlobService.class);
      when(blobService.getBlobUni("requirements", "2.0.0/requirements.json.gz", true))
          .thenReturn(Uni.createFrom().item(ExternalServiceTestFixture.REQUIREMENTS_JSON));
      when(blobService.getBlobUni("requirements", "1.2.3/requirements.yml", false))
          .thenReturn(Uni.createFrom().item(ExternalServiceTestFixture.REQUIREMENTS_YAML));

      AudRequirementsRepository jsonRepository =
          sut.load("2.0.0", "requirements", blobService, true)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);
      AudRequirementsRepository yamlRepository =
          sut.load("1.2.3", "requirements", blobService, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(jsonRepository.getData().requirements()).hasSize(1);
      assertThat(yamlRepository.getData().requirements()).hasSize(1);
      assertThat(
              Files.readString(temporaryDirectory.resolve("2.0.0").resolve("requirements.json.gz")))
          .isEqualTo(ExternalServiceTestFixture.REQUIREMENTS_JSON);
      assertThat(Files.readString(temporaryDirectory.resolve("1.2.3").resolve("requirements.yml")))
          .isEqualTo(ExternalServiceTestFixture.REQUIREMENTS_YAML);
    }

    @Test
    void disabledDumpAcceptsMissingBasePath() {
      DumpConfig dumpConfig = mock(DumpConfig.class);
      when(requirementsConfig.dump()).thenReturn(Optional.of(dumpConfig));
      when(dumpConfig.enabled()).thenReturn(false);
      when(dumpConfig.basePath()).thenReturn(Optional.empty());
      BlobService blobService = mock(BlobService.class);
      when(blobService.getBlobUni("requirements", "1.2.3/requirements.yml", false))
          .thenReturn(Uni.createFrom().item(ExternalServiceTestFixture.REQUIREMENTS_YAML));

      AudRequirementsRepository repository =
          sut.load("1.2.3", "requirements", blobService, false)
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData().requirements()).hasSize(1);
    }

    @Test
    void blobFailureIsPropagated() {
      BlobService blobService = mock(BlobService.class);
      when(blobService.getBlobUni("requirements", "1.2.3/requirements.yml", false))
          .thenReturn(Uni.createFrom().failure(new IOException("requirements unavailable")));

      assertThatThrownBy(
              () ->
                  sut.load("1.2.3", "requirements", blobService, false)
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(java.util.concurrent.CompletionException.class)
          .hasRootCauseInstanceOf(IOException.class)
          .hasRootCauseMessage("requirements unavailable");
    }

    @Test
    void initObjectsUsesConfiguredContainer() {
      when(blobClient.downloadContent())
          .thenReturn(
              Mono.just(BinaryData.fromString(ExternalServiceTestFixture.REQUIREMENTS_YAML)));

      AudRequirementsRepository repository =
          sut.initObjects("1.2.3", false).await().atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(repository.getData().requirements()).hasSize(1);
      verify(containerClient).getBlobAsyncClient("1.2.3/requirements.yml");
    }
  }

  @Nested
  class ProcessResponses {
    @Test
    void yamlAndJsonFormatsPopulateRealRequirements() throws IOException {
      AudRequirementsRepository yaml =
          sut.processResponses(
              "1.2.3", new String[] {ExternalServiceTestFixture.REQUIREMENTS_YAML}, false);
      AudRequirementsRepository json =
          sut.processResponses(
              "2.0.0", new String[] {ExternalServiceTestFixture.REQUIREMENTS_JSON}, true);

      assertThat(yaml.getData().requirements())
          .singleElement()
          .satisfies(
              (Requirement requirement) ->
                  assertThat(requirement.getRepo())
                      .isEqualTo("projects/AUD/repos/application/raw/"));
      assertThat(json.getData().requirements())
          .singleElement()
          .satisfies(
              (Requirement requirement) ->
                  assertThat(requirement.getRepo()).isEqualTo("projects/AUD/repos/json/raw/"));
      assertThat(yaml.getDataMD5Hash()).isNotEqualTo(json.getDataMD5Hash());
      assertThat(yaml.getDataSHA256Hash()).isNotEqualTo(json.getDataSHA256Hash());
    }

    @Test
    void emptyAndNullResponsesDoNotCreateRequirements() throws IOException {
      AudRequirementsRepository empty = sut.processResponses("empty", new String[0], false);
      AudRequirementsRepository nullResponse =
          sut.processResponses("null", new String[] {null}, false);

      assertThat(empty.getData()).isNull();
      assertThat(empty.getDataMD5Hash()).isNull();
      assertThat(nullResponse.getData()).isNull();
      assertThat(nullResponse.getDataMD5Hash()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
    }

    @Test
    void malformedContentIsPropagatedForBothFormats() {
      assertThatThrownBy(
              () -> sut.processResponses("1.0.0", new String[] {"requirements: ["}, false))
          .isInstanceOf(IOException.class);
      assertThatThrownBy(
              () -> sut.processResponses("1.0.0", new String[] {"{\"requirements\": ["}, true))
          .isInstanceOf(IOException.class);
    }
  }

  @Nested
  class Save {
    @Test
    void saveUsesRequirementsContainerAndVersionedName() {
      BlockBlobItem savedItem = blockBlobItem();
      when(blobClient.upload(any(BinaryData.class), eq(true))).thenReturn(Mono.just(savedItem));
      ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

      BlockBlobItem result =
          sut.save("3.0.0", "content", "requirements.json.gz")
              .await()
              .atMost(ExternalServiceTestFixture.TIMEOUT);

      assertThat(result).isSameAs(savedItem);
      verify(containerClient).getBlobAsyncClient(nameCaptor.capture());
      assertThat(nameCaptor.getValue()).isEqualTo("3.0.0/requirements.json.gz");
    }

    @Test
    void saveFailureIsPropagated() {
      when(blobClient.upload(any(BinaryData.class), eq(true)))
          .thenReturn(Mono.error(new IllegalStateException("save failed")));

      assertThatThrownBy(
              () ->
                  sut.save("3.0.0", "content", "requirements.json.gz")
                      .await()
                      .atMost(ExternalServiceTestFixture.TIMEOUT))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("save failed");
    }
  }

  private static BlockBlobItem blockBlobItem() {
    return new BlockBlobItem(
        "etag", OffsetDateTime.parse("2026-01-01T00:00:00Z"), null, Boolean.TRUE, null, null, null);
  }
}

package com.amadeus.aud.libraries.engine.data.services.external.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@ResourceLock("mutiny-infrastructure")
class LocalFileServiceTest {
  private static final Duration TIMEOUT = Duration.ofSeconds(2);

  @TempDir Path temporaryDirectory;

  private LocalFileService sut;

  @BeforeEach
  void setUp() {
    AudDataConfig audDataConfig = mock(AudDataConfig.class);
    AudDataConfig.LocalFileConfig localFileConfig = mock(AudDataConfig.LocalFileConfig.class);
    when(audDataConfig.localFile()).thenReturn(localFileConfig);
    when(localFileConfig.localDataDirectory()).thenReturn(temporaryDirectory.toString());
    sut = new LocalFileService(audDataConfig);
  }

  @Nested
  class InitObjects {
    @Test
    void explicitVersionPrefersGzipAndReadsPlainFiles() throws IOException {
      Path versionDirectory = Files.createDirectories(temporaryDirectory.resolve("1.2.3"));
      Files.writeString(versionDirectory.resolve("api.yml"), "plain-api");
      writeGzip(versionDirectory.resolve("api.yml.gz"), "compressed-api");
      Files.writeString(versionDirectory.resolve("metadata.yml"), "plain-metadata");
      AtomicReference<String> processedVersion = new AtomicReference<>();
      AtomicReference<String[]> processedResponses = new AtomicReference<>();
      AtomicReference<Boolean> processedAsJson = new AtomicReference<>();
      LocalFileDataProcessor<String> processor =
          (String version, String[] responses, boolean jsonPreferred) -> {
            processedVersion.set(version);
            processedResponses.set(responses);
            processedAsJson.set(jsonPreferred);
            return "repository";
          };

      String result =
          sut.initObjects("1.2.3", new String[] {"api.yml", "metadata.yml"}, processor, false)
              .await()
              .atMost(TIMEOUT);

      assertThat(result).isEqualTo("repository");
      assertThat(processedVersion).hasValue("1.2.3");
      assertThat(processedResponses.get()).containsExactly("compressed-api", "plain-metadata");
      assertThat(processedAsJson).hasValue(false);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "latest")
    void latestAliasesLoadHighestSemanticVersion(String requestedVersion) throws IOException {
      Files.createDirectories(temporaryDirectory.resolve("1.9.9"));
      Path latestDirectory = Files.createDirectories(temporaryDirectory.resolve("2.0.0"));
      Files.writeString(temporaryDirectory.resolve("1.9.9").resolve("api.yml"), "older-content");
      Files.writeString(latestDirectory.resolve("api.yml"), "latest-content");
      LocalFileDataProcessor<String> processor =
          (String version, String[] responses, boolean jsonPreferred) ->
              version + ":" + responses[0];

      String result =
          sut.initObjects(requestedVersion, new String[] {"api.yml"}, processor, false)
              .await()
              .atMost(TIMEOUT);

      assertThat(result).isEqualTo("2.0.0:latest-content");
    }

    @Test
    void emptyFileListStillInvokesProcessor() {
      LocalFileDataProcessor<Integer> processor =
          (String version, String[] responses, boolean jsonPreferred) -> responses.length;

      Integer responseCount =
          sut.initObjects("3.4.5", new String[0], processor, true).await().atMost(TIMEOUT);

      assertThat(responseCount).isZero();
    }

    @Test
    void processorFailureIsPropagated() throws IOException {
      Path versionDirectory = Files.createDirectories(temporaryDirectory.resolve("1.0.0"));
      Files.writeString(versionDirectory.resolve("api.yml"), "content");
      LocalFileDataProcessor<String> processor =
          (String version, String[] responses, boolean jsonPreferred) -> {
            throw new IOException("processor failed");
          };

      assertThatThrownBy(
              () ->
                  sut.initObjects("1.0.0", new String[] {"api.yml"}, processor, false)
                      .await()
                      .atMost(TIMEOUT))
          .hasRootCauseMessage("processor failed");
    }

    @Test
    void missingFileIsPropagated() throws IOException {
      Files.createDirectories(temporaryDirectory.resolve("1.0.0"));
      LocalFileDataProcessor<String> processor =
          (String version, String[] responses, boolean jsonPreferred) -> responses[0];

      assertThatThrownBy(
              () ->
                  sut.initObjects("1.0.0", new String[] {"missing.yml"}, processor, false)
                      .await()
                      .atMost(TIMEOUT))
          .hasRootCauseInstanceOf(java.nio.file.NoSuchFileException.class);
    }

    @Test
    void invalidGzipIsPropagated() throws IOException {
      Path versionDirectory = Files.createDirectories(temporaryDirectory.resolve("1.0.0"));
      Files.writeString(versionDirectory.resolve("api.yml.gz"), "not-gzip");
      LocalFileDataProcessor<String> processor =
          (String version, String[] responses, boolean jsonPreferred) -> responses[0];

      assertThatThrownBy(
              () ->
                  sut.initObjects("1.0.0", new String[] {"api.yml"}, processor, false)
                      .await()
                      .atMost(TIMEOUT))
          .hasRootCauseInstanceOf(java.util.zip.ZipException.class);
    }
  }

  @Nested
  class FindVersions {
    @Test
    void latestIgnoresFilesInvalidVersionsAndNonDirectories() throws IOException {
      Files.createDirectories(temporaryDirectory.resolve("1.9.9"));
      Files.createDirectories(temporaryDirectory.resolve("2.0.0"));
      Files.createDirectories(temporaryDirectory.resolve("2.0.1"));
      Files.createDirectories(temporaryDirectory.resolve("02.0.2"));
      Files.createDirectories(temporaryDirectory.resolve("latest"));
      Files.writeString(temporaryDirectory.resolve("9.9.9"), "not a directory");

      String result = sut.findLatestVersion().await().atMost(TIMEOUT);

      assertThat(result).isEqualTo("2.0.1");
    }

    @Test
    void latestFailsWhenNoValidVersionDirectoryExists() throws IOException {
      Files.createDirectories(temporaryDirectory.resolve("latest"));
      Files.writeString(temporaryDirectory.resolve("1.0.0"), "not a directory");

      assertThatThrownBy(() -> sut.findLatestVersion().await().atMost(TIMEOUT))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("No version directories found");
    }

    @Test
    void latestPropagatesMissingBaseDirectory() throws IOException {
      Files.delete(temporaryDirectory);

      assertThatThrownBy(() -> sut.findLatestVersion().await().atMost(TIMEOUT))
          .hasRootCauseInstanceOf(java.nio.file.NoSuchFileException.class);
    }

    @Test
    void allVersionsAreFilteredAndSortedDescending() throws IOException {
      Files.createDirectories(temporaryDirectory.resolve("1.10.0"));
      Files.createDirectories(temporaryDirectory.resolve("2.0.0"));
      Files.createDirectories(temporaryDirectory.resolve("1.2.11"));
      Files.createDirectories(temporaryDirectory.resolve("1.2.3"));
      Files.createDirectories(temporaryDirectory.resolve("1.2"));
      Files.writeString(temporaryDirectory.resolve("9.9.9"), "not a directory");

      List<String> versions = sut.findAllVersions();

      assertThat(versions).containsExactly("2.0.0", "1.10.0", "1.2.11", "1.2.3");
    }

    @Test
    void allVersionsReturnsEmptyListForEmptyDirectory() throws IOException {
      List<String> versions = sut.findAllVersions();

      assertThat(versions).isEmpty();
    }

    @Test
    void allVersionsPropagatesMissingBaseDirectory() throws IOException {
      Files.delete(temporaryDirectory);

      assertThatThrownBy(sut::findAllVersions)
          .isInstanceOf(java.nio.file.NoSuchFileException.class);
    }
  }

  private static void writeGzip(Path path, String content) throws IOException {
    try (GZIPOutputStream outputStream = new GZIPOutputStream(Files.newOutputStream(path))) {
      outputStream.write(content.getBytes(StandardCharsets.UTF_8));
    }
  }
}

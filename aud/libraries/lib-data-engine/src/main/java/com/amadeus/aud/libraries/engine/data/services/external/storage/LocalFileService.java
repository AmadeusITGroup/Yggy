package com.amadeus.aud.libraries.engine.data.services.external.storage;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@ApplicationScoped
public class LocalFileService {

  private final AudDataConfig audDataConfig;
  private static final Pattern SEMVER =
      Pattern.compile("^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)$");

  @Inject
  public LocalFileService(AudDataConfig audDataConfig) {
    this.audDataConfig = audDataConfig;
  }

  /**
   * Initialize objects from a specific version or the latest version.
   *
   * @param version The version to load, or null for the latest
   * @param fileNames The names of the files to load
   * @param dataProcessor The processor to handle the loaded data
   * @param jsonPreferred Whether to prefer JSON format over YAML
   * @return A Uni emitting the repository with the loaded data
   */
  public <R> Uni<R> initObjects(
      @Nullable String version,
      String[] fileNames,
      LocalFileDataProcessor<R> dataProcessor,
      boolean jsonPreferred) {
    if (version == null || "latest".equals(version)) {
      return loadLatest(fileNames, dataProcessor, jsonPreferred);
    }
    return load(version, fileNames, dataProcessor, jsonPreferred);
  }

  /**
   * Load the latest version of the data from the local file system.
   *
   * @param fileNames The names of the files to load
   * @param jsonPreferred Whether to prefer JSON format over YAML
   * @return A Uni emitting the repository with the loaded data
   */
  private <R> Uni<R> loadLatest(
      String[] fileNames, LocalFileDataProcessor<R> dataProcessor, boolean jsonPreferred) {
    return findLatestVersion()
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transformToUni(version -> load(version, fileNames, dataProcessor, jsonPreferred));
  }

  /**
   * Load a specific version of the data from the local file system.
   *
   * @param version The version to load
   * @param fileNames The names of the files to load
   * @param jsonPreferred Whether to prefer JSON format over YAML
   * @return A Uni emitting the repository with the loaded data
   */
  private <R> Uni<R> load(
      String version,
      String[] fileNames,
      LocalFileDataProcessor<R> dataProcessor,
      boolean jsonPreferred) {
    List<Uni<String>> contents = new ArrayList<>(fileNames.length);
    for (int i = 0; i < fileNames.length; i++) {
      String fileName = fileNames[i];
      contents.add(loadFile(version, fileName));
      // Uni.createFrom()
      //     .item(() -> Unchecked.supplier(() -> loadFile(version, fileName)).get())
      //     .runSubscriptionOn(Infrastructure.getDefaultWorkerPool()));
    }
    return Uni.join()
        .all(contents)
        .andCollectFailures()
        .onItem()
        .transform(
            Unchecked.function(
                (List<String> responses) ->
                    dataProcessor.processResponses(
                        version, responses.toArray(new String[0]), jsonPreferred)));
  }

  private Uni<String> loadFile(String version, String fileName) {
    return Uni.createFrom()
        .item(
            Unchecked.supplier(
                () -> {
                  Path path =
                      Path.of(audDataConfig.localFile().localDataDirectory())
                          .resolve(version)
                          .resolve(fileName);

                  // Prefer compressed file if it exists
                  Path gzPath = path.resolveSibling(path.getFileName() + ".gz");
                  if (Files.exists(gzPath)) {
                    path = gzPath;
                  }
                  if (path.getFileName().toString().endsWith(".gz")) {
                    try (GZIPInputStream gis = new GZIPInputStream(Files.newInputStream(path))) {
                      return new String(gis.readAllBytes(), StandardCharsets.UTF_8);
                    }
                  }

                  return Files.readString(path);
                }))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }

  /**
   * Find the latest version available in the local file system.
   *
   * @return A Uni emitting the latest version as a string
   */
  public Uni<String> findLatestVersion() {
    return Uni.createFrom()
        .item(
            () ->
                Unchecked.supplier(
                        () -> {
                          Path baseDir = Path.of(audDataConfig.localFile().localDataDirectory());
                          String latestVersion = null;

                          try (DirectoryStream<Path> dirs = Files.newDirectoryStream(baseDir)) {
                            for (Path path : dirs) {
                              if (!Files.isDirectory(path)) {
                                continue;
                              }

                              String name = path.getFileName().toString();

                              if (!SEMVER.matcher(name).matches()) {
                                continue;
                              }

                              if (latestVersion == null
                                  || compareVersions(name, latestVersion) > 0) {
                                latestVersion = name;
                              }
                            }
                          }

                          if (latestVersion == null) {
                            throw new IllegalStateException("No version directories found");
                          }

                          return latestVersion;
                        })
                    .get())
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }

  public List<String> findAllVersions() throws IOException {
    Path baseDir = Path.of(audDataConfig.localFile().localDataDirectory());
    List<String> versions = new ArrayList<>();

    try (DirectoryStream<Path> dirs = Files.newDirectoryStream(baseDir)) {
      for (Path path : dirs) {
        if (!Files.isDirectory(path)) {
          continue;
        }

        String name = path.getFileName().toString();

        if (SEMVER.matcher(name).matches()) {
          versions.add(name);
        }
      }
    }

    versions.sort((v1, v2) -> compareVersions(v2, v1)); // descending

    return versions;
  }

  private int compareVersions(String v1, String v2) {
    String[] p1 = v1.split("\\.");
    String[] p2 = v2.split("\\.");

    int max = Math.max(p1.length, p2.length);

    for (int i = 0; i < max; i++) {
      int n1 = i < p1.length ? Integer.parseInt(p1[i]) : 0;
      int n2 = i < p2.length ? Integer.parseInt(p2[i]) : 0;

      int cmp = Integer.compare(n1, n2);

      if (cmp != 0) {
        return cmp;
      }
    }

    return 0;
  }
}

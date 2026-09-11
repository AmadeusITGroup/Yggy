package com.amadeus.aud.libraries.engine.data.services.external.parsing;

import com.amadeus.aud.libraries.yaml.models.deserializers.KindBasedDeserializer;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

final class ParsingServiceTestSupport {
  private static final long REPORTED_LARGE_FILE_SIZE = 101L * 1024 * 1024;

  private ParsingServiceTestSupport() {}

  static YamlService configuredYamlService(boolean enableOnTheFlyFixes) {
    YamlService service = new YamlService();
    ObjectMapper conversionMapper = new YamlService().getMapper();
    KindBasedDeserializer deserializer =
        new KindBasedDeserializer(
            new DefaultKindMapper(), conversionMapper, false, enableOnTheFlyFixes);
    SimpleModule module = new SimpleModule();
    module.addDeserializer(Object.class, deserializer);
    service.getMapper().registerModule(module);
    return service;
  }

  static File resourceFile(String name) {
    URL resource = ParsingServiceTestSupport.class.getResource("/parsing/" + name);
    if (resource == null) {
      throw new IllegalArgumentException("Missing test resource: " + name);
    }
    try {
      URI uri = resource.toURI();
      return new File(uri);
    } catch (URISyntaxException exception) {
      throw new IllegalArgumentException("Invalid test resource URI: " + name, exception);
    }
  }

  static Path outputPath(String testCase, String fileName) {
    return Path.of("target", "parsing-service-tests", testCase, fileName);
  }

  static void deleteOutput(Path path) throws IOException {
    Files.deleteIfExists(path);
    Path testCaseDirectory = path.getParent();
    if (testCaseDirectory != null) {
      Files.deleteIfExists(testCaseDirectory);
    }
  }

  static final class ReportedLargeFile extends File {
    ReportedLargeFile(File actualFile) {
      super(actualFile.toURI());
    }

    @Override
    public long length() {
      return REPORTED_LARGE_FILE_SIZE;
    }
  }
}

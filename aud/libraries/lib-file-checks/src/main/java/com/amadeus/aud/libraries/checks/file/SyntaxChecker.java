package com.amadeus.aud.libraries.checks.file;

import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.types.FileCheck;
import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.models.parsing.YamlService;
import com.amadeus.aud.libraries.models.parsing.YamlService.MissingDocumentStartException;
import com.amadeus.aud.libraries.models.parsing.YamlService.NonAsciiYamlException;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.utils.ModelElementMixin;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.JacksonYAMLParseException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.yaml.snakeyaml.LoaderOptions;

@Singleton
public final class SyntaxChecker implements FileCheck, WithId {
  static final CharSequence CODE = "SYN00001";

  @Override
  public String id() {
    return CODE.toString();
  }

  @Override
  public Collection<Message> check(CharSequence domain, Path path) {
    Collection<Message> messages = new ConcurrentLinkedQueue<>();
    check(path, messages);
    return messages;
  }

  private static void check(final Path path, Collection<Message> messages) {
    final ObjectMapper mapper = SyntaxChecker.mapper(messages);
    final YAMLFactory yamlFactory = new YAMLFactory();
    int numberOfThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    try (Stream<Path> paths = Files.walk(path, 10);
        ExecutorService executors = Executors.newFixedThreadPool(numberOfThreads)) {
      paths
          .filter(Files::isRegularFile)
          .filter((Path p) -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
          .forEach((Path p) -> executors.submit(() -> checkYaml(yamlFactory, mapper, p, messages)));
    } catch (IOException e) {
      var links = new ArrayList<URI>();
      links.add(path.toUri());
      messages.add(
          Message.error(
              CODE, "Error finding yaml files:\n" + e.getMessage().replace('\\', '/'), links));
    }
  }

  private static ObjectMapper mapper(Collection<Message> messages) {
    ObjectMapper objectMapper = buildObjectMapper();
    SimpleModule module = new SimpleModule();
    KindBasedDeserializer kindBasedDeserializer =
        new KindBasedDeserializer(buildObjectMapper(), messages);

    module.addDeserializer(Object.class, kindBasedDeserializer);
    objectMapper.registerModule(module);

    return objectMapper;
  }

  private static void checkYaml(
      YAMLFactory yamlFactory, ObjectMapper mapper, Path path, Collection<Message> messages) {
    Log.debugf("Checking %s", path);
    List<URI> links = new ArrayList<>();
    try {
      YamlService.importFromFile(yamlFactory, mapper, path, new ConcurrentHashMap<>());
    } catch (MissingDocumentStartException e) {
      links.add(fileUri(path, 1));
      messages.add(Message.error(CODE, e.getMessage(), links));
    } catch (NonAsciiYamlException e) {
      links.add(fileUri(path, e.getLine()));
      messages.add(Message.error(CODE, e.getMessage(), links));
    } catch (JacksonYAMLParseException e) {
      links.add(fileUri(path, e.getLocation().getLineNr()));
      messages.add(
          Message.error(CODE, "Error parsing Yaml file:\n" + e.getOriginalMessage(), links));
    } catch (RuntimeException e) {
      if (e.getCause() != null && e.getCause() instanceof JacksonYAMLParseException cause) {
        links.add(fileUri(path, cause.getLocation().getLineNr()));
        messages.add(Message.error(CODE, "Error parsing Yaml file:\n" + e.getMessage(), links));
      } else {
        links.add(fileUri(path));
        messages.add(
            Message.error(
                CODE,
                "Error reading Yaml file:\n"
                    + (e.getMessage() != null ? e.getMessage() : e.toString()),
                links));
      }
    } catch (IOException e) {
      links.add(fileUri(path));
      messages.add(Message.error(CODE, "Error reading Yaml file:\n" + e.getMessage(), links));
    }
  }

  private static ObjectMapper buildObjectMapper() {
    LoaderOptions loaderOptions = new LoaderOptions();
    loaderOptions.setCodePointLimit(Integer.MAX_VALUE);
    YAMLFactory yamlFactory = YAMLFactory.builder().loaderOptions(loaderOptions).build();
    ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
    configureMapper(objectMapper);
    return objectMapper;
  }

  private static void configureMapper(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.addMixIn(ModelElement.class, ModelElementMixin.class);

    // IMPORTANT: leave it as is
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true);
  }

  private static URI fileUri(Path path) {
    return path.toUri();
  }

  private static URI fileUri(Path path, int line) {
    return URI.create(path.toUri() + ":" + line);
  }
}

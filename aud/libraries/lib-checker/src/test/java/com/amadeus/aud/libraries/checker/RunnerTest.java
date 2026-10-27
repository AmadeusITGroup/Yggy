package com.amadeus.aud.libraries.checker;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.checker.models.Bom;
import com.amadeus.aud.libraries.checker.models.BomFromAudRepository;
import com.amadeus.aud.libraries.checker.models.Details;
import com.amadeus.aud.libraries.checker.models.Level;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.Models;
import com.amadeus.aud.libraries.checker.models.Transitional;
import com.amadeus.aud.libraries.checker.types.BomCheck;
import com.amadeus.aud.libraries.checker.types.DomainCheck;
import com.amadeus.aud.libraries.checker.types.FileCheck;
import com.amadeus.aud.libraries.checker.types.GlobalCheck;
import com.amadeus.aud.libraries.checker.types.ModelsCheck;
import com.amadeus.aud.libraries.checker.types.OtherCheck;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

class RunnerTest {
  @TempDir Path tempDir;

  private Runner.RunnerBuilder runnerBuilder() {
    return Runner.builder()
        .withModelsImplClass(Models.class)
        .withInstant(instantSupplier())
        .withCdiSupplier(cdiSupplier());
  }

  private Supplier<Instant> instantSupplier() {
    Deque<Instant> instants = new ArrayDeque<>();
    // 2 instants, 1 second apart
    instants.add(Instant.ofEpochMilli(100_000));
    instants.add(Instant.ofEpochMilli(101_000));
    return instants::pop;
  }

  private Supplier<CDI<Object>> cdiSupplier() {
    return cdiSupplier(Mockito.mock(Models.class));
  }

  @SuppressWarnings("unchecked")
  private Supplier<CDI<Object>> cdiSupplier(Models modelsMock) {
    CDI<Object> cdiMock = Mockito.mock(CDI.class);

    Instance<Models> modelsMockInstance = Mockito.mock(Instance.class);
    Mockito.doReturn(modelsMock).when(modelsMockInstance).get();
    Mockito.doReturn(modelsMockInstance).when(cdiMock).select(Models.class);

    Instance<BomFromAudRepository> bomMockInstance = Mockito.mock(Instance.class);
    BomFromAudRepository bomMock = Mockito.mock(BomFromAudRepository.class);
    Version versionMock = Mockito.mock(Version.class);
    Mockito.doReturn("1.0.1234").when(versionMock).getVersionNumber();
    Mockito.doReturn(versionMock).when(bomMock).getVersion();
    Mockito.doReturn(bomMock).when(bomMockInstance).get();
    Mockito.doReturn(bomMockInstance).when(cdiMock).select(BomFromAudRepository.class);

    Instance<GraphService> graphServiceInstance = Mockito.mock(Instance.class);
    GraphService graphServiceMock = Mockito.mock(GraphService.class);
    Mockito.doReturn(graphServiceMock).when(graphServiceInstance).get();
    Mockito.doReturn(graphServiceInstance).when(cdiMock).select(GraphService.class);

    return () -> cdiMock;
  }

  @SuppressWarnings("unchecked")
  private Supplier<CDI<Object>> cdiSupplierWithThrowingGraph() {
    CDI<Object> cdiMock = Mockito.mock(CDI.class);

    Instance<Models> modelsMockInstance = Mockito.mock(Instance.class);
    Models modelsMock = Mockito.mock(Models.class);
    Mockito.doReturn(modelsMock).when(modelsMockInstance).get();
    Mockito.doReturn(modelsMockInstance).when(cdiMock).select(Models.class);

    Instance<BomFromAudRepository> bomMockInstance2 = Mockito.mock(Instance.class);
    BomFromAudRepository bomMock2 = Mockito.mock(BomFromAudRepository.class);
    Version versionMock2 = Mockito.mock(Version.class);
    Mockito.doReturn("1.0.1234").when(versionMock2).getVersionNumber();
    Mockito.doReturn(versionMock2).when(bomMock2).getVersion();
    Mockito.doReturn(bomMock2).when(bomMockInstance2).get();
    Mockito.doReturn(bomMockInstance2).when(cdiMock).select(BomFromAudRepository.class);

    Instance<GraphService> graphServiceInstance = Mockito.mock(Instance.class);
    GraphService graphServiceMock = Mockito.mock(GraphService.class);
    Mockito.doThrow(new RuntimeException(""))
        .when(graphServiceMock)
        .populateGraphFromDependencies(Mockito.any(), Mockito.any());
    Mockito.doReturn(graphServiceMock).when(graphServiceInstance).get();
    Mockito.doReturn(graphServiceInstance).when(cdiMock).select(GraphService.class);

    return () -> cdiMock;
  }

  @Test
  @DisplayName("No checks should return 2 context messages")
  void noChecks() throws Exception {
    Runner sut =
        runnerBuilder()
            .withChecks(List.of())
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(2);

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(2);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    validateLast(messages);
  }

  private void validateLast(List<Collection<Message>> messages) {
    messages
        .getLast()
        .forEach(
            (Message message) -> {
              assertThat(message.level()).isEqualTo(Level.CONTEXT);
              assertThat(message.details()).isNull();
              assertThat(message.links()).isEmpty();
              assertThat(message.transitional()).isNull();
            });
  }

  private static List<Message> firstMessage() {
    return List.of(
        new Message(
            "",
            Level.CONTEXT,
            "Checks against version 1.0.1234 for domains ABC",
            null,
            null,
            List.of()));
  }

  @Test
  @DisplayName("Single File check")
  void singleFileChecks() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Message message = Message.info("FC00012", "File check passed", null, null, List.of(uri));
    FileCheck fileCheck = (domain, path) -> List.of(message);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(3);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(fileCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            queue.put(check.call());
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(3);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(message);
    validateLast(messages);
  }

  @Test
  @DisplayName("Single Models check")
  void singleModelsChecks() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Message message = Message.info("FC00012", "Models check passed", null, null, List.of(uri));
    ModelsCheck modelsCheck = (domain, models) -> List.of(message);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(3);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(modelsCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            queue.put(check.call());
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(3);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(message);
    validateLast(messages);
  }

  @Test
  @DisplayName("Single Bom check")
  void singleBomChecks() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Message message = Message.info("FC00012", "Bom check passed", null, null, List.of(uri));
    BomCheck bomCheck = (CharSequence domain, Bom bom) -> List.of(message);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(3);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(bomCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            queue.put(check.call());
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(3);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(message);
    validateLast(messages);
  }

  @Test
  @DisplayName("Single Other check")
  void singleOtherChecks() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Message message = Message.info("FC00012", "Models check passed", null, null, List.of(uri));
    OtherCheck otherCheck = (CharSequence domain, Bom bom, List<Link> links) -> List.of(message);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(3);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(otherCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            queue.put(check.call());
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(3);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(message);
    validateLast(messages);
  }

  @Test
  @DisplayName("Single check of each type should 1 message for each check and 2 context messages")
  void singleCheckOfEachType() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Details details = new Details("ABC", "component", "name");
    Transitional transitional = Transitional.of("2025-09-01");
    Message fileCheckMessage =
        Message.info("FC00012", "Other check not passed", details, transitional, List.of(uri));
    Message modelsCheckMessage =
        Message.warning("FC00012", "Other check not passed", details, transitional, List.of(uri));
    Message domainCheckMessages =
        Message.warning("D000012", "Domain check not passed", details, transitional, List.of(uri));
    Message otherCheckMessage =
        Message.error("FC00012", "Other check not passed", details, transitional, List.of(uri));
    FileCheck fileCheck = (domain, path) -> List.of(fileCheckMessage);
    DomainCheck domainCheck = (domain, models) -> List.of(domainCheckMessages);
    ModelsCheck modelsCheck = (domain, models) -> List.of(modelsCheckMessage);
    OtherCheck otherCheck =
        (CharSequence domain, Bom bom, List<Link> links) -> List.of(otherCheckMessage);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(6);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(fileCheck, otherCheck, modelsCheck, domainCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            queue.put(check.call());
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(Path.of("/tmp/random-path")), queue);

    assertThat(queue).hasSize(6);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(fileCheckMessage);
    assertThat(messages.get(2)).containsExactly(domainCheckMessages);
    assertThat(messages.get(3)).containsExactly(modelsCheckMessage);
    assertThat(messages.get(4)).containsExactly(otherCheckMessage);
    validateLast(messages);
  }

  @Test
  @DisplayName("Checks should stop after file check if it returns an error")
  void stopAfterFileCheckIfError() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Details details = new Details("ABC", "component", "name");
    Transitional transitional = Transitional.of("2025-09-01");
    Message fileCheckMessage =
        Message.error("FC00012", "File check not passed", details, transitional, List.of(uri));
    Message modelsCheckMessage =
        Message.warning("FC00012", "Model check not passed", details, transitional, List.of(uri));
    FileCheck fileCheck = (domain, path) -> List.of(fileCheckMessage);
    ModelsCheck modelsCheck = (domain, models) -> List.of(modelsCheckMessage);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(3);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(fileCheck, modelsCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    AtomicBoolean error = new AtomicBoolean(false);
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            Collection<Message> checkMessages = check.call();
                            if (checkMessages.stream().anyMatch(m -> m.level() == Level.ERROR)) {
                              error.set(true);
                            }
                            queue.put(checkMessages);
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return error.get();
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(Path.of("/")), queue);

    assertThat(queue).hasSize(3);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(fileCheckMessage);
    validateLast(messages);
  }

  @Test
  @DisplayName("Checks should stop after model check if it returns an error")
  void stopAfterDomainCheckIfError() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Details details = new Details("ABC", "component", "name");
    Message fileCheckMessage =
        Message.warning("FC00012", "File check not passed", details, null, List.of(uri));
    Message domainCheckMessages =
        Message.error("FC00012", "Domain check not passed", details, null, List.of(uri));
    FileCheck fileCheck = (domain, path) -> List.of(fileCheckMessage);
    DomainCheck domainCheck = (domain, models) -> List.of(domainCheckMessages);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(4);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(fileCheck, domainCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    AtomicBoolean error = new AtomicBoolean(false);
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            Collection<Message> checkMessages = check.call();
                            if (checkMessages.stream().anyMatch(m -> m.level() == Level.ERROR)) {
                              error.set(true);
                            }
                            queue.put(checkMessages);
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return error.get();
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(4);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(fileCheckMessage);
    assertThat(messages.get(2)).containsExactly(domainCheckMessages);
    validateLast(messages);
  }

  @Test
  @DisplayName("Checks should stop after model check if it returns an error")
  void stopAfterModelCheckIfError() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Details details = new Details("ABC", "component", "name");
    Message fileCheckMessage =
        Message.warning("FC00012", "File check not passed", details, null, List.of(uri));
    Message modelsCheckMessage =
        Message.error("FC00012", "Model check not passed", details, null, List.of(uri));
    FileCheck fileCheck = (domain, path) -> List.of(fileCheckMessage);
    ModelsCheck modelsCheck = (domain, models) -> List.of(modelsCheckMessage);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(4);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(fileCheck, modelsCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    AtomicBoolean error = new AtomicBoolean(false);
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            Collection<Message> checkMessages = check.call();
                            if (checkMessages.stream().anyMatch(m -> m.level() == Level.ERROR)) {
                              error.set(true);
                            }
                            queue.put(checkMessages);
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return error.get();
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(4);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(fileCheckMessage);
    assertThat(messages.get(2)).containsExactly(modelsCheckMessage);
    validateLast(messages);
  }

  @Test
  @DisplayName("Checks should stop after bom check if it returns an error")
  void stopAfterBomCheckIfError() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Details details = new Details("ABC", "component", "name");
    Message fileCheckMessage =
        Message.warning("FC00012", "File check not passed", details, null, List.of(uri));
    Message modelsCheckMessage =
        Message.warning("FC00012", "Model check not passed", details, null, List.of(uri));
    Message bomCheckMessage =
        Message.error("FC00012", "Model check not passed", details, null, List.of(uri));
    FileCheck fileCheck = (domain, path) -> List.of(fileCheckMessage);
    ModelsCheck modelsCheck = (domain, models) -> List.of(modelsCheckMessage);
    BomCheck bomCheck = (domain, bom) -> List.of(bomCheckMessage);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(5);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(fileCheck, modelsCheck, bomCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    AtomicBoolean error = new AtomicBoolean(false);
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            Collection<Message> checkMessages = check.call();
                            if (checkMessages.stream().anyMatch(m -> m.level() == Level.ERROR)) {
                              error.set(true);
                            }
                            queue.put(checkMessages);
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return error.get();
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(5);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(fileCheckMessage);
    assertThat(messages.get(2)).containsExactly(modelsCheckMessage);
    assertThat(messages.get(3)).containsExactly(bomCheckMessage);
    validateLast(messages);
  }

  @Test
  @DisplayName("If graph check fails, an error message is added to the report")
  void errorIfGraphFails() throws Exception {
    Message message =
        Message.error(
            "INT00001",
            """
            Error while trying to create a graph.
            This is likely due to a bad AUD definition:
            """,
            null,
            null,
            List.of());
    OtherCheck otherCheck = (CharSequence domain, Bom bom, List<Link> links) -> List.of(message);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(3);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(otherCheck))
            .withInstant(instantSupplier())
            .withCdiSupplier(cdiSupplierWithThrowingGraph())
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            queue.put(check.call());
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(3);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(message);
    validateLast(messages);
  }

  @Test
  @DisplayName("Single Global check")
  void singleGlobalChecks() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Message message = Message.info("FC00012", "Global check passed", null, null, List.of(uri));
    GlobalCheck globalCheck =
        (Collection<String> domains, Models models, Bom bom, List<Link> links) -> List.of(message);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(3);
    Runner sut =
        runnerBuilder()
            .withChecks(List.of(globalCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    checks.forEach(
                        (Callable<Collection<Message>> check) -> {
                          try {
                            queue.put(check.call());
                          } catch (Exception e) {
                            throw new RuntimeException(e);
                          }
                        });
                    return false;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of("name", Duration.ofMillis(10).toMillis());
                  }
                })
            .build();

    sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(queue).hasSize(3);
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(message);
    validateLast(messages);
  }

  @Test
  @DisplayName("Optional.empty() path skips FileChecks")
  void emptyPathBypassesFileChecks() throws Exception {
    FileCheck fileCheck =
        (CharSequence domain, Path path) -> {
          throw new AssertionError(
              "FileCheck should not be invoked when running with Optional.empty() path");
        };
    CheckRunner checkRunner = Mockito.mock(CheckRunner.class);
    Runner sut =
        runnerBuilder().withChecks(List.of(fileCheck)).withCheckRunner(checkRunner).build();
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(5);

    boolean result = sut.run("1.0.1234", List.of("ABC"), Optional.empty(), queue);

    assertThat(result).isFalse();
    Mockito.verify(checkRunner, Mockito.never())
        .runChecks(Mockito.any(), Mockito.any(), Mockito.any());
  }

  private Runner modelsCheckRunner(Models modelsMock) {
    ModelsCheck modelsCheck = (domain, models) -> List.of();
    CheckRunner checkRunner = Mockito.mock(CheckRunner.class);
    return runnerBuilder()
        .withCdiSupplier(cdiSupplier(modelsMock))
        .withChecks(List.of(modelsCheck))
        .withCheckRunner(checkRunner)
        .build();
  }

  @Test
  @DisplayName("Without local files, selected domains keep their stored data")
  void noPathDoesNotOverrideSelectedDomains() throws Exception {
    Models modelsMock = Mockito.mock(Models.class);
    Runner sut = modelsCheckRunner(modelsMock);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(5);

    sut.run("1.0.1234", List.of("AUD"), Optional.empty(), queue);

    Mockito.verify(modelsMock).loadLatestWithDomains(Set.of(), Map.of());
  }

  @Test
  @DisplayName("With local files, a selected domain without local elements is replaced by nothing")
  void pathWithoutElementsStillOverridesSelectedDomains() throws Exception {
    Models modelsMock = Mockito.mock(Models.class);
    Runner sut = modelsCheckRunner(modelsMock);
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(5);

    // tempDir is empty: the domain was emptied on purpose
    sut.run("1.0.1234", List.of("AUD"), Optional.of(tempDir), queue);

    Mockito.verify(modelsMock).loadLatestWithDomains(Set.of("AUD"), Map.of());
  }

  @Test
  @DisplayName("Multi-domain OtherCheck executes for each domain")
  void multiDomainOtherChecks() throws Exception {
    List<String> executedDomains = new ArrayList<>();
    OtherCheck otherCheck =
        (CharSequence domain, Bom bom, List<Link> links) -> {
          executedDomains.add(domain.toString());
          return List.of();
        };
    CheckRunner checkRunner = Mockito.mock(CheckRunner.class);
    Mockito.when(checkRunner.runChecks(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenAnswer(
            (InvocationOnMock inv) -> {
              Collection<WrappedCaller> callers = inv.getArgument(1);
              for (WrappedCaller caller : callers) {
                caller.call();
              }
              return false;
            });

    Runner sut =
        runnerBuilder().withChecks(List.of(otherCheck)).withCheckRunner(checkRunner).build();
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(5);

    boolean result = sut.run("1.0.1234", List.of("DOM_A", "DOM_B"), Optional.of(tempDir), queue);

    assertThat(result).isFalse();
    assertThat(executedDomains).containsExactly("DOM_A", "DOM_B");
  }

  private enum TestErrorCode {
    FC00001,
    MC00001,
    BC00001,
    OC00001,
    GC00001
  }

  @Test
  @DisplayName("When failFast is false, errors in earlier checks do not halt subsequent checks")
  void failFastFalseRunsAllPhasesDespiteErrors() throws Exception {
    URI uri = URI.create("file://hello.yaml");
    Details details = new Details("ABC", "component", "name");
    Message fileCheckError =
        Message.error(
            TestErrorCode.FC00001.name(), "File check failed", details, null, List.of(uri));
    Message modelsCheckError =
        Message.error(
            TestErrorCode.MC00001.name(), "Models check failed", details, null, List.of(uri));
    Message bomCheckError =
        Message.error(
            TestErrorCode.BC00001.name(), "Bom check failed", details, null, List.of(uri));
    Message otherCheckError =
        Message.error(
            TestErrorCode.OC00001.name(), "Other check failed", details, null, List.of(uri));
    Message globalCheckError =
        Message.error(
            TestErrorCode.GC00001.name(), "Global check failed", details, null, List.of(uri));

    FileCheck fileCheck = (domain, path) -> List.of(fileCheckError);
    ModelsCheck modelsCheck = (domain, models) -> List.of(modelsCheckError);
    BomCheck bomCheck = (domain, bom) -> List.of(bomCheckError);
    OtherCheck otherCheck =
        (CharSequence domain, Bom bom, List<Link> links) -> List.of(otherCheckError);
    GlobalCheck globalCheck =
        (Collection<String> domains, Models models, Bom bom, List<Link> links) ->
            List.of(globalCheckError);

    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(10);
    Runner sut =
        runnerBuilder()
            .withFailFast(false)
            .withChecks(List.of(fileCheck, modelsCheck, bomCheck, otherCheck, globalCheck))
            .withCheckRunner(
                new CheckRunner() {
                  @Override
                  public boolean runChecks(
                      CharSequence name,
                      Collection<WrappedCaller> checks,
                      BlockingQueue<Collection<Message>> queue) {
                    boolean hasError = false;
                    for (WrappedCaller check : checks) {
                      try {
                        Collection<Message> msgs = check.call();
                        if (msgs.stream().anyMatch(m -> m.level() == Level.ERROR)) {
                          hasError = true;
                        }
                        queue.put(msgs);
                      } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(
                            "Test interrupted while putting message into queue", e);
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    }
                    return hasError;
                  }

                  @Override
                  public Map<String, Long> timings() {
                    return Map.of();
                  }
                })
            .build();

    boolean hasErrors = sut.run("1.0.1234", List.of("ABC"), Optional.of(tempDir), queue);

    assertThat(hasErrors).isTrue();
    List<Collection<Message>> messages = new ArrayList<>(queue.size());
    queue.drainTo(messages);
    assertThat(messages).first().isEqualTo(firstMessage());
    assertThat(messages.get(1)).containsExactly(fileCheckError);
    assertThat(messages.get(2)).containsExactly(modelsCheckError);
    assertThat(messages.get(3)).containsExactly(bomCheckError);
    assertThat(messages.get(4)).containsExactly(otherCheckError);
    assertThat(messages.get(5)).containsExactly(globalCheckError);
    validateLast(messages);
  }
}

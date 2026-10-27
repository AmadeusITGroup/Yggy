package com.amadeus.aud.libraries.checker;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.checker.config.Configuration;
import com.amadeus.aud.libraries.checker.config.Defaults;
import com.amadeus.aud.libraries.checker.config.SpecificError;
import com.amadeus.aud.libraries.checker.models.Details;
import com.amadeus.aud.libraries.checker.models.Level;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.types.FileCheck;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SingleCheckTypeRunnerTest {

  @Test
  @DisplayName("When the check itself fails, it should stop the process")
  void messageDueToBugInCheckerShouldStopExecution() throws Exception {
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of();
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(
            new WrappedCaller(
                (FileCheck) (domain, path) -> List.of(),
                () -> {
                  throw new ExecutionException(new RuntimeException("Bug in checker"));
                }));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isTrue();
    assertThat(queue).hasSize(1);
    final Message m = new ArrayList<>(queue.take()).getFirst();
    assertThat(m.code()).isEqualTo("INT00001");
    assertThat(m.level()).isEqualTo(Level.ERROR);
    assertThat(m.message())
        .startsWith("Check failed due to a bug in the check 'SingleCheckTypeRunner");
  }

  @Test
  @DisplayName("Message withSpecificError null should not alter the message")
  void messageWithoutSpecificError() throws Exception {
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of();
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Message message = Message.error("code", "message", null, null, List.of());
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isTrue();
    assertThat(queue.take()).containsExactly(message);
  }

  @Test
  @DisplayName("Message withSpecificError but that is not an ERROR should not alter the message")
  void messageWithSpecificErrorNotError() throws Exception {
    Message message = Message.warning("code", "message", null, null, List.of());
    SpecificError errorConfig =
        new SpecificError(LocalDate.now(), Duration.ofDays(3), null, null, null);
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isFalse();
    assertThat(queue.take()).containsExactly(message);
  }

  @Test
  @DisplayName("Message withSpecificError both dates passed should not alter the message")
  void messageWithSpecificErrorBothDatesPassed() throws Exception {
    Message message = Message.error("code", "message", null, null, List.of());
    SpecificError errorConfig =
        new SpecificError(
            LocalDate.now().minusDays(5),
            Duration.ofDays(3),
            null,
            null,
            LocalDate.now().minusDays(1));
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isTrue();
    assertThat(queue.take()).containsExactly(message);
  }

  @Test
  @DisplayName(
      "Message withSpecificError date passed but no exceptional date should not alter the message")
  void messageWithSpecificErrorDatePassedNoExceptionalDate() throws Exception {
    Message message = Message.error("code", "message", null, null, List.of());
    SpecificError errorConfig =
        new SpecificError(LocalDate.now().minusDays(5), Duration.ofDays(3), null, null, null);
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(2);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isTrue();
    assertThat(queue.take()).containsExactly(message);
  }

  @Test
  @DisplayName(
      "Message withSpecificError date passed with exceptional date not passed but no domain should not alter the message")
  void messageWithSpecificErrorDatePassedWithExceptionalDateNoDomain() throws Exception {
    Message message =
        Message.error("code", "message", new Details(null, null, null), null, List.of());
    SpecificError errorConfig =
        new SpecificError(
            LocalDate.now().minusDays(5),
            Duration.ofDays(3),
            null,
            null,
            LocalDate.now().plusDays(5));
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isTrue();
    assertThat(queue.take()).containsExactly(message);
  }

  @Test
  @DisplayName(
      "Message withSpecificError date passed with exceptional date not passed but unmatched domain should not alter the message")
  void messageWithSpecificErrorDatePassedWithExceptionalDateUnmatchedDomain() throws Exception {
    Message message =
        Message.error("code", "message", new Details("scope1", null, null), null, List.of());
    SpecificError errorConfig =
        new SpecificError(
            LocalDate.now().minusDays(5),
            Duration.ofDays(3),
            null,
            Pattern.compile("scope2"),
            LocalDate.now().plusDays(5));
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isTrue();
    assertThat(queue.take()).containsExactly(message);
  }

  @Test
  @DisplayName(
      "Message withSpecificError date passed with exceptional date not passed with matching domain should alter the message")
  void messageWithSpecificErrorDatePassedWithExceptionalDateMatchingDomain() throws Exception {
    Message message =
        Message.error(
            "code", "message", new Details("scope1", null, null), null, new ArrayList<>());
    SpecificError errorConfig =
        new SpecificError(
            LocalDate.now().minusDays(5),
            Duration.ofDays(3),
            URI.create("file://hello"),
            Pattern.compile("scope1"),
            LocalDate.now().plusDays(5));
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isFalse();
    assertThat(queue).hasSize(1);
    Message newMessage = new ArrayList<>(queue.take()).getFirst();
    assertThat(newMessage).isNotEqualTo(message);
    assertThat(newMessage.code()).isEqualTo(message.code());
    assertThat(newMessage.level()).isEqualTo(Level.WARNING);
    assertThat(newMessage.message()).isEqualTo(message.message());
    assertThat(newMessage.links()).hasSize(1);
    assertThat(newMessage.transitional().limitDate())
        .isEqualTo(errorConfig.exceptionalDomainsLimitDate());
  }

  @Test
  @DisplayName("Message withSpecificError with limit date in the future should alter the message")
  void messageWithSpecificErrorDateInFuture() throws Exception {
    Message message =
        Message.error(
            "code", "message", new Details("scope1", null, null), null, new ArrayList<>());
    SpecificError errorConfig =
        new SpecificError(
            LocalDate.now().plusDays(5),
            Duration.ofDays(3),
            URI.create("file://hello"),
            null,
            null);
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isFalse();
    assertThat(queue).hasSize(1);
    Message newMessage = new ArrayList<>(queue.take()).getFirst();
    assertThat(newMessage).isNotEqualTo(message);
    assertThat(newMessage.code()).isEqualTo(message.code());
    assertThat(newMessage.level()).isEqualTo(Level.WARNING);
    assertThat(newMessage.message()).isEqualTo(message.message());
    assertThat(newMessage.links()).hasSize(1);
    assertThat(newMessage.transitional().limitDate()).isEqualTo(errorConfig.limitDate());
  }

  @Test
  @DisplayName(
      "Message withSpecificError only with a link should add it without altering the level")
  void messageWithSpecificErrorLinkOnly() throws Exception {
    Message message =
        Message.error(
            "code", "message", new Details("scope1", null, null), null, new ArrayList<>());
    SpecificError errorConfig =
        new SpecificError(null, null, URI.create("https://example.com"), null, null);
    Defaults defaults = new Defaults(LocalDate.now(), Duration.ofDays(30));
    Map<String, SpecificError> specificErrors = Map.of("code", errorConfig);
    Configuration configuration = new Configuration(defaults, specificErrors);
    CheckRunner sut = new SingleCheckTypeRunner(configuration);
    Collection<WrappedCaller> checks =
        List.of(new WrappedCaller((FileCheck) (domain, path) -> List.of(), () -> List.of(message)));
    BlockingQueue<Collection<Message>> queue = new ArrayBlockingQueue<>(1);

    boolean isError = sut.runChecks("name", checks, queue);

    assertThat(isError).isTrue();
    assertThat(queue).hasSize(1);
    Message newMessage = new ArrayList<>(queue.take()).getFirst();
    assertThat(newMessage.code()).isEqualTo(message.code());
    assertThat(newMessage.level()).isEqualTo(message.level());
    assertThat(newMessage.message()).isEqualTo(message.message());
    assertThat(newMessage.links()).hasSize(1).first().isEqualTo(URI.create("https://example.com"));
    assertThat(newMessage.transitional()).isNull();
  }
}

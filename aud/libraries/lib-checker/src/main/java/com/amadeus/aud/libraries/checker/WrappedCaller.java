package com.amadeus.aud.libraries.checker;

import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.types.CheckType;
import com.amadeus.aud.libraries.common.WithId;
import io.quarkus.logging.Log;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class WrappedCaller implements Callable<Collection<Message>> {
  private final String name;
  private final Callable<Collection<Message>> callable;
  private Optional<Duration> duration = Optional.empty();

  WrappedCaller(CheckType check, Callable<Collection<Message>> callable) {
    this.name = check instanceof WithId withId ? withId.id() : check.getClass().getSimpleName();
    this.callable = callable;
  }

  @Override
  public Collection<Message> call() {
    Instant start = Instant.now();
    try {
      return this.callable.call();
    } catch (Exception e) {
      CharSequence message =
          "Check failed due to a bug in the check '%s' itself:%n%s".formatted(name, e.getMessage());
      Log.error(message, e);
      return List.of(Message.error("INT00001", message, new ArrayList<>()));
    } finally {
      this.duration = Optional.of(Duration.between(start, Instant.now()));
    }
  }

  public String name() {
    return name;
  }

  public Optional<Duration> duration() {
    return duration;
  }
}

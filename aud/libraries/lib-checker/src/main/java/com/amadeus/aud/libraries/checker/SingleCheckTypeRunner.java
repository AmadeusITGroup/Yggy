package com.amadeus.aud.libraries.checker;

import com.amadeus.aud.libraries.checker.config.Configuration;
import com.amadeus.aud.libraries.checker.models.Level;
import com.amadeus.aud.libraries.checker.models.Message;
import io.quarkus.logging.Log;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SingleCheckTypeRunner implements CheckRunner {
  private final Configuration configuration;
  private final ExecutorService executors;
  private final Map<String, Long> timings;

  public SingleCheckTypeRunner(Configuration configuration, WorkerExecutor executors) {
    this.configuration = configuration;
    this.executors = executors;
    this.timings = new HashMap<>();
  }

  protected SingleCheckTypeRunner(Configuration configuration) {
    this(configuration, new WorkerExecutor());
  }

  @Override
  public boolean runChecks(
      CharSequence name,
      Collection<WrappedCaller> checks,
      BlockingQueue<Collection<Message>> queue) {
    Collection<Future<Collection<Message>>> futures;
    Log.infof("%s...", name);
    Instant start = Instant.now();
    try {
      futures = executors.invokeAll(checks);
    } catch (InterruptedException e) {
      // Restore interrupted status
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

    Instant end = Instant.now();
    Log.infof("%s took %s ms", name, Duration.between(start, end).toMillis());
    boolean hasErrors = collectMessages(futures, queue);
    this.timings.putAll(collectTimings(checks));
    return hasErrors;
  }

  private boolean collectMessages(
      Collection<Future<Collection<Message>>> futures, BlockingQueue<Collection<Message>> queue) {
    boolean hasErrors = false;
    for (Future<Collection<Message>> f : futures) {
      try {
        Collection<Message> checkerMessages = f.get();
        Collection<Message> messages = new ArrayList<>(checkerMessages.size());
        for (Message message : checkerMessages) {
          Message postProcessMessage =
              message.withErrorConfig(
                  this.configuration.specificErrors().get(message.code().toString()));
          if (postProcessMessage.level() == Level.ERROR) {
            hasErrors = true;
          }
          messages.add(postProcessMessage);
        }
        queue.put(messages);
      } catch (InterruptedException e) {
        queue.add(List.of(Message.context("Check interrupted: " + e.getMessage())));
        // Restore interrupted status
        Thread.currentThread().interrupt();
      } catch (ExecutionException e) {
        CharSequence message =
            "Check failed due to a bug in the check framework itself: "
                + e.getStackTrace()[0].getClassName();
        Log.error(message, e);
        queue.add(
            List.of(Message.error("INT00001", message + "\n" + e.getMessage(), new ArrayList<>())));
        hasErrors = true;
      }
    }

    return hasErrors;
  }

  private Map<String, Long> collectTimings(Collection<WrappedCaller> checks) {
    Map<String, Long> times = new HashMap<>();
    for (WrappedCaller check : checks) {
      check
          .duration()
          .ifPresent(
              (Duration duration) -> times.merge(check.name(), duration.toMillis(), Long::sum));
    }
    return times;
  }

  public Map<String, Long> timings() {
    return this.timings;
  }
}

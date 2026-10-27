package com.amadeus.aud.libraries.checker;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Execution-scoped settings used by checks that produce auxiliary artifacts.
 *
 * <p>The checker currently executes its checks on worker threads and some check contracts do not
 * carry an execution context. The service therefore serializes complete runs while this holder is
 * active.
 */
public final class CheckerExecutionContext implements AutoCloseable {
  private static final Path DEFAULT_ARTIFACT_DIRECTORY =
      Path.of("/tmp").toAbsolutePath().normalize();
  private static final AtomicReference<State> CURRENT =
      new AtomicReference<>(new State(DEFAULT_ARTIFACT_DIRECTORY, true));

  private final State previous;
  private boolean closed;

  private CheckerExecutionContext(State previous) {
    this.previous = previous;
  }

  public static CheckerExecutionContext open(Path artifactDirectory) {
    return open(artifactDirectory, true);
  }

  public static CheckerExecutionContext open(Path artifactDirectory, boolean reportUploadsEnabled) {
    Objects.requireNonNull(artifactDirectory, "artifactDirectory");
    Path normalizedDirectory = artifactDirectory.toAbsolutePath().normalize();
    State previous = CURRENT.getAndSet(new State(normalizedDirectory, reportUploadsEnabled));
    return new CheckerExecutionContext(previous);
  }

  public static Path artifact(String filename) {
    Objects.requireNonNull(filename, "filename");
    State state = CURRENT.get();
    Path artifact = state.artifactDirectory().resolve(filename).normalize();
    if (!Objects.equals(artifact.getParent(), state.artifactDirectory())) {
      throw new IllegalArgumentException("Artifact filename must remain in the artifact directory");
    }
    return artifact;
  }

  public Path artifactPath(String filename) {
    return artifact(filename);
  }

  public static boolean reportUploadsEnabled() {
    return CURRENT.get().reportUploadsEnabled();
  }

  @Override
  public void close() {
    if (!this.closed) {
      CURRENT.set(this.previous);
      this.closed = true;
    }
  }

  private record State(Path artifactDirectory, boolean reportUploadsEnabled) {}
}

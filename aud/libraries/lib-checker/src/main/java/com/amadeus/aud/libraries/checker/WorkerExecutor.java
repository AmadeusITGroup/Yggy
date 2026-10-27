package com.amadeus.aud.libraries.checker;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class WorkerExecutor extends ThreadPoolExecutor {
  public WorkerExecutor() {
    super(
        Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
        Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>());
  }
}

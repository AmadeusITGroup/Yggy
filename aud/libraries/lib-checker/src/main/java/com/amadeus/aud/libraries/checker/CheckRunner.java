package com.amadeus.aud.libraries.checker;

import com.amadeus.aud.libraries.checker.models.Message;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public interface CheckRunner {
  boolean runChecks(
      CharSequence name,
      Collection<WrappedCaller> checks,
      BlockingQueue<Collection<Message>> queue);

  Map<String, Long> timings();
}

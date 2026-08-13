package com.amadeus.aud.libraries.engine.data.unistrategies;

import com.amadeus.aud.libraries.engine.data.interfaces.ProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ProducerStrategy;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import java.util.Collection;
import java.util.Collections;

public class BaseProducerStrategy<T, U extends ProducerContext> implements ProducerStrategy<T, U> {

  @Override
  public Uni<Collection<T>> withStrategy(Uni<Collection<T>> executable, U context) {
    return executable
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  @Override
  public Collection<T> await(Uni<Collection<T>> executable, U context) {
    Uni<Collection<T>> uni = withFailure(executable, Collections.emptySet());
    return uni.await().atMost(context.maxWait());
  }

  public static <U> Uni<U> withFailure(Uni<U> executable, U defaultValue) {
    return executable
        .onFailure(
            (Throwable throwable) -> {
              Log.errorf(
                  "Error %s happened when creating Uni<%s>: %s",
                  throwable.toString(),
                  defaultValue.getClass().getSimpleName(),
                  throwable.getMessage());
              return true;
            })
        .recoverWithItem(defaultValue);
  }
}

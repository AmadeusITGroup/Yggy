package com.amadeus.aud.libraries.engine.data.interfaces;

import io.smallrye.mutiny.Uni;
import java.util.Collection;

public interface Producer<T, U extends ProducerContext> {

  Uni<Collection<T>> prepareProduceExecutable(final U context);

  Collection<T> produce(final U context);
}

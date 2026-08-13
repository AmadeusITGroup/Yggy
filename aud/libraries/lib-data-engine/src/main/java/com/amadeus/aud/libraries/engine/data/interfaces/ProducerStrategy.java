package com.amadeus.aud.libraries.engine.data.interfaces;

import io.smallrye.mutiny.Uni;
import java.util.Collection;

public interface ProducerStrategy<T, U extends ProducerContext> {

  Uni<Collection<T>> withStrategy(Uni<Collection<T>> executable, U context);

  Collection<T> await(Uni<Collection<T>> executable, U context);
}

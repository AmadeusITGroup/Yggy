package com.amadeus.aud.libraries.engine.data.interfaces;

import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import java.util.Collection;

public interface ConvertingProducer<
    T extends ModelElement,
    U extends WithId,
    V extends
        ConvertingProducerContext<
                ? extends T, ? extends ModelElementsProducerContext<? extends T>>> {

  Uni<Collection<U>> prepareProduceExecutableContinueFromUni(
      final V context, Uni<Collection<T>> unis);

  Collection<U> produceContinueFromUni(final V context, Uni<Collection<T>> unis);

  Uni<Collection<U>> prepareProduceExecutableContinueFromUniWithVersion(
      final V context, Uni<Tuple2<Collection<T>, Version>> unis);

  Collection<U> produceContinueFromUniWithVersion(
      final V context, Uni<Tuple2<Collection<T>, Version>> unis);
}

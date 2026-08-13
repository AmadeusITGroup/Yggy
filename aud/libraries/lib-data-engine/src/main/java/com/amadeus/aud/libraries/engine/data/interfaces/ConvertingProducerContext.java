package com.amadeus.aud.libraries.engine.data.interfaces;

import com.amadeus.aud.libraries.yaml.models.ModelElement;
import java.util.Collection;
import java.util.Set;

public interface ConvertingProducerContext<
        T extends ModelElement, U extends ModelElementsProducerContext<? extends T>>
    extends ProducerContext {

  default Collection<T> baseModelElements() {
    return Set.of();
  }

  U getModelElementsProducerContext();
}

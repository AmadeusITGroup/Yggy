package com.amadeus.aud.libraries.engine.data.interfaces;

import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.base.BaseConverter;
import com.amadeus.aud.libraries.yaml.models.ModelElement;

public interface ConverterFactory<T extends ModelElement, U extends WithId> {
  BaseConverter<
          T,
          U,
          ? extends
              ConvertingProducerContext<
                  ? extends T, ? extends ModelElementsProducerContext<? extends T>>>
      newConverter();
}

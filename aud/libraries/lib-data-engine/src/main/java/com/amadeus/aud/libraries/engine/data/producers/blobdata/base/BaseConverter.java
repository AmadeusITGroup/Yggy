package com.amadeus.aud.libraries.engine.data.producers.blobdata.base;

import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.engine.data.interfaces.ConvertingProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import io.smallrye.mutiny.converters.UniConverter;
import java.util.Collection;

public abstract class BaseConverter<
        T extends ModelElement,
        U extends WithId,
        V extends
            ConvertingProducerContext<
                    ? extends T, ? extends ModelElementsProducerContext<? extends T>>>
    implements UniConverter<Collection<T>, Collection<U>> {

  protected V producerContext;

  protected Version version;

  public BaseConverter<T, U, V> init(V producerContext) {
    this.producerContext = producerContext;
    return this;
  }

  public void setVersion(Version version) {
    this.version = version;
  }
}

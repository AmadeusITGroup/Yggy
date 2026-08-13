package com.amadeus.aud.libraries.engine.data.factories;

import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.engine.data.interfaces.ConvertingProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ProducerStrategy;
import com.amadeus.aud.libraries.engine.data.interfaces.StrategyFactory;
import com.amadeus.aud.libraries.engine.data.unistrategies.BaseProducerStrategy;
import com.amadeus.aud.libraries.yaml.models.ModelElement;

public class ConvertingStrategyFactory<
        T extends WithId,
        U extends
            ConvertingProducerContext<
                    ? extends ModelElement,
                    ? extends ModelElementsProducerContext<? extends ModelElement>>>
    implements StrategyFactory<T, U> {

  @Override
  public ProducerStrategy<T, U> newStrategy() {
    return new BaseProducerStrategy<>();
  }
}

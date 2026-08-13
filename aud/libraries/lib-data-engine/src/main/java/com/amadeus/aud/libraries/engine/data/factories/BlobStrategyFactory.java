package com.amadeus.aud.libraries.engine.data.factories;

import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ProducerStrategy;
import com.amadeus.aud.libraries.engine.data.interfaces.StrategyFactory;
import com.amadeus.aud.libraries.engine.data.unistrategies.BaseProducerStrategy;

public class BlobStrategyFactory<T, U extends ModelElementsProducerContext<? extends T>>
    implements StrategyFactory<T, U> {

  @Override
  public ProducerStrategy<T, U> newStrategy() {
    return new BaseProducerStrategy<>();
  }
}

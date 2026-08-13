package com.amadeus.aud.libraries.engine.data.interfaces;

public interface StrategyFactory<T, U extends ProducerContext> {
  ProducerStrategy<T, U> newStrategy();
}

package com.amadeus.aud.libraries.engine.data.producers.blobdata.base;

import com.amadeus.aud.libraries.engine.data.interfaces.Producer;
import com.amadeus.aud.libraries.engine.data.interfaces.ProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ProducerStrategy;
import com.amadeus.aud.libraries.engine.data.interfaces.StrategyFactory;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogService;
import io.smallrye.mutiny.Uni;
import java.util.Collection;

public abstract class BaseProducer<T, U extends ProducerContext> extends BaseLogService
    implements Producer<T, U> {

  protected BaseProducer(StrategyFactory<T, U> strategyFactory) {
    this.strategyFactory = strategyFactory;
  }

  protected StrategyFactory<T, U> strategyFactory;

  public abstract Uni<Collection<T>> prepareProduceExecutable(final U context);

  @Override
  public Collection<T> produce(final U context) {
    ProducerStrategy<T, U> strategy = strategyFactory.newStrategy();

    Uni<Collection<T>> executable = prepareProduceExecutable(context);
    Uni<Collection<T>> uniAfterExecution = strategy.withStrategy(executable, context);
    return strategy.await(uniAfterExecution, context);
  }

  public StrategyFactory<T, U> getStrategyFactory() {
    return strategyFactory;
  }
}

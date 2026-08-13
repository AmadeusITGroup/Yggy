package com.amadeus.aud.libraries.engine.data.producers.blobdata;

import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.engine.data.interfaces.ConverterFactory;
import com.amadeus.aud.libraries.engine.data.interfaces.ConvertingProducer;
import com.amadeus.aud.libraries.engine.data.interfaces.ConvertingProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ProducerStrategy;
import com.amadeus.aud.libraries.engine.data.interfaces.StrategyFactory;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.base.BaseConverter;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.base.BaseProducer;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.Collection;

public class DTOProducer<
        T extends ModelElement,
        U extends WithId,
        V extends ConvertingProducerContext<T, ? extends ModelElementsProducerContext<? extends T>>>
    extends BaseProducer<U, V> implements ConvertingProducer<T, U, V> {

  private final ConverterFactory<T, U> converterFactory;

  public DTOProducer(
      StrategyFactory<U, V> dtoStrategyFactory, ConverterFactory<T, U> converterFactory) {
    super(dtoStrategyFactory);
    this.converterFactory = converterFactory;
  }

  @Override
  public Uni<Collection<U>> prepareProduceExecutable(final V context) {

    return prepareProduceExecutableContinueFromUni(
        context, Uni.createFrom().item(context.baseModelElements()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Uni<Collection<U>> prepareProduceExecutableContinueFromUni(
      V context, Uni<Collection<T>> baseUnis) {

    BaseConverter<T, U, V> converter = (BaseConverter<T, U, V>) converterFactory.newConverter();
    if (converter instanceof BaseConverter<T, U, V> baseConverter) {
      baseConverter.init(context);
    }
    return baseUnis.onItem().transformToUni(converter::from);
  }

  @Override
  public Collection<U> produceContinueFromUni(V context, Uni<Collection<T>> unis) {
    return commonApplyStrategy(context, prepareProduceExecutableContinueFromUni(context, unis));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Uni<Collection<U>> prepareProduceExecutableContinueFromUniWithVersion(
      V context, Uni<Tuple2<Collection<T>, Version>> baseUnis) {
    BaseConverter<T, U, V> converter = (BaseConverter<T, U, V>) converterFactory.newConverter();
    converter.init(context);

    return baseUnis
        .onItem()
        .transformToUni(
            Unchecked.function(
                (Tuple2<Collection<T>, Version> baseElementsAndVersion) -> {
                  converter.setVersion(baseElementsAndVersion.getItem2());
                  return converter.from(baseElementsAndVersion.getItem1());
                }));
  }

  @Override
  public Collection<U> produceContinueFromUniWithVersion(
      V context, Uni<Tuple2<Collection<T>, Version>> unis) {
    return commonApplyStrategy(
        context, prepareProduceExecutableContinueFromUniWithVersion(context, unis));
  }

  protected Collection<U> commonApplyStrategy(V context, Uni<Collection<U>> preparedExecutable) {
    ProducerStrategy<U, V> strategy = strategyFactory.newStrategy();

    Uni<Collection<U>> uniAfterExecution = strategy.withStrategy(preparedExecutable, context);
    return strategy.await(uniAfterExecution, context);
  }
}

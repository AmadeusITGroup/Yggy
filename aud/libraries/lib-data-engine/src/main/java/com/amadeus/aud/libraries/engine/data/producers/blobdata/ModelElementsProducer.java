package com.amadeus.aud.libraries.engine.data.producers.blobdata;

import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.StrategyFactory;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.base.BaseProducer;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class ModelElementsProducer<
        T extends ModelElement, U extends ModelElementsProducerContext<? extends T>>
    extends BaseProducer<T, U> {

  public ModelElementsProducer(StrategyFactory<T, U> strategyFactory) {
    super(strategyFactory);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Uni<Collection<T>> prepareProduceExecutable(U context) {

    return Uni.createFrom()
        .item(
            Unchecked.supplier(
                () -> {
                  Set<T> modelElements = new HashSet<>();

                  for (String response : context.getResponses()) {
                    ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>>
                        mapOfModelElements = context.getParsingService().importFromString(response);
                    List<ModelElement> modelElementsOfClass =
                        mapOfModelElements.getOrDefault(context.getModelElementClass(), List.of());
                    for (ModelElement modelElement : modelElementsOfClass) {
                      if (context
                          .getModelElementClass()
                          .isAssignableFrom(modelElement.getClass())) {
                        modelElements.add((T) modelElement);
                      }
                    }
                  }
                  return modelElements;
                }));
  }
}

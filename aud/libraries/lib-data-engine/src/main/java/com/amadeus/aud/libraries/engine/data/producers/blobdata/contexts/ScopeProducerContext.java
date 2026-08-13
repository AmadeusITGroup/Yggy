package com.amadeus.aud.libraries.engine.data.producers.blobdata.contexts;

import com.amadeus.aud.libraries.engine.data.interfaces.ConvertingProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;

public class ScopeProducerContext
    implements ConvertingProducerContext<
        ModelElement, ModelElementsProducerContext<? extends V1Scope>> {

  private final Collection<ModelElement> scopeModelElements;
  private final ModelElementsProducerContext<? extends V1Scope> modelElementsContext;

  @Inject
  public ScopeProducerContext(
      ModelElementsProducerContext<? extends V1Scope> modelElementsContext) {
    this(modelElementsContext, Set.of());
  }

  @Inject
  public ScopeProducerContext(
      ModelElementsProducerContext<? extends V1Scope> modelElementsContext,
      Collection<ModelElement> scopeModelElements) {
    this.scopeModelElements = scopeModelElements;
    this.modelElementsContext = modelElementsContext;
  }

  public Collection<ModelElement> getScopeModelElements() {
    return scopeModelElements;
  }

  @Override
  public Collection<ModelElement> baseModelElements() {
    return getScopeModelElements();
  }

  @Override
  public ModelElementsProducerContext<? extends V1Scope> getModelElementsProducerContext() {
    return modelElementsContext;
  }

  @Override
  public Duration maxWait() {
    return modelElementsContext.maxWait();
  }
}

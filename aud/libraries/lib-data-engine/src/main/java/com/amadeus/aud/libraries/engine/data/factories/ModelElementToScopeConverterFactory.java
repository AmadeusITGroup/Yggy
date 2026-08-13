package com.amadeus.aud.libraries.engine.data.factories;

import com.amadeus.aud.libraries.engine.data.interfaces.ConverterFactory;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.base.BaseConverter;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.contexts.ScopeProducerContext;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.converters.ModelElementToScopeConverter;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.ModelElement;

public class ModelElementToScopeConverterFactory implements ConverterFactory<ModelElement, Scope> {

  @Override
  public BaseConverter<ModelElement, Scope, ScopeProducerContext> newConverter() {
    return new ModelElementToScopeConverter();
  }
}

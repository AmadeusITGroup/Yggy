package com.amadeus.aud.libraries.engine.data.interfaces;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudScopesConfig;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.AbstractDocumentParsingService;
import java.util.Collection;

public interface ModelElementsProducerContext<T> extends ProducerContext {

  Class<T> getModelElementClass();

  AbstractDocumentParsingService getParsingService();

  BlobFormat getBlobFormat();

  String getContainerName();

  String getBlobNameExpression();

  Collection<String> getResponses();

  AudScopesConfig getAudScopesConfig();

  AudDataConfig getAudDataConfig();

  String getVersion();
}

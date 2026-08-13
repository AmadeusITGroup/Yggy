package com.amadeus.aud.libraries.engine.data.factories;

import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.ScopeBlobService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountDataProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ScopesServiceFactory {

  @Inject ScopeBlobService scopeBlobService;

  public StorageAccountDataProcessor<ScopesRepository> newScopesService() {
    return scopeBlobService;
  }
}

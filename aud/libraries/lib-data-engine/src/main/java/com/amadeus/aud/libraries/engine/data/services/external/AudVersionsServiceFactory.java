package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.LocalFileQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.StorageAccountQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.AudVersionsService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class AudVersionsServiceFactory {

  private final AudDataConfig audDataConfig;

  private final AudVersionsLocalFileService audVersionsLocalFileService;

  private final AudVersionsStorageAccountService audVersionsStorageAccountService;

  @Inject
  public AudVersionsServiceFactory(
      AudDataConfig audDataConfig,
      @LocalFileQualifier AudVersionsLocalFileService audVersionsLocalFileService,
      @StorageAccountQualifier AudVersionsStorageAccountService audVersionsStorageAccountService) {
    this.audDataConfig = audDataConfig;
    this.audVersionsLocalFileService = audVersionsLocalFileService;
    this.audVersionsStorageAccountService = audVersionsStorageAccountService;
  }

  @Produces
  @ApplicationScoped
  public AudVersionsService audVersionsService() {
    return audDataConfig.localFile().useLocalData()
        ? audVersionsLocalFileService
        : audVersionsStorageAccountService;
  }
}

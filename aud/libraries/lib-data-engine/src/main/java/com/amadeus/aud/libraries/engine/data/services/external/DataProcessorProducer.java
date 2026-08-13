package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.LocalFileQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.StorageAccountQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.DataProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class DataProcessorProducer {

  private final AudDataConfig audDataConfig;
  private final AudDataLocalFileService audDataLocalFileService;

  private final AudRequirementsLocalFileService audRequirementsLocalFileService;

  private final AudDataStorageAccountService audDataStorageAccountService;

  private final AudRequirementsStorageAccountService audRequirementsStorageAccountService;

  @Inject
  public DataProcessorProducer(
      AudDataConfig audDataConfig,
      @LocalFileQualifier AudDataLocalFileService audDataLocalFileService,
      @LocalFileQualifier AudRequirementsLocalFileService audRequirementsLocalFileService,
      @StorageAccountQualifier AudDataStorageAccountService audDataStorageAccountService,
      @StorageAccountQualifier
          AudRequirementsStorageAccountService audRequirementsStorageAccountService) {
    this.audDataConfig = audDataConfig;
    this.audDataLocalFileService = audDataLocalFileService;
    this.audRequirementsLocalFileService = audRequirementsLocalFileService;
    this.audDataStorageAccountService = audDataStorageAccountService;
    this.audRequirementsStorageAccountService = audRequirementsStorageAccountService;
  }

  @Produces
  @ApplicationScoped
  public DataProcessor<AudDataRepository> audDataDataProcessor() {
    return audDataConfig.localFile().useLocalData()
        ? audDataLocalFileService
        : audDataStorageAccountService;
  }

  @Produces
  @ApplicationScoped
  public DataProcessor<AudRequirementsRepository> audRequirementsDataProcessor() {
    return audDataConfig.localFile().useLocalData()
        ? audRequirementsLocalFileService
        : audRequirementsStorageAccountService;
  }
}

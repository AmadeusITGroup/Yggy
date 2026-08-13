package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudModelConfig;
import com.amadeus.aud.libraries.engine.data.configs.AudScopesConfig;
import com.amadeus.aud.libraries.engine.data.configs.YamlValidationConfig;
import com.amadeus.aud.libraries.engine.data.factories.BlobStrategyFactory;
import com.amadeus.aud.libraries.engine.data.factories.ConvertingStrategyFactory;
import com.amadeus.aud.libraries.engine.data.factories.ModelElementStrategyFactory;
import com.amadeus.aud.libraries.engine.data.factories.ModelElementToScopeConverterFactory;
import com.amadeus.aud.libraries.engine.data.interfaces.BlobFormat;
import com.amadeus.aud.libraries.engine.data.interfaces.ModelElementsProducerContext;
import com.amadeus.aud.libraries.engine.data.interfaces.ProducerStrategy;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.BlobResponseProducer;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.DTOProducer;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.ModelElementsProducer;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.contexts.ModelElementsBlobProducerContext;
import com.amadeus.aud.libraries.engine.data.producers.blobdata.contexts.ScopeProducerContext;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudRequirementsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.JsonService;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.ObjectMapperFactory;
import com.amadeus.aud.libraries.engine.data.services.external.parsing.YamlService;
import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.StorageAccountQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.BlobService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.DataProcessingService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountDataProcessor;
import com.amadeus.aud.libraries.models.core.db.Requirement;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.internals.Requirements;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.amadeus.aud.libraries.yaml.models.deserializers.KindBasedDeserializer;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class ScopeBlobService extends DataProcessingService
    implements StorageAccountDataProcessor<ScopesRepository> {

  private BlobResponseProducer<ModelElementsBlobProducerContext<String>> responseProducer;

  private ModelElementsProducer<ModelElement, ModelElementsProducerContext<V1Scope>>
      sopeModelElemensProducer;

  private DTOProducer<ModelElement, Scope, ScopeProducerContext> scopeConvertingProducer;

  private final YamlService yamlService;

  private final JsonService jsonService;

  private final AudRequirementsStorageAccountService audRequirementsStorageAccountService;

  AudScopesConfig audScopesConfig;
  // private final DTOProducer<> scopeConvertingProducer;

  AudModelConfig audModelConfig;

  AudDataConfig audDataConfig;

  @Inject
  public ScopeBlobService(
      AudScopesConfig audScopesConfig,
      AudDataConfig audDataConfig,
      AudModelConfig audModelConfig,
      YamlService yamlService,
      JsonService jsonService,
      @StorageAccountQualifier
          AudRequirementsStorageAccountService audRequirementsStorageAccountService) {
    this.audScopesConfig = audScopesConfig;
    this.audDataConfig = audDataConfig;
    this.audModelConfig = audModelConfig;
    this.yamlService = yamlService;
    this.jsonService = jsonService;
    this.audRequirementsStorageAccountService = audRequirementsStorageAccountService;
  }

  @PostConstruct
  public void postConstruct() {
    this.responseProducer = new BlobResponseProducer<>(new BlobStrategyFactory<>());
    this.sopeModelElemensProducer =
        new ModelElementsProducer<>(new ModelElementStrategyFactory<>());
    this.scopeConvertingProducer =
        new DTOProducer<>(
            new ConvertingStrategyFactory<>(), new ModelElementToScopeConverterFactory());

    ObjectMapper yamlMapper = yamlService.getMapper();
    YamlValidationConfig yamlValidationConfig = audModelConfig.yamlValidation();
    KindBasedDeserializer kindBasedDeserializer =
        new KindBasedDeserializer(
            new DefaultKindMapper(),
            ObjectMapperFactory.createYamlMapper(),
            yamlValidationConfig.enableWarning(),
            yamlValidationConfig.enableOnTheFlyFixes());

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Object.class, kindBasedDeserializer);
    yamlMapper.registerModule(module);
  }

  @PreDestroy
  void preDestroy() {
    if (responseProducer != null) {
      responseProducer.close();
    }
  }

  /**
   * Load data from a specific version.
   *
   * @param containerName The container name to load from
   * @param blobService The blob service to use for retrieving data
   * @param jsonPreferred
   * @return A Uni emitting the repository with the loaded data
   */
  @Override
  public Uni<ScopesRepository> load(
      String versionString,
      String containerName,
      @Nullable BlobService blobService,
      boolean jsonPreferred) {

    if (!audScopesConfig.enabled()) {
      return Uni.createFrom().item(new ScopesRepository());
    }

    ModelElementsBlobProducerContext<String> responseProducerContext =
        new ModelElementsBlobProducerContext<>(
            versionString,
            jsonPreferred ? BlobFormat.JSON : BlobFormat.YAML,
            audScopesConfig,
            audDataConfig,
            containerName,
            jsonPreferred ? ".*\\.json" : ".*\\.yml|.*\\.yaml");

    ProducerStrategy<String, ModelElementsBlobProducerContext<String>> blobResponsesStrategy =
        responseProducer.getStrategyFactory().newStrategy();

    Uni<Collection<String>> responses =
        responseProducer.prepareProduceExecutable(responseProducerContext);

    Uni<ScopesRepository> uniScopesRepository =
        blobResponsesStrategy
            .withStrategy(responses, responseProducerContext)
            .onItem()
            .transform(
                Unchecked.function(
                    (Collection<String> strings) ->
                        processResponses(
                            versionString,
                            strings.toArray(new String[strings.size()]),
                            jsonPreferred)));

    if (responseProducerContext.getAudScopesConfig().associateScopeWithRequirements()
        && blobService != null) {
      Uni<AudRequirementsRepository> uniRequirementsRepository =
          audRequirementsStorageAccountService.load(
              versionString,
              audDataConfig.cloudStorage().requirementsContainerName(),
              blobService,
              jsonPreferred);

      uniScopesRepository =
          Uni.combine()
              .all()
              .unis(uniScopesRepository, uniRequirementsRepository)
              .asTuple()
              .onItem()
              .transformToUni(
                  Unchecked.function(
                      (Tuple2<ScopesRepository, AudRequirementsRepository>
                              scopesRepositoryAndRequirementsRepository) -> {
                        associateScopesWithRequirements(
                            scopesRepositoryAndRequirementsRepository.getItem1(),
                            scopesRepositoryAndRequirementsRepository.getItem2());
                        return Uni.createFrom()
                            .item(scopesRepositoryAndRequirementsRepository.getItem1());
                      }));
    }
    return uniScopesRepository;
  }

  /**
   * Process the response and store it in the repository.
   *
   * @param versionString The version identifier
   * @param responses The response strings to process
   * @param jsonPreferred
   * @return The repository containing the processed data
   * @throws IOException If an error occurs during processing
   */
  public ScopesRepository processResponses(
      String versionString, String[] responses, boolean jsonPreferred) throws IOException {

    ModelElementsProducerContext<V1Scope> modelElementProducerContext =
        new ModelElementsBlobProducerContext<V1Scope>(
            versionString,
            jsonPreferred ? BlobFormat.JSON : BlobFormat.YAML,
            audScopesConfig,
            audDataConfig,
            jsonPreferred ? jsonService : yamlService,
            V1Scope.class,
            Set.of(responses));

    ScopesRepository repository = new ScopesRepository();

    try {
      Uni<Collection<ModelElement>> scopeModelElementUni =
          sopeModelElemensProducer.prepareProduceExecutable(modelElementProducerContext);

      ScopeProducerContext scopeDtoProducerContext =
          new ScopeProducerContext(modelElementProducerContext);

      Collection<Scope> scopes =
          scopeConvertingProducer.produceContinueFromUni(
              scopeDtoProducerContext, scopeModelElementUni);

      repository.setDataVersion(versionString);
      repository.setData(new ConcurrentHashMap<>());

      calculateHashes(repository, responses);

      for (Scope scope : scopes) {
        repository.getData().put(scope.id(), scope);
      }
    } catch (Exception e) {
      throw new IOException(e);
    }

    return repository;
  }

  /**
   * <b>IMPORTANT</b>: It looks like the core-data/api.yaml file already contains the enhanced scope
   * information. For the moment we are keeping the same logic that was used in the python version
   * of the code, but perhaps we should automatically instantiate the Scope object from the first
   * pass over the core-data repository. To load the scopes automatically from the yaml file, we
   * would need to update the {@link Scope} class to extend the {@link V1Scope} class and add the
   * following fields:
   *
   * <ul>
   *   <li>{@code repository_url}
   *   <li>{@code repository_files}
   *   <li>{@code repository_version}
   * </ul>
   *
   * <p>{@code repository_url} would be the URL of the core scope repository {@code
   * repository_files} would be the list of files {@code repository_version} would be the version of
   * the repository In this way, we do not really need to associate the scopes with the requirement
   * object.
   */
  void associateScopesWithRequirements(
      ScopesRepository scopeRepository, AudRequirementsRepository repository) {
    Log.debugf("Associating scopes with core-requirements...");

    Map<String, Scope> scopeMap =
        Objects.requireNonNullElseGet(
            scopeRepository.getData(),
            () -> {
              scopeRepository.setData(new ConcurrentHashMap<>());
              return scopeRepository.getData();
            });

    Objects.requireNonNullElseGet(
            repository.getData(),
            () -> {
              repository.setData(new Requirements(List.of()));
              return repository.getData();
            })
        .requirements()
        .forEach(
            (Requirement requirement) -> {
              String uniqueId = AbstractBase.generateUniqueId(requirement.getScope());
              Scope scope = scopeMap.get(uniqueId);
              if (scope != null) {
                scope.setRequirement(requirement);
              } else {
                Log.warnf("Scope %s not found in core-data repository", uniqueId);
              }
            });
    Log.debugf("Completed associating Scopes with core-requirements");
  }
}

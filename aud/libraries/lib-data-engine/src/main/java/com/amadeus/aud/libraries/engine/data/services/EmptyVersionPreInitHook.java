package com.amadeus.aud.libraries.engine.data.services;

import com.amadeus.aud.libraries.engine.data.configs.AudScopesConfig;
import com.amadeus.aud.libraries.engine.data.factories.ScopesServiceFactory;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountDataProcessor;
import com.amadeus.aud.libraries.engine.data.unistrategies.BaseProducerStrategy;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;

@ApplicationScoped
public class EmptyVersionPreInitHook implements VersionPreInitHook {
  private final ScopesServiceFactory scopesServiceFactory;
  private final AudScopesConfig audScopesConfig;

  @Inject
  public EmptyVersionPreInitHook(
      ScopesServiceFactory scopesServiceFactory, AudScopesConfig audScopesConfig) {
    this.scopesServiceFactory = scopesServiceFactory;
    this.audScopesConfig = audScopesConfig;
  }

  @Override
  public Uni<Void> run(Version version, boolean associateWithRequirement) {
    String localBlobDirectory = audScopesConfig.localScopesDirectory();
    StorageAccountDataProcessor<ScopesRepository> amadeusScopeService =
        scopesServiceFactory.newScopesService();
    Duration maxWait = audScopesConfig.maxLoadDuration();

    return BaseProducerStrategy.withFailure(
            amadeusScopeService.load(version.getVersionNumber(), localBlobDirectory, null, false),
            new ScopesRepository())
        .ifNoItem()
        .after(maxWait)
        .fail()
        .invoke(
            (ScopesRepository repository) -> {
              repository.autoCreateScope();
              version.setScopesRepository(repository);
            })
        .replaceWithVoid();
  }
}

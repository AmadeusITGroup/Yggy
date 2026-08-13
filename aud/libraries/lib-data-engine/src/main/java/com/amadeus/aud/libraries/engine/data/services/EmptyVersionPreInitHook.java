package com.amadeus.aud.libraries.engine.data.services;

import com.amadeus.aud.libraries.engine.data.factories.ScopesServiceFactory;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.storage.StorageAccountDataProcessor;
import com.amadeus.aud.libraries.engine.data.unistrategies.BaseProducerStrategy;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Optional;
import org.eclipse.microprofile.config.ConfigProvider;

@ApplicationScoped
public class EmptyVersionPreInitHook implements VersionPreInitHook {

  @Inject ScopesServiceFactory scopesServiceFactory;

  @Override
  public Uni<Void> run(Version version, boolean associateWithRequirement) {

    Optional<String> localBlobDirectory =
        ConfigProvider.getConfig().getOptionalValue("quarkus.file.storage.directory", String.class);
    StorageAccountDataProcessor<ScopesRepository> amadeusScopeService =
        scopesServiceFactory.newScopesService();

    Optional<Integer> maxWait =
        ConfigProvider.getConfig().getOptionalValue("aud-scopes.max-load-duration", Integer.class);

    Uni<ScopesRepository> uni =
        BaseProducerStrategy.withFailure(
            amadeusScopeService.load(
                version.getVersionNumber(), localBlobDirectory.orElse("."), null, false),
            new ScopesRepository());
    ScopesRepository repository =
        uni.await().atMost(Duration.ofSeconds(maxWait.orElse(Integer.valueOf(1200))));
    repository.autoCreateScope();
    version.setScopesRepository(repository);

    return Uni.createFrom().voidItem();
  }
}

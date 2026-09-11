package com.amadeus.aud.libraries.engine.data.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.configs.AudScopesConfig;
import com.amadeus.aud.libraries.engine.data.factories.ScopesServiceFactory;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.services.external.ScopeBlobService;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class EmptyVersionPreInitHookTest {
  @Test
  void runDefersLoadingUntilSubscription() {
    ScopesServiceFactory scopesServiceFactory = mock(ScopesServiceFactory.class);
    AudScopesConfig audScopesConfig = mock(AudScopesConfig.class);
    ScopeBlobService scopesService = mock(ScopeBlobService.class);
    Version version = mock(Version.class);
    ScopesRepository repository = mock(ScopesRepository.class);
    AtomicBoolean subscribed = new AtomicBoolean();
    when(scopesServiceFactory.newScopesService()).thenReturn(scopesService);
    when(audScopesConfig.localScopesDirectory()).thenReturn("/scopes");
    when(audScopesConfig.maxLoadDuration()).thenReturn(Duration.ofMillis(100));
    when(version.getVersionNumber()).thenReturn("1.0.0");
    when(scopesService.load("1.0.0", "/scopes", null, false))
        .thenReturn(
            Uni.createFrom()
                .deferred(
                    () -> {
                      subscribed.set(true);
                      return Uni.createFrom().item(repository);
                    }));
    EmptyVersionPreInitHook hook =
        new EmptyVersionPreInitHook(scopesServiceFactory, audScopesConfig);

    Uni<Void> result = hook.run(version, false);

    assertThat(subscribed).isFalse();
    verify(repository, never()).autoCreateScope();
    verify(version, never()).setScopesRepository(repository);

    result.await().atMost(Duration.ofMillis(200));

    assertThat(subscribed).isTrue();
    verify(repository).autoCreateScope();
    verify(version).setScopesRepository(repository);
  }
}

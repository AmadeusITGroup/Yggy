package com.amadeus.aud.services.apis.schedulers;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class VersionScheduler {
  private final AudService audService;

  @Inject
  public VersionScheduler(AudService audService) {
    this.audService = audService;
  }

  @Scheduled(every = "3m", delay = 5, delayUnit = TimeUnit.MINUTES)
  //    @Scheduled(cron = "{cron.expr}")
  void checkNewVersion() {
    Log.debugf("Checking if there is a new version of the requirements.");

    audService.checkForNewVersion().subscribe().with((Version newVersion) -> {});
  }
}

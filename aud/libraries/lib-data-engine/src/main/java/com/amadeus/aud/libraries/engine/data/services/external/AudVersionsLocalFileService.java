package com.amadeus.aud.libraries.engine.data.services.external;

import com.amadeus.aud.libraries.engine.data.services.external.qualifiers.LocalFileQualifier;
import com.amadeus.aud.libraries.engine.data.services.external.storage.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.services.external.storage.LocalFileService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.utils.VersionUtils;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@LocalFileQualifier
@ApplicationScoped
public class AudVersionsLocalFileService implements AudVersionsService {
  private static final Logger Log = Logger.getLogger(AudVersionsLocalFileService.class);
  private final LocalFileService localFileService;

  @Inject
  public AudVersionsLocalFileService(LocalFileService localFileService) {
    this.localFileService = localFileService;
  }

  @Override
  @LogExecutionTime
  public Uni<String> checkNewVersion(@Nullable String lastVersionNumber) {
    return localFileService
        .findLatestVersion()
        .onItem()
        .transformToUni(
            (String version) -> {
              if (lastVersionNumber == null
                  || VersionUtils.toLong(version) > VersionUtils.toLong(lastVersionNumber)) {
                Log.infof("New version found: %s", version);
                return Uni.createFrom().item(version);
              } else {
                Log.infof("No new version found. Latest is %s", version);
                return Uni.createFrom().nullItem();
              }
            })
        .onFailure()
        .invoke(
            e -> Log.error("Failed to get latest version from the Local File System. Error:", e));
  }

  @Override
  public Uni<
          java.util.List<
              com.amadeus.aud.libraries.engine.data.services.external.storage.SingleVersion>>
      listAllVersions() {
    return Uni.createFrom()
        .item(
            () ->
                io.smallrye.mutiny.unchecked.Unchecked.supplier(localFileService::findAllVersions)
                    .get())
        .runSubscriptionOn(io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transform(
            versions ->
                versions.stream()
                    .map(
                        v ->
                            new com.amadeus.aud.libraries.engine.data.services.external.storage
                                .SingleVersion(v, null, 0))
                    .toList())
        .onFailure()
        .invoke(e -> Log.error("Failed to list versions from the Local File System. Error:", e));
  }

  @Override
  public Uni<String> getLatestVersion() {
    return localFileService.findLatestVersion();
  }
}

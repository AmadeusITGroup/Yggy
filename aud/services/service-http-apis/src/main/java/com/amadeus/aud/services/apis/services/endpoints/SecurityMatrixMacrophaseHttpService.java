package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.yaml.models.SecurityMatrixMacrophaseResponse;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
import com.amadeus.aud.services.apis.models.core.matrix.SecurityMatrixMacrophase;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class SecurityMatrixMacrophaseHttpService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public SecurityMatrixMacrophaseHttpService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getSecurityMatrixMacrophases(String version, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getSecurityMatrixMacrophases(ver, uriInfo));
  }

  @CacheResult(
      cacheName = "getSecurityMatrixMacrophases",
      keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getSecurityMatrixMacrophases(Version version, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getSecurityMatrixMacrophasesResponse(version, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private Response getSecurityMatrixMacrophasesResponse(Version version, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();
    MacroPhasesRepository macroPhasesRepository = version.getMacroPhasesRepository();
    checkRepository(macroPhasesRepository, versionNumber, "Macro Phases");

    return getOkResponse(
        new SecurityMatrixMacrophaseResponse()
            .matrices(buildMatrix(macroPhasesRepository.getData()))
            .url(UrlUtils.getUrl(uriInfo))
            .urlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber))
            .version(versionNumber)
            .apiVersion(audApiConfig.version()));
  }

  public SecurityMatrixMacrophase buildMatrix(Map<String, MacroPhase> macroPhases) {
    SecurityMatrixMacrophase matrix = new SecurityMatrixMacrophase(new HashMap<>());

    for (MacroPhase source : macroPhases.values()) {
      for (MacroPhase target : macroPhases.values()) {
        addDefaultControl(matrix, source, target);
        updateOverrideControl(matrix, source, target);
      }
    }

    return matrix;
  }

  private void addDefaultControl(
      SecurityMatrixMacrophase matrix, MacroPhase source, MacroPhase target) {
    matrix.addPhaseControl(
        source.getName(), target.getName(), V1ConnectionPhaseSpec.ControlEnum.RED);
  }

  private void updateOverrideControl(
      SecurityMatrixMacrophase matrix, MacroPhase source, MacroPhase target) {
    for (ConnectionPhase phase : source.getConnectionPhases()) {
      if (Objects.equals(phase.getSourcePhase(), source)
          && Objects.equals(phase.getDestinationPhase(), target)) {
        matrix.addPhaseControl(source.getName(), target.getName(), phase.getControl());
        break;
      }
    }
  }
}

package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.services.apis.models.http.responses.scopeversions.ScopeVersionsResponse;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class ScopeVersionsService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public ScopeVersionsService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getScopeVersions(String dataVersion, String scope, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getScopeVersions(version, scope, uriInfo));
  }

  public Uni<Response> getScopeVersions(Version version, String scope, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getScopeVersionsResponse(version, scope, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private Response getScopeVersionsResponse(Version version, String scope, UriInfo uriInfo)
      throws JsonProcessingException {
    String versionNumber = version.getVersionNumber();

    ScopesRepository scopesRepository = version.getScopesRepository();
    checkRepository(scopesRepository, version.getVersionNumber(), "Scopes");

    Map<String, String> scopeVersions = new TreeMap<>();
    ConcurrentMap<String, Scope> data = scopesRepository.getData();

    if (scope != null) {
      Scope dataScope = data.get(scope);
      if (dataScope != null && dataScope.getRequirement() != null) {
        scopeVersions.put(scope, dataScope.getRequirement().getVersion());
      }
    } else {
      for (Entry<String, Scope> scopeEntry : data.entrySet()) {
        Scope scopeFromEntry = scopeEntry.getValue();
        if (scopeFromEntry.getRequirement() != null) {
          scopeVersions.put(scopeEntry.getKey(), scopeFromEntry.getRequirement().getVersion());
        }
      }
    }

    String url = UrlUtils.getUrl(uriInfo);

    return getOkResponse(
        new ScopeVersionsResponse(url, url, versionNumber, audApiConfig.version(), scopeVersions));
  }
}

package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.GraphResponse;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.services.storage.GraphEngineService;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class GraphService extends BaseService {
  private final GraphEngineService graphEngineService;
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public GraphService(
      GraphEngineService graphEngineService, AudService audService, AudApiConfig audApiConfig) {
    this.graphEngineService = graphEngineService;
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getGraph(UriInfo uriInfo) {
    return getGraph(null, uriInfo);
  }

  public Uni<Response> getGraph(@Nullable String version, UriInfo uriInfo) {
    Uni<Version> versionUni;
    if (version == null) {
      versionUni = audService.getCurrentVersion();
    } else {
      versionUni = audService.getVersion(version);
    }

    return versionUni
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transform(
            Unchecked.function(
                (Version versionObj) -> {
                  if (versionObj == null) {
                    if (version == null) {
                      Log.error("No latest version found.");
                      throw new InternalServerErrorException("No latest version found.");
                    } else {
                      Log.infof("Version %s not found.", version);
                      throw new NotFoundException("Version not found: " + version);
                    }
                  }

                  String versionNumber = versionObj.getVersionNumber();

                  List<Edge> edges =
                      graphEngineService.getAllEdgesByNodeIds(versionNumber, List.of(), List.of());

                  Set<String> nodeIds = new LinkedHashSet<>();
                  List<String> edgeNames = new ArrayList<>();
                  for (Edge edge : edges) {
                    nodeIds.add(edge.getSourceNodeId());
                    nodeIds.add(edge.getTargetNodeId());
                    edgeNames.add(edge.getName());
                  }

                  return Response.ok(
                          new GraphResponse(
                              UrlUtils.getUrl(uriInfo),
                              UrlUtils.getVersionedUrl(uriInfo, versionNumber),
                              versionNumber,
                              audApiConfig.version(),
                              new ArrayList<>(nodeIds),
                              edgeNames))
                      .build();
                }));
  }
}

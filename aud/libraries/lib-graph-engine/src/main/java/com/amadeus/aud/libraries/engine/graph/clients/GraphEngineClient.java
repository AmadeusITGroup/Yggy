package com.amadeus.aud.libraries.engine.graph.clients;

import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Graph;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1")
@RegisterRestClient(configKey = "graph-engine")
@OidcClientFilter(value = "graph-engine")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GraphEngineClient {

  @POST
  @Path("/versions/{version}/graph")
  Uni<Response> init(@PathParam("version") String version);

  @GET
  @Path("/versions/{version}/graph/init/status")
  Uni<Response> getGraphInitStatus(@PathParam("version") String version);

  @GET
  @Path("/versions/{version}/graph")
  Uni<Graph> getGraph(@PathParam("version") String version);

  @GET
  @Path("/versions/{version}/graph/nodes")
  Uni<List<Node>> getGraphNodes(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      @QueryParam("landingZone") List<String> landingZones,
      @QueryParam("platform") List<String> platforms,
      @QueryParam("target") List<String> targets);

  @GET
  @Path("/versions/{version}/graph/edges")
  Uni<List<Edge>> getGraphEdges(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      @QueryParam("landingZone") List<String> landingZones,
      @QueryParam("platform") List<String> platforms,
      @QueryParam("target") List<String> targets,
      @QueryParam("direction") @DefaultValue("ANY") EdgeDirection direction);

  @POST
  @Path("/versions/{version}/graph/edges/by-node-ids")
  Uni<List<Edge>> getGraphEdgesByNodeIds(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      EdgesByNodeIdsRequest request);

  @GET
  @Path("/versions/{version}/graph/edges/by-scope")
  Uni<List<Edge>> getGraphEdgesByScope(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      @QueryParam("scope") String scope);

  @GET
  @Path("/versions/{version}/graph/statistics")
  Uni<GraphStatistics> getGraphStatistics(@PathParam("version") String version);

  @GET
  @Path("/versions")
  Uni<List<String>> getAvailableVersions();
}

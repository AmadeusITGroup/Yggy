package com.amadeus.aud.services.engine.resources;

import com.amadeus.aud.libraries.engine.graph.clients.EdgesByNodeIdsRequest;
import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import com.amadeus.aud.libraries.engine.graph.models.dto.Edge;
import com.amadeus.aud.libraries.engine.graph.models.dto.Graph;
import com.amadeus.aud.libraries.engine.graph.models.dto.Node;
import com.amadeus.aud.libraries.engine.graph.models.filters.EdgeDirection;
import com.amadeus.aud.services.engine.resources.models.GraphInitStatusResponse;
import com.amadeus.aud.services.engine.services.resources.GraphResourceService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Graph API", description = "Graph processing and management operations")
public class GraphResource {

  @Inject GraphResourceService graphService;

  @POST
  @Path("/versions/{version}/graph")
  @Operation(summary = "Queue graph initialization for a specific version")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "202",
            description = "Graph initialization request accepted",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphInitStatusResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "queued",
                          summary = "Initialization queued",
                          value =
                              "{\"version\":\"v1\",\"status\":\"QUEUED\",\"message\":\"Graph initialization has been queued\"}"),
                      @ExampleObject(
                          name = "alreadyInProgress",
                          summary = "Initialization already in progress",
                          value =
                              "{\"version\":\"v1\",\"status\":\"IN_PROGRESS\",\"message\":\"Graph initialization is already in progress\"}")
                    })),
        @APIResponse(
            responseCode = "200",
            description = "Graph already initialized",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphInitStatusResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "alreadyReady",
                          summary = "Initialization not needed because graph is ready",
                          value =
                              "{\"version\":\"v1\",\"status\":\"READY\",\"message\":\"Graph is already initialized\"}")
                    })),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public Response initGraph(@PathParam("version") String versionString) {
    return graphService.initGraphAsync(versionString);
  }

  @GET
  @Path("/versions/{version}/graph/init/status")
  @Operation(summary = "Get graph initialization status for a specific version")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Graph is ready",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphInitStatusResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "ready",
                          summary = "Initialization finished",
                          value = "{\"version\":\"v1\",\"status\":\"READY\"}")
                    })),
        @APIResponse(
            responseCode = "202",
            description = "Graph initialization is in progress",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphInitStatusResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "inProgress",
                          summary = "Initialization is running",
                          value = "{\"version\":\"v1\",\"status\":\"IN_PROGRESS\"}")
                    })),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(
            responseCode = "404",
            description = "Graph version is not initialized",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphInitStatusResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "notReady",
                          summary = "Initialization has not been started",
                          value =
                              "{\"version\":\"v1\",\"status\":\"NOT_READY\",\"message\":\"Graph is not initialized for this version\"}")
                    })),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public Response getGraphInitStatus(@PathParam("version") String versionString) {
    return graphService.getInitStatus(versionString);
  }

  @GET
  @Path("/versions/{version}/graph")
  @Operation(summary = "Get graph for a specific version")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Graph retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Graph.class))),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public Graph getGraph(@PathParam("version") String versionString) {
    return graphService.getGraph(versionString);
  }

  @GET
  @Path("/versions/{version}/graph/nodes")
  @Operation(
      summary = "Get nodes for a specific graph version with pagination and optional filtering",
      description =
          """
        Retrieves nodes from a graph with optional filtering by landing zone. \
        If no filters are provided, returns all nodes.""")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "200", description = "Nodes retrieved successfully"),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public List<Node> getGraphNodes(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      @QueryParam("landingZone") List<String> landingZones,
      @QueryParam("platform") List<String> platforms,
      @QueryParam("target") List<String> targets) {
    return graphService.getGraphNodes(versionString, landingZones, platforms, targets, page, size);
  }

  @GET
  @Path("/versions/{version}/graph/edges")
  @Operation(summary = "Get edges for a specific graph version with pagination")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "200", description = "Edges retrieved successfully"),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public List<Edge> getGraphEdges(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      @QueryParam("landingZone") List<String> landingZones,
      @QueryParam("platform") List<String> platforms,
      @QueryParam("target") List<String> targets,
      @QueryParam("direction") @DefaultValue("ANY") EdgeDirection direction) {
    return graphService.getGraphEdges(
        versionString, landingZones, platforms, targets, direction, page, size);
  }

  @GET
  @Path("/versions/{version}/graph/edges/by-scope")
  @Operation(
      summary = "Get edges for a specific scope",
      description =
          """
      Optimized endpoint for connectivity queries. \
      Returns edges where source contains the scope \
      or destination contains the scope.""")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "200", description = "Edges retrieved successfully"),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public List<Edge> getGraphEdgesByScope(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      @Parameter(required = true, description = "Scope used to filter graph edges")
          @QueryParam("scope")
          @NotBlank(message = "query parameter 'scope' is required")
          String scope) {
    return graphService.getGraphEdgesByScope(versionString, scope, page, size);
  }

  @POST
  @Path("/versions/{version}/graph/edges/by-node-ids")
  @Operation(
      summary = "Get edges connecting specific source and destination node IDs",
      description =
          """
                Optimized endpoint for connectivity queries. \
                Returns edges where source is in sourceNodeIds \
                and destination is in destinationNodeIds.""")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "200", description = "Edges retrieved successfully"),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public List<Edge> getGraphEdgesByNodeIds(
      @PathParam("version") String versionString,
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("100") int size,
      EdgesByNodeIdsRequest request) {
    List<String> sourceNodeIds = request != null ? request.getSourceNodeIds() : null;
    List<String> destinationNodeIds = request != null ? request.getDestinationNodeIds() : null;
    return graphService.getGraphEdgesByNodeIds(
        versionString, sourceNodeIds, destinationNodeIds, page, size);
  }

  @GET
  @Path("/versions/{version}/graph/statistics")
  @Operation(
      summary = "Get pre-computed graph statistics",
      description =
          """
      Returns statistics (links/nodes counts per scope) computed at graph initialization \
      time. Since the graph is immutable, these statistics are always up-to-date.""")
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GraphStatistics.class))),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public GraphStatistics getGraphStatistics(@PathParam("version") String versionString) {
    return graphService.getGraphStatistics(versionString);
  }

  @GET
  @Path("/versions")
  @Operation(summary = "Get all available graph versions")
  @APIResponses(
      value = {
        @APIResponse(responseCode = "200", description = "List of available versions"),
        @APIResponse(responseCode = "400", description = "Bad request"),
        @APIResponse(responseCode = "401", description = "Unauthorized"),
        @APIResponse(responseCode = "403", description = "Forbidden"),
        @APIResponse(responseCode = "404", description = "Not found"),
        @APIResponse(responseCode = "500", description = "Internal server error")
      })
  public List<String> getAvailableVersions() {
    return graphService.getAllVersions();
  }
}

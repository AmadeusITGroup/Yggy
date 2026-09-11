package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.services.apis.services.endpoints.GraphService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class GraphResource {
  private final GraphService graphService;

  @Context UriInfo uriInfo;

  @Inject
  public GraphResource(GraphService graphService) {
    this.graphService = graphService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dump of the graph")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "array of node + array of edges",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Object.class))),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Path("graphdump")
  public Uni<Response> getGraph() {
    return graphService.getGraph(uriInfo);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Dump of the graph (with version)")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "array of node + array of edges",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Object.class))),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      @Parameter(
          name = "version",
          description = "The requirements version",
          required = true,
          in = ParameterIn.PATH,
          schema = @Schema(type = SchemaType.STRING)))
  @Path("version/{version}/graphdump")
  public Uni<Response> getGraphWithVersion(@PathParam("version") String version) {
    return graphService.getGraph(version, uriInfo);
  }
}

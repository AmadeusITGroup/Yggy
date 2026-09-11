package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.ScopeDiffResponse;
import com.amadeus.aud.services.apis.services.endpoints.ScopeDiffService;
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
public class ScopeDiffResource {
  private final ScopeDiffService scopediffService;

  @Context UriInfo uriInfo;

  @Inject
  public ScopeDiffResource(ScopeDiffService scopediffService) {
    this.scopediffService = scopediffService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get Scope diff between specific versions",
      description = "Retrieves the Scope diff between specific versions.")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Scopediff retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ScopeDiffResponse.class))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      value = {
        @Parameter(
            name = "oldVersion",
            description = "The old version for comparison",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "newVersion",
            description = "The new version for comparison",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
      })
  @Path("scopediff/{oldVersion}/{newVersion}")
  public Uni<Response> getScopediffBetweenVersions(
      @PathParam("oldVersion") String oldVersion, @PathParam("newVersion") String newVersion) {
    return scopediffService.getScopeVersionsDiff(oldVersion, newVersion, uriInfo);
  }
}

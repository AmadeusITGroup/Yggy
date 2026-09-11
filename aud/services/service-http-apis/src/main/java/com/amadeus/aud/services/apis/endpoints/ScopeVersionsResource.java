package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.services.apis.models.http.responses.scopeversions.ScopeVersionsResponse;
import com.amadeus.aud.services.apis.services.endpoints.ScopeVersionsService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
public class ScopeVersionsResource {
  private final ScopeVersionsService scopeVersionsService;

  @Context UriInfo uriInfo;

  @Inject
  public ScopeVersionsResource(ScopeVersionsService scopeVersionsService) {
    this.scopeVersionsService = scopeVersionsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get Scope Versions for specific version",
      description = "Retrieves the Scope Versions for specific version.")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Scope Versions retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ScopeVersionsResponse.class))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      value = {
        @Parameter(
            name = "version",
            description = "The requirements version",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "scope",
            description = "The scope to filter",
            required = false,
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING))
      })
  @Path("version/{version}/scope-versions")
  public Uni<Response> getScopeVersionsWithVersion(
      @PathParam("version") String version, @QueryParam("scope") String scope) {
    return scopeVersionsService.getScopeVersions(version, scope, uriInfo);
  }
}

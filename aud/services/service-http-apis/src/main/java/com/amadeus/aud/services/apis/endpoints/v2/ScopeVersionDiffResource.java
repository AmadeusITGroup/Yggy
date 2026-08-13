package com.amadeus.aud.services.apis.endpoints.v2;

import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem;
import com.amadeus.aud.libraries.yaml.models.Error;
import com.amadeus.aud.services.apis.services.endpoints.v2.ScopeVersionDiffService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
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
public class ScopeVersionDiffResource {
  private final ScopeVersionDiffService scopeVersionDiffService;

  @Inject
  public ScopeVersionDiffResource(ScopeVersionDiffService scopeVersionDiffService) {
    this.scopeVersionDiffService = scopeVersionDiffService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List scope differences between two data versions")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Scope version differences",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(
                            type = SchemaType.OBJECT,
                            properties = {
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "data",
                                  type = SchemaType.ARRAY,
                                  implementation = ScopeVersionDiffItem.class),
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "metadata",
                                  implementation = Metadata.class)
                            }))),
        @APIResponse(
            responseCode = "400",
            description =
                "Bad request - missing version query parameters or invalid pagination parameters",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Error.class),
                    example =
                        """
          {
              "code": 400,
              "message": "oldVersion query parameter is required"
          }
          """)),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      value = {
        @Parameter(
            name = "oldVersion",
            description = "The older data version number",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "newVersion",
            description = "The newer data version number",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "pageNum",
            description = "Page number, starting at 1",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER)),
        @Parameter(
            name = "pageSize",
            description = "Number of items per page",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER))
      })
  @Path("v2/scope-diffs")
  public Uni<jakarta.ws.rs.core.Response> getScopeVersionDiff(
      @QueryParam("oldVersion") String oldVersion,
      @QueryParam("newVersion") String newVersion,
      @QueryParam("pageNum") Integer pageNum,
      @QueryParam("pageSize") Integer pageSize) {
    return scopeVersionDiffService
        .getScopeVersionDiffResponse(oldVersion, newVersion, pageNum, pageSize)
        .onItem()
        .transform(response -> jakarta.ws.rs.core.Response.ok(response).build());
  }
}

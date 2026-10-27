package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.services.apis.models.http.responses.GenericResponse;
import com.amadeus.aud.services.apis.services.endpoints.FullListService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
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
public class FullListResource {
  private final FullListService fullListService;

  @Context UriInfo uriInfo;

  @Inject
  public FullListResource(FullListService fullListService) {
    this.fullListService = fullListService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary =
          "List of a given object type with all properties for a given version. ONLY for SDN.",
      deprecated = true)
  @Tags(value = @Tag(name = "List"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "The full list for that object type for that version",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class))),
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
            name = "objectType",
            description = "The object type to get the full list of",
            required = true,
            schema = @Schema(type = SchemaType.STRING))
      })
  @Path("version/{version}/fulllist/{objectType}")
  public Uni<Response> getFullListJavaFormatWithVersion(
      @PathParam("version") String version, @PathParam("objectType") String objectType) {
    return fullListService.getFullListJavaFormat(version, objectType, uriInfo);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary =
          "List of a given object type with all properties for a given version. ONLY for E2E, SNOW and SIGHT.",
      deprecated = true)
  @Tags(value = @Tag(name = "List"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "The full list for that object type for that version",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GenericResponse.class))),
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
            name = "objectType",
            description = "The object type to get the full list of",
            required = true,
            schema = @Schema(type = SchemaType.STRING))
      })
  @Path("version/{version}/v1/fulllist/{objectType}")
  public Uni<Response> getFullListPythonFormatWithVersion(
      @PathParam("version") String version, @PathParam("objectType") String objectType) {
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    return fullListService.getFullListPythonFormat(version, objectType, uriInfo, queryParams);
  }
}

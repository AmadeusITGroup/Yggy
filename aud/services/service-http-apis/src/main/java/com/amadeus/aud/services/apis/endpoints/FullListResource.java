package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.yaml.models.Error;
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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
@APIResponse(
    responseCode = "500",
    description = "Internal server error",
    content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Error.class),
            example =
                """
        {
            "code": 500,
            "message": "Internal server error"
        }
        """))
@APIResponse(
    responseCode = "401",
    description = "Unauthorized Error",
    headers = {
      @Header(
          name = "WWW-Authenticate",
          description = "The authentication method that should be used to gain access",
          schema =
              @Schema(
                  implementation = String.class,
                  defaultValue = "Bearer realm=\"Access to AUD API\""))
    },
    content =
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = Error.class),
            example =
                """
        {
            "code": 401,
            "message": "User is not authorized to access this resource"
        }
        """))
public class FullListResource {
  private final FullListService fullListService;

  @Context UriInfo uriInfo;

  @Inject
  public FullListResource(FullListService fullListService) {
    this.fullListService = fullListService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of a given object type with all properties")
  @Tags(value = @Tag(name = "List"))
  @APIResponse(
      responseCode = "200",
      description = "The full list for that object type",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = GenericResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "Object type not found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Map.class),
              example =
                  """
                  {
                      "code": 404,
                      "message": "Object type not found"
                  }
                """))
  @Parameters(
      @Parameter(
          name = "objectType",
          description = "The object type to get the full list of",
          required = true,
          schema = @Schema(type = SchemaType.STRING)))
  @Path("fulllist/{objectType}")
  public Uni<Response> getFullListPythonFormat(@PathParam("objectType") String objectType) {
    // DO NOT REMOVE Python format while SDN migrate
    // MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    // return fullListService
    //     .getFullList(Constants.LATEST, objectType, uriInfo)
    //     .map(response -> fullListService.applyQueryParamFilter(response, objectType,
    // queryParams));
    return fullListService.getFullListJavaFormat(Constants.LATEST, objectType, uriInfo);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of a given object type with all properties")
  @Tags(value = @Tag(name = "List"))
  @APIResponse(
      responseCode = "200",
      description = "The full list for that object type",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = GenericResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "Object type not found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Map.class),
              example =
                  """
                  {
                      "code": 404,
                      "message": "Object type not found"
                  }
                """))
  @Parameters(
      @Parameter(
          name = "objectType",
          description = "The object type to get the full list of",
          required = true,
          schema = @Schema(type = SchemaType.STRING)))
  @Path("v1/fulllist/{objectType}")
  public Uni<Response> getFullListJavaFormat(@PathParam("objectType") String objectType) {
    return fullListService.getFullListJavaFormat(Constants.LATEST, objectType, uriInfo);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of a given object type with all properties for a given version")
  @Tags(value = @Tag(name = "List"))
  @APIResponse(
      responseCode = "200",
      description = "The full list for that object type for that version",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = GenericResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "Object type not found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Map.class),
              example =
                  """
                {
                    "code": 404,
                    "message": "Object type not found"
                }
                """))
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
  public Uni<Response> getFullListPythonFormatWithVersion(
      @PathParam("version") String version, @PathParam("objectType") String objectType) {
    // DO NOT REMOVE while SDN migrate
    // MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    // return fullListService
    //     .getFullList(version, objectType, uriInfo)
    //     .map(response -> fullListService.applyQueryParamFilter(response, objectType,
    // queryParams));
    return fullListService.getFullListJavaFormat(version, objectType, uriInfo);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of a given object type with all properties for a given version")
  @Tags(value = @Tag(name = "List"))
  @APIResponse(
      responseCode = "200",
      description = "The full list for that object type for that version",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = GenericResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "Object type not found",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Map.class),
              example =
                  """
                {
                    "code": 404,
                    "message": "Object type not found"
                }
                """))
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
  public Uni<Response> getFullListJavaFormatWithVersion(
      @PathParam("version") String version, @PathParam("objectType") String objectType) {
    return fullListService.getFullListJavaFormat(version, objectType, uriInfo);
  }
}

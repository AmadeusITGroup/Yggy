package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.Error;
import com.amadeus.aud.services.apis.models.http.responses.GenericResponse;
import com.amadeus.aud.services.apis.services.endpoints.ModelsService;
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
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.Separator;

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
public class ModelsResource {
  private final ModelsService modelsService;

  @Context UriInfo uriInfo;

  @Inject
  public ModelsResource(ModelsService modelsService) {
    this.modelsService = modelsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of a given object type with all properties for a specific version")
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
      value = {
        @Parameter(
            name = "version",
            description = "The requirements version",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "objectType",
            description = "The type of object to retrieve",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "where",
            description =
                """
            Filter conditions in the format where[name]=value.
            Repeatable for multiple conditions (each condition is ANDed).
            '*' and '?' can be used as globbing wildcards.
            """,
            required = false,
            examples = {
              @ExampleObject(name = "Exact Match", value = "where[name]=hello"),
              @ExampleObject(name = "Starts with", value = "where[name]=*AUD"),
              @ExampleObject(name = "Ends with", value = "where[name]=AUD*"),
              @ExampleObject(name = "One extra character", value = "where[name]=?ACS"),
            },
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "select",
            description =
                "Comma-separated list of properties to include in the response. Appears only once.",
            required = false,
            examples = {
              @ExampleObject(name = "Only name and description", value = "select=name,description"),
            },
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "limit",
            description = "Maximum number of values to return. Appears only once.",
            required = false,
            example = "10",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER, minimum = "0", exclusiveMinimum = true))
      })
  @Path("version/{version}/models/{objectType}")
  public Uni<Response> getModels(
      @PathParam("version") String version,
      @PathParam("objectType") String objectType,
      @RestQuery("select") @Separator(",") List<String> select,
      @QueryParam("limit") Integer limit) {
    return this.modelsService.get(
        objectType, version, uriInfo, select, limit == null ? Integer.MAX_VALUE : limit);
  }
}

package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.Error;
import com.amadeus.aud.services.apis.services.endpoints.ObjectService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
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
import org.jboss.resteasy.reactive.RestQuery;

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
public class ObjResource {
  private final ObjectService objectService;

  @Context UriInfo uriInfo;

  @Inject
  public ObjResource(ObjectService objectService) {
    this.objectService = objectService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary =
          "Object of a given object type with filtered by name all properties for a specific version")
  @Tags(value = @Tag(name = "List"))
  @APIResponse(
      responseCode = "200",
      description = "The Object for that object type filtered by name",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = Map.class)))
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
            name = "name",
            description = "The name of object to retrieve",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING))
      })
  @Path("version/{version}/obj/{objectType}")
  public Uni<Response> getObjects(
      @PathParam("version") String version,
      @PathParam("objectType") String objectType,
      @RestQuery("name") @NotBlank String name) {
    return this.objectService.get(version, objectType, name, uriInfo);
  }
}

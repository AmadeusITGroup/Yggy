package com.amadeus.aud.services.apis.endpoints.v2;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.yaml.models.Error;
import com.amadeus.aud.services.apis.services.endpoints.v2.ListOfObjectOfTypeService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
public class ListOfObjectOfTypeResource {
  private final ListOfObjectOfTypeService listOfObjectsByType;

  @Inject
  public ListOfObjectOfTypeResource(ListOfObjectOfTypeService listOfObjectsByType) {
    this.listOfObjectsByType = listOfObjectsByType;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of objects of a given object type for a specific version")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "The Object for that object",
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
                                  implementation = Object.class),
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "metadata",
                                  implementation = Metadata.class)
                            }))),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid pagination parameters",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Error.class),
                    example =
                        """
          {
              "code": 400,
              "message": "pageNum (5) exceeds last page (3)"
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
            name = "version",
            description = "The version number",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "objectType",
            description = "The type of object list to retrieve",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(implementation = ObjectTypeListNamesEnum.class)),
        @Parameter(
            name = "pageNum",
            description =
                "The page number to retrieve (starting from 1). It defaults to 1 if not set.",
            required = false,
            example = "1",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1")),
        @Parameter(
            name = "pageSize",
            description =
                "The number of items to retrieve per page. It defaults to 100 if not set.",
            required = false,
            example = "10",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1", maximum = "200"))
      })
  @Path("v2/versions/{version}/data/{objectType}")
  public Uni<Response> getAudObjectList(
      @PathParam("version") String version,
      @PathParam("objectType") String objectType,
      @QueryParam("pageNum") Integer pageNum,
      @QueryParam("pageSize") Integer pageSize) {

    return this.listOfObjectsByType
        .getObjectTypeResponse(version, objectType, pageNum, pageSize)
        .onItem()
        .transform(response -> Response.ok(response).build());
  }
}

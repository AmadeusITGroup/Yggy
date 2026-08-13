package com.amadeus.aud.services.apis.endpoints.v2;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.ConnectivityDetailItem;
import com.amadeus.aud.libraries.backend.models.v2.InstanceRepartition;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.ReferencesResponse;
import com.amadeus.aud.services.apis.services.endpoints.v2.ConnectivityDetailDirection;
import com.amadeus.aud.services.apis.services.endpoints.v2.ConnectivityDetailService;
import com.amadeus.aud.services.apis.services.endpoints.v2.InstanceRepartitionSelector;
import com.amadeus.aud.services.apis.services.endpoints.v2.InstanceRepartitionService;
import com.amadeus.aud.services.apis.services.endpoints.v2.ObjectByIdService;
import com.amadeus.aud.services.apis.services.endpoints.v2.ReferencesService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
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
public class ObjectByIdResource {

  private final ConnectivityDetailService connectivityDetailService;
  private final ObjectByIdService objectByIdService;
  private final ReferencesService referencesService;
  private final InstanceRepartitionService instanceRepartitionService;

  @Inject
  public ObjectByIdResource(
      ConnectivityDetailService connectivityDetailService,
      ObjectByIdService objectByIdService,
      ReferencesService referencesService,
      InstanceRepartitionService instanceRepartitionService) {
    this.connectivityDetailService = connectivityDetailService;
    this.objectByIdService = objectByIdService;
    this.referencesService = referencesService;
    this.instanceRepartitionService = instanceRepartitionService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get object of a given object type for a specific version")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "The Object for that object type by id",
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
                                  type = SchemaType.OBJECT,
                                  implementation = Metadata.class)
                            }))),
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
            name = "id",
            description = "The id of object type to retrieve",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "rawData",
            description = "Return the raw data of the object instead of processed object itself",
            required = false,
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.BOOLEAN)),
      })
  @Path("v2/versions/{version}/data/{objectType}/{id}")
  public Uni<Response> getAudObjectBasedOnType(
      @PathParam("version") String version,
      @PathParam("objectType") String objectType,
      @PathParam("id") String id,
      @QueryParam("rawData") boolean rawData) {

    return this.objectByIdService
        .getObjectByIdResponse(version, objectType, id, rawData)
        .onItem()
        .transform(response -> Response.ok(response).build());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get connectivity rows for a supported object")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Connectivity detail response",
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
                                  implementation = ConnectivityDetailItem.class),
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "metadata",
                                  implementation = Metadata.class)
                            }))),
        @APIResponse(
            responseCode = "400",
            description =
                "Bad request - unsupported object type, invalid direction, or invalid pagination parameters",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Error.class))),
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
            description = "The type of object for which connectivity can be retrieved",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(implementation = ObjectTypeListNamesEnum.class)),
        @Parameter(
            name = "id",
            description = "The id of the object",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "pageNum",
            description = "The page number to retrieve, starting from 1.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1")),
        @Parameter(
            name = "pageSize",
            description = "The number of items to retrieve per page.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER, minimum = "1", maximum = "200")),
        @Parameter(
            name = "direction",
            description =
                """
                Connectivity direction. Supported values are both, in, and out. \
                Directional filtering only applies to source/target-bound object types.""",
            in = ParameterIn.QUERY,
            schema =
                @Schema(
                    type = SchemaType.STRING,
                    implementation = ConnectivityDetailDirection.class,
                    enumeration = {"both", "in", "out"},
                    defaultValue = "both")),
        @Parameter(
            name = "pci",
            description =
                "When true, only PCI connectivity rows are returned. False or omitted returns all rows.",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.BOOLEAN, defaultValue = "false"))
      })
  @Path("v2/versions/{version}/data/{objectType}/{id}/connectivity")
  public Uni<Response> getConnectivityDetail(
      @PathParam("version") String version,
      @PathParam("objectType") String objectType,
      @PathParam("id") String id,
      @DefaultValue("both") @QueryParam("direction") String direction,
      @QueryParam("pci") Boolean pci,
      @QueryParam("pageNum") Integer pageNum,
      @QueryParam("pageSize") Integer pageSize) {

    return connectivityDetailService
        .getConnectivityDetailResponse(version, objectType, id, direction, pci, pageNum, pageSize)
        .onItem()
        .transform(response -> Response.ok(response).build());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get references (children) of a given object")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "The direct and inherited references for the given object",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(
                            type = SchemaType.OBJECT,
                            properties = {
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "data",
                                  type = SchemaType.OBJECT,
                                  implementation = ReferencesResponse.class)
                            }),
                    example =
                        """
            {
              "data": {
                "direct": {
                  "componentinstances": [
                    {
                      "id": "External URLs for 1AE2E", "scope": "1AE2E", "name": "External URLs for 1AE2E"
                    }
                  ],
                  "roles": [
                    {
                      "id": "Application", "scope": "GlobalDefinitions", "name": "Application"
                    }
                  ],
                  "scopereleasenotes": [
                    {
                      "id": "1AE2E***1.0.62", "scope": "1AE2E", "name": "1AE2E"
                    }
                  ]
                },
                "inherited": {
                   "componentinstances": []
                }
              }
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
            name = "id",
            description = "The id of object type to retrieve",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
      })
  @Path("v2/versions/{version}/data/{objectType}/{id}/references")
  public Uni<Response> getReferences(
      @PathParam("version") String version,
      @PathParam("objectType") String objectType,
      @PathParam("id") String id) {

    return referencesService
        .getReferences(version, objectType, id)
        .onItem()
        .transform(
            refs ->
                Response.ok(
                        new com.amadeus.aud.libraries.backend.models.v2.Response<>(
                            refs,
                            new Metadata()
                                .withInitialColumns(
                                    java.util.List.of("Name", "Scope", "Description"))))
                    .build());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get instance repartition for a component")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "The instance repartition data for the component",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(
                            type = SchemaType.OBJECT,
                            properties = {
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "data",
                                  implementation = InstanceRepartition.class),
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "metadata",
                                  implementation = Metadata.class)
                            }),
                    example =
                        """
                        {
                            "data": {
                                "breakdowns": {
                                    "continent": [
                                        {
                                            "count": 10,
                                            "name": "Europe"
                                        }
                                    ],
                                    "landingZone": [
                                        {
                                            "count": 1,
                                            "name": "LZ-1"
                                        },
                                        {
                                            "count": 4,
                                            "name": "LZ-2"
                                        }
                                    ],
                                    "macrophase": [
                                        {
                                            "count": 3,
                                            "name": "PRD"
                                        }
                                    ],
                                    "namespace": [
                                        {
                                            "count": 2,
                                            "name": "NS-1"
                                        }
                                    ],
                                    "paas": [
                                        {
                                            "count": 5,
                                            "name": "PS-1"
                                        }
                                    ],
                                    "securityZone": [
                                        {
                                            "count": 10,
                                            "name": "N/A"
                                        }
                                    ],
                                    "target": [
                                        {
                                            "count": 5,
                                            "name": "TG-1"
                                        }
                                    ]
                                },
                                "total": 15
                            },
                            "metadata": {}
                        }
                    """)),
        @APIResponse(
            responseCode = "400",
            description = "Bad request - invalid parameter values",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(
                            implementation = com.amadeus.aud.libraries.yaml.models.Error.class))),
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
            name = "id",
            description = "The id of the component",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "selector",
            description = "Instance selection filter (both, direct, indirect)",
            required = false,
            in = ParameterIn.QUERY,
            schema =
                @Schema(implementation = InstanceRepartitionSelector.class, defaultValue = "both"))
      })
  @Path("v2/versions/{version}/data/components/{id}/instance-repartition")
  public Uni<Response> getInstanceRepartition(
      @PathParam("version") String version,
      @PathParam("id") String id,
      @DefaultValue("both") @QueryParam("selector") String selector) {

    return instanceRepartitionService
        .getInstanceRepartition(version, ObjectTypeListNamesEnum.COMPONENTS, id, selector)
        .onItem()
        .transform(
            repartition ->
                Response.ok(
                        new com.amadeus.aud.libraries.backend.models.v2.Response<>(
                            repartition, new Metadata()))
                    .build());
  }
}

package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.Error;
import com.amadeus.aud.libraries.yaml.models.IpResponse;
import com.amadeus.aud.services.apis.services.endpoints.AudOpenReportService;
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
public class AudOpenReportResource {
  private final AudOpenReportService openReportService;

  @Context UriInfo uriInfo;

  @Inject
  public AudOpenReportResource(AudOpenReportService openReportService) {
    this.openReportService = openReportService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get Open Report",
      description = "Get Open Report between two Component Instances or/and Ip Addresses")
  @Tags(value = {@Tag(name = "Report")})
  @APIResponse(
      responseCode = "200",
      description = "Open Report successfully retrieved",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = IpResponse.class)))
  @Parameters({
    @Parameter(
        name = "version",
        description = "The requirements version",
        required = true,
        in = ParameterIn.PATH,
        schema = @Schema(type = SchemaType.STRING)),
    @Parameter(
        in = ParameterIn.PATH,
        name = "source",
        description = "Source IP address or Component Instance Name",
        required = true,
        schema = @Schema(type = SchemaType.STRING)),
    @Parameter(
        in = ParameterIn.PATH,
        name = "destination",
        description = "Destination IP address or Component Instance Name",
        required = true,
        schema = @Schema(type = SchemaType.STRING))
  })
  @Path("version/{version}/audopenreport/{source}/{destination}")
  public Uni<Response> getOpenReport(
      @PathParam("version") String version,
      @PathParam("source") String srcStr,
      @PathParam("destination") String dstStr) {
    return openReportService.getOpenReport(version, srcStr, dstStr, uriInfo);
  }
}

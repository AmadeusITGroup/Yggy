package com.amadeus.aud.services.audopen.http;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.audopen.models.OpenResponse;
import com.amadeus.aud.libraries.audopen.ports.AudOpenPort;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
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
public class AudOpenReportResource extends BaseService {
  private final AudOpenPort openReportService;
  private final AudService audService;

  @Context UriInfo uriInfo;

  @Inject
  public AudOpenReportResource(AudOpenPort openReportService, AudService audService) {
    this.openReportService = openReportService;
    this.audService = audService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get Open Report",
      description = "Get Open Report between two Component Instances or/and Ip Addresses")
  @Tags(value = {@Tag(name = "Report")})
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Open Report successfully retrieved",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = OpenResponse.class))),
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
    return getOpenReport(version, srcStr, dstStr, uriInfo);
  }

  public Uni<Response> getOpenReport(
      String dataVersion, String source, String destination, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getOpenReport(version, source, destination, uriInfo));
  }

  public Uni<Response> getOpenReport(
      Version version, String source, String destination, UriInfo uriInfo) {
    return this.openReportService
        .get(
            version,
            source,
            destination,
            UrlUtils.getUrl(uriInfo),
            UrlUtils.getVersionedUrl(uriInfo, version.getVersionNumber()))
        .onItem()
        .transform(Unchecked.function(this::getOkResponse))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }
}

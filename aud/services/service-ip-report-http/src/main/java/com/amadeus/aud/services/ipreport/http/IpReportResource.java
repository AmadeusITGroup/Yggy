package com.amadeus.aud.services.ipreport.http;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.ipreport.models.IpResponse;
import com.amadeus.aud.libraries.ipreport.ports.IpReportPort;
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
import java.net.URI;
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
public class IpReportResource extends BaseService {
  private final IpReportPort ipReportService;
  private final AudService audService;
  @Context UriInfo uriInfo;

  @Inject
  public IpReportResource(IpReportPort ipReportService, AudService audService) {
    this.ipReportService = ipReportService;
    this.audService = audService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get IP Report",
      description = "Retrieve the IP report for a given IP address.")
  @Tags(value = {@Tag(name = "Report")})
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "IP Report fetched successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = IpResponse.class))),
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
            name = "ip_str",
            description = "IP address in IPv4 format (e.g., 192.1.1.1)",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING))
      })
  @Path("version/{version}/ipreport/{ip_str}")
  public Uni<Response> getIpReport(
      @PathParam("ip_str") @ValidIPv4 String ipStr, @PathParam("version") String version) {
    return getIpReport(version, ipStr, uriInfo);
  }

  private Uni<Response> getIpReport(String dataVersion, String ipStr, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getIpReport(version, ipStr, uriInfo));
  }

  private Uni<Response> getIpReport(Version version, String ipStr, UriInfo uriInfo) {
    return this.ipReportService
        .get(
            version,
            ipStr,
            uriInfo.getRequestUri().toString(),
            getVersionedUrl(uriInfo, version.getVersionNumber()))
        .onItem()
        .transform(Unchecked.function(this::getOkResponse))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private static String getVersionedUrl(UriInfo uriInfo, String versionNumber) {
    URI uri = uriInfo.getRequestUri();
    return new StringBuffer()
        .append(uri.getScheme())
        .append("://")
        .append(uri.getHost())
        .append("/version/")
        .append(versionNumber)
        .append(uri.getPath())
        .toString();
  }
}

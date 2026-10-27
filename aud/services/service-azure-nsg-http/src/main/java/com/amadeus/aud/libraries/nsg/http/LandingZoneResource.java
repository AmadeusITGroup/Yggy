package com.amadeus.aud.libraries.nsg.http;

import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.nsg.models.NetworkSecurity;
import com.amadeus.aud.libraries.nsg.ports.LandingZoneNetworkSecurityPort;
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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class LandingZoneResource {
  private final LandingZoneNetworkSecurityPort landingZoneService;

  @Context UriInfo uriInfo;

  @Inject
  public LandingZoneResource(LandingZoneNetworkSecurityPort landingZoneService) {
    this.landingZoneService = landingZoneService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Network Security")
  @Tags(value = @Tag(name = "Azure NSG"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "network, subnets and security groups",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NetworkSecurity.class))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError"),
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
            name = "landingZone",
            description = "The landing zone name",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING))
      })
  @Path("version/{version}/landingzone/{landingZoneName}/network-security")
  public Uni<Response> getNetworkSecurityWithVersion(
      @PathParam("version") String version, @PathParam("landingZoneName") String landingZoneName) {
    return landingZoneService
        .getNetworkSecurity(version, landingZoneName)
        .map(networkSecurity -> Response.ok(withUrls(networkSecurity)).build());
  }

  private NetworkSecurity withUrls(NetworkSecurity networkSecurity) {
    return new NetworkSecurity(
        UrlUtils.getUrl(uriInfo),
        UrlUtils.getVersionedUrl(uriInfo, networkSecurity.getVersion()),
        networkSecurity.getVersion(),
        networkSecurity.getApiVersion(),
        networkSecurity.getNetworks(),
        networkSecurity.getSubnets(),
        networkSecurity.getNsgs());
  }
}

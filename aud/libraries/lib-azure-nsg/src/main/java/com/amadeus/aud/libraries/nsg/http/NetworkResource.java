package com.amadeus.aud.libraries.nsg.http;

import com.amadeus.aud.libraries.nsg.models.NetworkConfig;
import com.amadeus.aud.libraries.nsg.services.NetworkService;
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
public class NetworkResource {
  private final NetworkService networkService;

  @Context UriInfo uriInfo;

  @Inject
  public NetworkResource(NetworkService networkService) {
    this.networkService = networkService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Network Config for specific Version")
  @Tags(value = @Tag(name = "Azure NSG"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Provides the configuration of the network; CIDRs and Subnets",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NetworkConfig.class))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      @Parameter(
          name = "networkName",
          description = "The network name",
          required = true,
          in = ParameterIn.PATH,
          schema = @Schema(type = SchemaType.STRING)))
  @Path("version/{version}/network/{networkName}/config")
  public Uni<Response> getNetworkConfig(
      @PathParam("version") String version, @PathParam("networkName") String networkName) {
    return networkService.getNetworkConfig(version, networkName, uriInfo);
  }
}

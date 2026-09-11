package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.yaml.models.TargetCIDRsResponse;
import com.amadeus.aud.services.apis.services.endpoints.TargetCidrsService;
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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class TargetResource {

  private final TargetCidrsService targetCidrsService;
  @Context UriInfo uriInfo;

  @Inject
  public TargetResource(TargetCidrsService targetCidrsService) {
    this.targetCidrsService = targetCidrsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of cidrs information, used by UDR automation")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "An array of Target CIDRs",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TargetCIDRsResponse.class))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      value = {
        @Parameter(
            name = "target",
            description = "The target name",
            required = true,
            schema =
                @Schema(
                    type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.STRING))
      })
  @Path("target/{target}/cidrs")
  public Uni<Response> getTargetCidrs(@PathParam("target") String target) {
    return targetCidrsService.getTargetCidrs(Constants.LATEST, target, uriInfo);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List of cidrs information, used by UDR automation (with version)")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "An array of TargetCIDRs",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TargetCIDRsResponse.class))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      value = {
        @Parameter(
            name = "version",
            description = "The version of the data model",
            required = true,
            schema =
                @Schema(
                    type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.STRING)),
        @Parameter(
            name = "target",
            description = "The target name",
            required = true,
            schema =
                @Schema(
                    type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.STRING))
      })
  @Path("version/{version}/target/{target}/cidrs")
  public Uni<Response> getTargetCidrsWithVersion(
      @PathParam("version") String version, @PathParam("target") String target) {
    return targetCidrsService.getTargetCidrs(version, target, uriInfo);
  }
}

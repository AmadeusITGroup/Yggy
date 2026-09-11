package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.SecurityZoningResponse;
import com.amadeus.aud.services.apis.services.endpoints.SecurityZoningService;
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
public class SecurityZoningResource {
  private final SecurityZoningService securityZoningService;

  @Context UriInfo uriInfo;

  @Inject
  public SecurityZoningResource(SecurityZoningService securityZoningService) {
    this.securityZoningService = securityZoningService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Security Zoning (with version)")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "security zoning response",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SecurityZoningResponse.class))),
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
      })
  @Path("version/{version}/securityzoning")
  public Uni<Response> getSecurityZoningWithVersion(@PathParam("version") String version) {
    return securityZoningService.getSecurityZoning(version, uriInfo);
  }
}

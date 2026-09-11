package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.CloudProtectedTargetsResponse;
import com.amadeus.aud.services.apis.services.endpoints.CloudProtectedTargetsService;
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
public class CloudProtectedTargetsResource {
  private final CloudProtectedTargetsService cloudProtectedTargetsService;

  @Context UriInfo uriInfo;

  @Inject
  public CloudProtectedTargetsResource(CloudProtectedTargetsService cloudProtectedTargetsService) {
    this.cloudProtectedTargetsService = cloudProtectedTargetsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get Cloud Protected Targets for specific version",
      description = "Retrieves the cloud protected targets for specific version.")
  @Tags(value = @Tag(name = "api"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Cloud Protected Targets retrieved successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CloudProtectedTargetsResponse.class))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      @Parameter(
          name = "version",
          description = "The requirements version",
          required = true,
          in = ParameterIn.PATH,
          schema = @Schema(type = SchemaType.STRING)))
  @Path("version/{version}/cloud-protected-targets")
  public Uni<Response> getCloudProtectedTargetsWithVersion(@PathParam("version") String version) {
    return cloudProtectedTargetsService.getCloudProtectedTargets(version, uriInfo);
  }
}

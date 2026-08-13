package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.Error;
import com.amadeus.aud.libraries.yaml.models.SecurityMatrixMacrophaseResponse;
import com.amadeus.aud.services.apis.services.endpoints.SecurityMatrixMacrophaseHttpService;
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
public class SecurityMatrixMacrophaseResource {
  private final SecurityMatrixMacrophaseHttpService securityMatrixMacrophaseHttpService;

  @Context UriInfo uriInfo;

  @Inject
  public SecurityMatrixMacrophaseResource(
      SecurityMatrixMacrophaseHttpService securityMatrixMacrophaseHttpService) {
    this.securityMatrixMacrophaseHttpService = securityMatrixMacrophaseHttpService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get Security Matrix Macrophase for specific version",
      description =
          "Retrieves the security matrix macrophase connection between Macrophases for specific version.")
  @Tags(value = @Tag(name = "api"))
  @APIResponse(
      responseCode = "200",
      description = "Security Matrix Macrophase retrieved successfully",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON,
              schema = @Schema(implementation = SecurityMatrixMacrophaseResponse.class)))
  @Parameters(
      @Parameter(
          name = "version",
          description = "The requirements version",
          required = true,
          in = ParameterIn.PATH,
          schema = @Schema(type = SchemaType.STRING)))
  @Path("version/{version}/securitymatrixmacrophase")
  public Uni<Response> getSecurityMatrixMacrophases(@PathParam("version") String version) {
    return securityMatrixMacrophaseHttpService.getSecurityMatrixMacrophases(version, uriInfo);
  }
}

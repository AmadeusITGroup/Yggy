package com.amadeus.aud.services.apis.endpoints.v2;

import com.amadeus.aud.libraries.engine.data.services.external.storage.SingleVersion;
import com.amadeus.aud.services.apis.services.endpoints.VersionService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class VersionsResource {
  private final VersionService versionService;

  @Context UriInfo uriInfo;

  @Inject
  public VersionsResource(VersionService versionService) {
    this.versionService = versionService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get current versions information",
      description = "Returns the versions information")
  @Tags(value = @Tag(name = "v2/Version"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "A Version object",
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
                                  implementation = SingleVersion.class)
                            }),
                    example =
                        """
            {
              "data": [
                {"version":"1.0.27821","timestamp":"2025-05-07T16:29:23Z", "links": 10},
                {"version":"1.0.27820","timestamp":"2025-05-07T16:06:12Z", "links": 0}
              ]
            }
          """)),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Path("v2/versions")
  public Uni<jakarta.ws.rs.core.Response> getVersions() {
    return versionService.getAllAvailableVersionsResponse();
  }
}

package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.Version;
import com.amadeus.aud.services.apis.models.http.requests.VersionLoadRequest;
import com.amadeus.aud.services.apis.services.endpoints.VersionService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
public class VersionResource {
  private final VersionService versionService;

  @Context UriInfo uriInfo;

  @Inject
  public VersionResource(VersionService versionService) {
    this.versionService = versionService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get current version information",
      description = "Returns the version information of the API and AUD Requirements")
  @Tags(value = @Tag(name = "Version"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "A Version object",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Version.class),
                    example =
                        """
                                {
                                  "requirements_version": "1.0.20069",
                                  "api_version": "53.7.2",
                                  "requirements_md5": "5e14b238dbd698a3a09c818463e7c720",
                                  "platform": "local"
                                }
                                """)),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Path("version")
  public Uni<Version> getVersion() {
    return versionService.getLatestVersionUni();
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Trigger loading of a specific version or the latest version")
  @Tags(value = @Tag(name = "Version"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Version loading initiated",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    example =
                        """
                                {
                                  "requirements_version": "1.0.12345",
                                  "api_version": "1.0.0",
                                  "requirements_md5": "abcdef1234567890",
                                  "platform": "local"
                                }
                                """)),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Path("version/load")
  public Uni<Response> loadVersion(VersionLoadRequest requestVersion) {
    return versionService.getVersion(requestVersion);
  }

  @GET
  @Path("version/loaded")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get the list of loaded versions")
  @Tags(value = @Tag(name = "Version"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "List of loaded versions.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.ARRAY, implementation = Version.class),
                    example =
                        """
                                    [
                                      {
                                        "requirements_version": "1.0.12345",
                                        "api_version": "1.0.0",
                                        "requirements_md5": "abcdef1234567890",
                                        "platform": "local"
                                      }
                                    ]
                                    """)),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  public Uni<Response> getLoadedVersions() {
    return versionService.getLoadedVersions(uriInfo);
  }

  @GET
  @Path("health")
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "Health check", description = "Returns a simple health status message")
  @Tags(value = @Tag(name = "Health"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Service is healthy",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, example = "Still alive")),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  public Uni<String> getHealth() {
    return Uni.createFrom().item("Still alive");
  }
}

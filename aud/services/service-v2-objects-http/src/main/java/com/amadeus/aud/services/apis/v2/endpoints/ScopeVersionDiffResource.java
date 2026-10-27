package com.amadeus.aud.services.apis.v2.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.ScopeVersionDiffItem;
import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.SingleVersion;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.v2.objects.service.ScopeVersionDiffService;
import com.amadeus.aud.libraries.yaml.models.Error;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Objects;
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
public class ScopeVersionDiffResource {
  private final AudService audService;
  private final AudVersionsService audVersionService;
  private final ScopeVersionDiffService scopeVersionDiffService;

  @Inject
  public ScopeVersionDiffResource(
      AudService audService,
      AudVersionsService audVersionService,
      ScopeVersionDiffService scopeVersionDiffService) {
    this.audService = audService;
    this.audVersionService = audVersionService;
    this.scopeVersionDiffService = scopeVersionDiffService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "List scope differences between two data versions")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Scope version differences",
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
                                  implementation = ScopeVersionDiffItem.class),
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "metadata",
                                  implementation = Metadata.class)
                            }))),
        @APIResponse(
            responseCode = "400",
            description =
                "Bad request - missing version query parameters or invalid pagination parameters",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Error.class),
                    example =
                        """
          {
              "code": 400,
              "message": "oldVersion query parameter is required"
          }
          """)),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      value = {
        @Parameter(
            name = "oldVersion",
            description = "The older data version number",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "newVersion",
            description = "The newer data version number",
            required = true,
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "pageNum",
            description = "Page number, starting at 1",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER)),
        @Parameter(
            name = "pageSize",
            description = "Number of items per page",
            in = ParameterIn.QUERY,
            schema = @Schema(type = SchemaType.INTEGER))
      })
  @Path("v2/scope-diffs")
  public Uni<jakarta.ws.rs.core.Response> getScopeVersionDiff(
      @QueryParam("oldVersion") String oldVersionId,
      @QueryParam("newVersion") String newVersionId,
      @QueryParam("pageNum") Integer pageNum,
      @QueryParam("pageSize") Integer pageSize) {

    return this.audVersionService
        .listAllVersions()
        .onItem()
        .transformToUni(
            (List<SingleVersion> availableVersions) -> {
              if (availableVersions.isEmpty()) {
                throw new ServiceUnavailableException("No versions available");
              }

              String latestVersionId = availableVersions.getLast().version();

              validateVersionExists(oldVersionId, "oldVersion", availableVersions, latestVersionId);
              validateVersionExists(newVersionId, "newVersion", availableVersions, latestVersionId);

              return this.audService
                  .getVersion(oldVersionId)
                  .onItem()
                  .transformToUni(
                      (Version oldVersion) ->
                          this.audService
                              .getVersion(newVersionId)
                              .onItem()
                              .transformToUni(
                                  (Version newVersion) ->
                                      this.scopeVersionDiffService.getScopeVersionDiffResponse(
                                          oldVersion, newVersion, pageNum, pageSize)));
            })
        .onItem()
        .transform(response -> jakarta.ws.rs.core.Response.ok(response).build());
  }

  private static void validateVersionExists(
      @Nullable String versionId,
      String parameterName,
      List<SingleVersion> availableVersions,
      String latestVersionId) {
    if (versionId != null && !versionId.isBlank()) {
      if (Constants.LATEST.equalsIgnoreCase(versionId)) {
        return;
      }

      if (Objects.equals(versionId, latestVersionId)) {
        return;
      }

      for (SingleVersion availableVersion : availableVersions) {
        if (Objects.equals(availableVersion.version(), versionId)) {
          return;
        }
      }
    }

    throw new BadRequestException("%s: a valid version is required".formatted(parameterName));
  }
}

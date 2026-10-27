package com.amadeus.aud.services.apis.v2.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.v2.objects.service.StatisticsService;
import com.amadeus.aud.libraries.yaml.models.StatisticsScope;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
public class StatisticsPerScopeResource {
  private final AudService audService;
  private final StatisticsService statisticsService;

  @Inject
  public StatisticsPerScopeResource(AudService audService, StatisticsService statisticsService) {
    this.audService = audService;
    this.statisticsService = statisticsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Retrieve statistics for a scope and a version")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "Scope statistics",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema =
                        @Schema(
                            type = SchemaType.OBJECT,
                            properties = {
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "data",
                                  type = SchemaType.OBJECT,
                                  implementation = StatisticsScope.class),
                              @org.eclipse.microprofile.openapi.annotations.media.SchemaProperty(
                                  name = "metadata",
                                  implementation = Metadata.class)
                            }))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError"),
        @APIResponse(responseCode = "404", ref = "#/components/responses/NotFoundError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Parameters(
      value = {
        @Parameter(
            name = "version",
            description = "AUD version",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING)),
        @Parameter(
            name = "scope",
            description = "Scope name",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = SchemaType.STRING))
      })
  @Path("v2/versions/{version}/data/scopes/{scope}/statistics")
  public Uni<Response> getStatisticsForScope(
      @PathParam("version") String versionId, @PathParam("scope") String scope) {
    return this.audService
        .getVersion(versionId)
        .onItem()
        .transformToUni(
            version ->
                this.statisticsService
                    .getStatisticsForScope(version, scope)
                    .onItem()
                    .transform(response -> Response.ok(response).build()));
  }
}

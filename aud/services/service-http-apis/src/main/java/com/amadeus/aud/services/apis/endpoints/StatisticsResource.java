package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.v2.objects.service.StatisticsService;
import com.amadeus.aud.libraries.yaml.models.Statistics;
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
public class StatisticsResource {
  private final AudService audService;
  private final StatisticsService statisticsService;

  @Context UriInfo uriInfo;

  @Inject
  public StatisticsResource(AudService audService, StatisticsService statisticsService) {
    this.audService = audService;
    this.statisticsService = statisticsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Retrieve statistics about the AUDs (with version)")
  @Tags(value = @Tag(name = "Statistics"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "AUD Statistics response",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Statistics.class))),
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
  @Path("version/{version}/statistics")
  public Uni<Response> getStatisticsWithVersion(@PathParam("version") String versionId) {
    return this.audService
        .getVersion(versionId)
        .onItem()
        .transformToUni(
            version ->
                statisticsService.getStatistics(
                    version,
                    UrlUtils.getUrl(uriInfo),
                    UrlUtils.getVersionedUrl(uriInfo, version.getVersionNumber())))
        .onItem()
        .transform(statistics -> Response.ok(statistics).build());
  }
}

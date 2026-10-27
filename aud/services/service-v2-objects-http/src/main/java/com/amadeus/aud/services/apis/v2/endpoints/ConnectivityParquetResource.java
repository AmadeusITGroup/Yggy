package com.amadeus.aud.services.apis.v2.endpoints;

import com.amadeus.aud.libraries.v2.objects.service.ConnectivityParquetService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class ConnectivityParquetResource {

  private final ConnectivityParquetService connectivityParquetService;

  @Inject
  public ConnectivityParquetResource(ConnectivityParquetService connectivityParquetService) {
    this.connectivityParquetService = connectivityParquetService;
  }

  @GET
  @Path("v2/versions/latest/connectivity")
  @Operation(
      summary = "Redirect to temporary SAS URL for connectivity parquet file",
      description =
          "Returns a 307 Temporary Redirect to the Azure Blob Storage SAS URL for direct HTTP Range requests.")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "307",
            description = "Temporary redirect to Azure Blob Storage SAS URL"),
        @APIResponse(responseCode = "500", description = "Failed to generate SAS URL")
      })
  public Uni<Response> getParquetFileRedirect() {
    return Uni.createFrom()
        .item(connectivityParquetService::getSasUrlInfo)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .onItem()
        .transform(
            (ConnectivityParquetService.SasUrlInfo info) -> {
              long remainingSeconds =
                  Math.max(
                      0,
                      Duration.between(OffsetDateTime.now(), info.expiryTime().minusMinutes(5))
                          .toSeconds());
              long maxAge = Math.min(remainingSeconds, 300);

              return Response.temporaryRedirect(URI.create(info.url()))
                  .header("Cache-Control", "private, max-age=" + maxAge)
                  .build();
            });
  }
}

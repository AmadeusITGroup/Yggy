package com.amadeus.aud.services.engine.resources;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class HealthResource {
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
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = MediaType.TEXT_PLAIN))
      })
  public Uni<String> getHealth() {
    return Uni.createFrom().item("Still alive");
  }
}

package com.amadeus.aud.services.apis.endpoints.v2;

import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import com.amadeus.aud.services.apis.services.endpoints.UrisService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class UrisResource {
  private final UrisService urisService;

  @Inject
  public UrisResource(UrisService urisService) {
    this.urisService = urisService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Uris backend call that returns the uris that will be displayed in the web ui.")
  @Tags(value = @Tag(name = "v2/Webui"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description =
                "Uris backend call that returns the uris that will be displayed in the web ui.",
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
                                  implementation = UriItem.class)
                            }),
                    examples =
                        @ExampleObject(
                            name = "Example response",
                            value =
                                """
            {
              "data": [
                {
                  "title": "Scopes",
                  "uri": "/scopes",
                  "category": "Governance"
                }
              ]
            }
            """))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  @Path("v2/uris")
  public Uni<Response> getUris() {
    return urisService.getUris();
  }
}

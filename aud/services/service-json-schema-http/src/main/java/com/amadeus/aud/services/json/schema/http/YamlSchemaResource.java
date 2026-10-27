package com.amadeus.aud.services.json.schema.http;

import com.amadeus.aud.libraries.json.schema.ports.SchemaPort;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Path("")
public class YamlSchemaResource {
  private final SchemaPort schemaGenerator;

  @Inject
  public YamlSchemaResource(SchemaPort schemaGenerator) {
    this.schemaGenerator = schemaGenerator;
  }

  @GET
  @Path("v1/yaml-schema")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get the JSON Schema for V1 YAML resources",
      description =
          "Returns a standalone JSON Schema that selects the V1 resource schema from the YAML kind field.")
  @Tags(value = @Tag(name = "Schema"))
  @APIResponses(
      value = {
        @APIResponse(
            responseCode = "200",
            description = "JSON Schema for one V1 YAML resource",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(type = SchemaType.OBJECT))),
        @APIResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @APIResponse(responseCode = "500", ref = "#/components/responses/InternalError")
      })
  public JsonNode getSchema() {
    return schemaGenerator.get();
  }
}

package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.Error;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@ApplicationPath("/")
@OpenAPIDefinition(
    info =
        @Info(
            title = "AUD",
            version = "1.0.0",
            description = "Amadeus Unified Descriptor API specification"),
    components =
        @Components(
            responses = {
              @APIResponse(
                  name = "UnauthorizedError",
                  description = "Unauthorized Error",
                  headers =
                      @Header(
                          name = "WWW-Authenticate",
                          description =
                              "The authentication method that should be used to gain access",
                          schema =
                              @Schema(
                                  type = SchemaType.STRING,
                                  defaultValue = "Bearer realm=\"Access to AUD API\"")),
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
                              """)),
              @APIResponse(
                  name = "ForbiddenError",
                  description = "Forbidden Error",
                  content =
                      @Content(
                          mediaType = MediaType.APPLICATION_JSON,
                          schema = @Schema(implementation = Error.class),
                          example =
                              """
                              {
                                  "code": 403,
                                  "message": "User does not have permission to access this resource"
                              }
                              """)),
              @APIResponse(
                  name = "NotFoundError",
                  description = "Not Found Error",
                  content =
                      @Content(
                          mediaType = MediaType.APPLICATION_JSON,
                          schema = @Schema(implementation = Error.class),
                          example =
                              """
                              {
                                  "code": 404,
                                  "message": "The resource you have requested does not exist."
                              }
                              """)),
              @APIResponse(
                  name = "InternalError",
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
            }))
public final class OpenApiConfiguration extends Application {}

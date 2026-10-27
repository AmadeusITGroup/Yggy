package com.amadeus.aud.services.apis;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Checks that the packaged application exposes exactly the endpoints listed in {@link
 * #EXPECTED_ENDPOINTS}.
 *
 * <p>It reads the OpenAPI schema written by the Quarkus build ({@code
 * quarkus.smallrye-openapi.store-schema-directory}), so it covers the endpoints of every module
 * Quarkus indexed, including the HTTP modules that are only runtime dependencies of this service.
 * No application is started, but the schema only exists after {@code package}, hence an integration
 * test.
 */
class EndpointInventoryIT {
  private static final String SCHEMA_FILE_PROPERTY = "openapi.schema.file";
  private static final Set<String> HTTP_METHODS =
      Set.of("get", "put", "post", "delete", "options", "head", "patch", "trace");

  /**
   * One {@code METHOD /path} per line, sorted. Update it when an endpoint is intentionally added,
   * removed or renamed.
   */
  private static final String EXPECTED_ENDPOINTS =
      """
      GET /graphdump
      GET /health
      GET /scopediff/{oldVersion}/{newVersion}
      GET /target/{target}/cidrs
      GET /v2/scope-diffs
      GET /v2/uris
      GET /v2/versions
      GET /v2/versions/latest/connectivity
      GET /v2/versions/{version}/data/components/{id}/instance-repartition
      GET /v2/versions/{version}/data/scopes/{scope}/statistics
      GET /v2/versions/{version}/data/{objectType}
      GET /v2/versions/{version}/data/{objectType}/{id}
      GET /v2/versions/{version}/data/{objectType}/{id}/connectivity
      GET /v2/versions/{version}/data/{objectType}/{id}/references
      GET /version
      GET /version/loaded
      GET /version/{version}/audopenreport/{source}/{destination}
      GET /version/{version}/cloud-protected-targets
      GET /version/{version}/fulllist/{objectType}
      GET /version/{version}/graphdump
      GET /version/{version}/ipreport/{ip_str}
      GET /version/{version}/landingzone/{landingZoneName}/network-security
      GET /version/{version}/models/{objectType}
      GET /version/{version}/network/{networkName}/config
      GET /version/{version}/obj/{objectType}
      GET /version/{version}/scope-versions
      GET /version/{version}/securitymatrix
      GET /version/{version}/securitymatrixmacrophase
      GET /version/{version}/securityzoning
      GET /version/{version}/statistics
      GET /version/{version}/target/{target}/cidrs
      GET /version/{version}/target/{target}/spokes-cidrs
      GET /version/{version}/v1/fulllist/{objectType}
      POST /version/load
      """;

  @Test
  void packagedApplicationExposesExactlyTheExpectedEndpoints() throws IOException {
    Path schemaFile = schemaFile();
    JsonNode schema = new ObjectMapper().readTree(schemaFile.toFile());
    List<String> expected = List.of(EXPECTED_ENDPOINTS.split("\n"));

    List<String> actual = endpointsOf(schema);

    assertThat(actual)
        .as(
            """
            endpoints in %s (update EndpointInventoryIT.EXPECTED_ENDPOINTS if the change is\
             intended)""",
            schemaFile)
        .containsExactlyInAnyOrderElementsOf(expected);
  }

  private static Path schemaFile() {
    String location = System.getProperty(SCHEMA_FILE_PROPERTY);
    if (location == null || location.isBlank()) {
      throw new IllegalStateException(
          "System property "
              + SCHEMA_FILE_PROPERTY
              + " is not set; run this test with `mvnd verify -pl services/service-http-apis`");
    }
    Path schemaFile = Path.of(location);
    if (!Files.isRegularFile(schemaFile)) {
      throw new IllegalStateException(
          schemaFile + " does not exist; it is written by the Quarkus build during `package`");
    }
    return schemaFile;
  }

  private static List<String> endpointsOf(JsonNode schema) {
    List<String> endpoints = new ArrayList<>();
    for (Map.Entry<String, JsonNode> path : schema.path("paths").properties()) {
      for (Map.Entry<String, JsonNode> operation : path.getValue().properties()) {
        String method = operation.getKey();
        if (HTTP_METHODS.contains(method)) {
          endpoints.add(method.toUpperCase(Locale.ROOT) + " " + path.getKey());
        }
      }
    }
    return endpoints;
  }
}

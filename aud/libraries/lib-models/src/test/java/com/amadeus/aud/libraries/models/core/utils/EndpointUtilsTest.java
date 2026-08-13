package com.amadeus.aud.libraries.models.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.models.core.internals.Endpoint.TypeEnum;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EndpointUtilsTest {
  @Nested
  class TestTranslateDataEndpointsToYamlEndpoints {
    @Test
    void basic() {
      Endpoint endpoint = new Endpoint(TypeEnum.FQDN, "example.com", "desc");
      Set<Endpoint> dataEndpoints = Set.of(endpoint);

      List<com.amadeus.aud.libraries.yaml.models.Endpoint> yamlEndpoints =
          EndpointUtils.translateDataEndpointsToYamlEndpoints(dataEndpoints);

      assertThat(yamlEndpoints).hasSize(1);
      com.amadeus.aud.libraries.yaml.models.Endpoint yamlEndpoint = yamlEndpoints.getFirst();
      assertThat(yamlEndpoint.getType()).isEqualTo("fqdn");
      assertThat(yamlEndpoint.getValue()).isEqualTo("example.com");
      assertThat(yamlEndpoint.getDescription()).isEqualTo("desc");
    }

    @Test
    void emptySet() {
      List<com.amadeus.aud.libraries.yaml.models.Endpoint> yamlEndpoints =
          EndpointUtils.translateDataEndpointsToYamlEndpoints(Collections.emptySet());
      assertThat(yamlEndpoints).isEmpty();
    }
  }
}

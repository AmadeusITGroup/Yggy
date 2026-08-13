package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.yaml.models.Endpoint;
import org.junit.jupiter.api.Test;

class EndpointFilterTest {

  @Test
  void matchesReturnsTrueWhenAllFiltersAreNull() {
    EndpointFilter filter = new EndpointFilter();
    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenEndpointIsNull() {
    EndpointFilter filter = new EndpointFilter();
    filter.setType("CIDR");

    assertThat(filter.matches(null)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenTypeMatches() {
    EndpointFilter filter = new EndpointFilter();
    filter.setType("CIDR");

    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenTypeDoesNotMatch() {
    EndpointFilter filter = new EndpointFilter();
    filter.setType("FQDN");

    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenTypeMatchesIgnoreCase() {
    EndpointFilter filter = new EndpointFilter();
    filter.setType("cidr");

    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isTrue();
  }

  @Test
  void matchesReturnsTrueWhenValueContains() {
    EndpointFilter filter = new EndpointFilter();
    filter.setValue("10.0");

    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenValueDoesNotContain() {
    EndpointFilter filter = new EndpointFilter();
    filter.setValue("192.168");

    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenBothTypeAndValueMatch() {
    EndpointFilter filter = new EndpointFilter();
    filter.setType("CIDR");
    filter.setValue("10.0");

    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenTypeMatchesButValueDoesNot() {
    EndpointFilter filter = new EndpointFilter();
    filter.setType("CIDR");
    filter.setValue("192.168");

    Endpoint endpoint = createEndpoint("CIDR", "10.0.0.0/24");

    assertThat(filter.matches(endpoint)).isFalse();
  }

  private Endpoint createEndpoint(String type, String value) {
    Endpoint endpoint = new Endpoint();
    endpoint.setType(type);
    endpoint.setValue(value);
    return endpoint;
  }
}

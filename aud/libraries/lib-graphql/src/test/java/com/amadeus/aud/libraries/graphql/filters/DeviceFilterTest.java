package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.V1Device;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DeviceFilterTest {

  private StringFilter sf(String value) {
    StringFilter filter = new StringFilter();
    filter.setLike(value);
    return filter;
  }

  private V1Device device() {
    V1Device device = new V1Device();
    device.setName("aud-requirements");
    device.setDeviceType("AUDs");
    return device;
  }

  @Nested
  class Matches {
    @Test
    void nullDevice_returnsFalse() {
      assertThat(new DeviceFilter().matches(null)).isFalse();
    }

    @Test
    void emptyFilter_matchesAll() {
      assertThat(new DeviceFilter().matches(device())).isTrue();
    }
  }

  @Nested
  class ByName {
    @Test
    void match() {
      DeviceFilter filter = new DeviceFilter();
      filter.setName(sf("aud-requirements"));

      assertThat(filter.matches(device())).isTrue();
    }

    @Test
    void noMatch() {
      DeviceFilter filter = new DeviceFilter();
      filter.setName(sf("tech01a"));

      assertThat(filter.matches(device())).isFalse();
    }
  }

  @Nested
  class ByDeviceType {
    @Test
    void match() {
      DeviceFilter filter = new DeviceFilter();
      filter.setDeviceType(sf("auds"));

      assertThat(filter.matches(device())).isTrue();
    }

    @Test
    void noMatch() {
      DeviceFilter filter = new DeviceFilter();
      filter.setDeviceType(sf("landingzone"));

      assertThat(filter.matches(device())).isFalse();
    }
  }

  @Nested
  class CombinedFields {
    @Test
    void match() {
      DeviceFilter filter = new DeviceFilter();
      filter.setName(sf("aud-requirements"));
      filter.setDeviceType(sf("auds"));

      assertThat(filter.matches(device())).isTrue();
    }

    @Test
    void nameMatchesButTypeDoesNot_noMatch() {
      DeviceFilter filter = new DeviceFilter();
      filter.setName(sf("tech01a"));
      filter.setDeviceType(sf("landingzone"));

      assertThat(filter.matches(device())).isFalse();
    }
  }
}

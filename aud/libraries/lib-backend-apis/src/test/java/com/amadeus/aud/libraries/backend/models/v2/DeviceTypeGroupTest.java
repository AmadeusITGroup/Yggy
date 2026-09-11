package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.yaml.models.V1Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DeviceTypeGroupTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Nested
  class Serialization {
    @Test
    void shouldSerializeAllFields() throws Exception {
      DeviceTypeGroup group = new DeviceTypeGroup("firewall", List.of("fw-001", "fw-002"));

      String output = mapper.writeValueAsString(group);

      assertThat(output)
          .isEqualTo("{\"device_type\":\"firewall\",\"device_names\":[\"fw-001\",\"fw-002\"]}");
    }
  }

  @Nested
  class FromSecurityLayers {
    @Test
    void shouldGroupByDeviceType() {
      List<SecurityLayerResult> layers =
          List.of(
              new SecurityLayerResult("firewall", "fw-001", true, false),
              new SecurityLayerResult("router", "rt-001", false, true),
              new SecurityLayerResult("firewall", "fw-002", false, true));

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromSecurityLayers(layers);

      assertThat(groups).hasSize(2);
      DeviceTypeGroup firewallGroup =
          groups.stream().filter(g -> "firewall".equals(g.deviceType())).findFirst().orElseThrow();
      assertThat(firewallGroup.deviceNames()).containsExactly("fw-001", "fw-002");

      DeviceTypeGroup routerGroup =
          groups.stream().filter(g -> "router".equals(g.deviceType())).findFirst().orElseThrow();
      assertThat(routerGroup.deviceNames()).containsExactly("rt-001");
    }

    @Test
    void shouldSortNamesWithinGroupCaseInsensitively() {
      List<SecurityLayerResult> layers =
          List.of(
              new SecurityLayerResult("firewall", "fw-Zebra", true, false),
              new SecurityLayerResult("firewall", "fw-apple", false, true),
              new SecurityLayerResult("firewall", "fw-Mango", false, false));

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromSecurityLayers(layers);

      assertThat(groups)
          .singleElement()
          .satisfies(
              group ->
                  assertThat(group.deviceNames())
                      .containsExactly("fw-apple", "fw-Mango", "fw-Zebra"));
    }

    @Test
    void shouldSortGroupsAlphabetically() {
      List<SecurityLayerResult> layers =
          List.of(
              new SecurityLayerResult("router", "rt-001", false, false),
              new SecurityLayerResult("appliance", "ap-001", false, false),
              new SecurityLayerResult("firewall", "fw-001", false, false));

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromSecurityLayers(layers);

      assertThat(groups)
          .extracting(DeviceTypeGroup::deviceType)
          .containsExactly("appliance", "firewall", "router");
    }

    @Test
    void shouldReturnEmptyListWhenInputIsEmpty() {
      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromSecurityLayers(List.of());

      assertThat(groups).isEmpty();
    }
  }

  @Nested
  class FromV1Devices {
    @Test
    void shouldReturnNullWhenInputIsNull() {
      assertThat(DeviceTypeGroup.fromV1Devices(null)).isNull();
    }

    @Test
    void shouldGroupByDeviceType() {
      V1Device d1 = new V1Device().deviceType("firewall").name("fw-001");
      V1Device d2 = new V1Device().deviceType("router").name("rt-001");
      V1Device d3 = new V1Device().deviceType("firewall").name("fw-002");

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of(d1, d2, d3));

      assertThat(groups).hasSize(2);
      DeviceTypeGroup firewallGroup =
          groups.stream().filter(g -> "firewall".equals(g.deviceType())).findFirst().orElseThrow();
      assertThat(firewallGroup.deviceNames()).containsExactlyInAnyOrder("fw-001", "fw-002");
    }

    @Test
    void shouldFilterOutEntriesWithNullName() {
      V1Device withName = new V1Device().deviceType("firewall").name("fw-001");
      V1Device withoutName = new V1Device().deviceType("firewall");

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of(withName, withoutName));

      assertThat(groups)
          .singleElement()
          .satisfies(group -> assertThat(group.deviceNames()).containsExactly("fw-001"));
    }

    @Test
    void shouldFilterOutNullElements() {
      V1Device valid = new V1Device().deviceType("firewall").name("fw-001");

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(Arrays.asList(valid, null));

      assertThat(groups)
          .singleElement()
          .satisfies(group -> assertThat(group.deviceNames()).containsExactly("fw-001"));
    }

    @Test
    void shouldSortNamesWithinGroupCaseInsensitively() {
      V1Device d1 = new V1Device().deviceType("firewall").name("fw-Zebra");
      V1Device d2 = new V1Device().deviceType("firewall").name("fw-apple");
      V1Device d3 = new V1Device().deviceType("firewall").name("fw-Mango");

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of(d1, d2, d3));

      assertThat(groups)
          .singleElement()
          .satisfies(
              group ->
                  assertThat(group.deviceNames())
                      .containsExactly("fw-apple", "fw-Mango", "fw-Zebra"));
    }

    @Test
    void shouldReturnEmptyListWhenInputIsEmpty() {
      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of());

      assertThat(groups).isEmpty();
    }

    @Test
    void shouldSortGroupsAlphabetically() {
      V1Device d1 = new V1Device().deviceType("router").name("rt-001");
      V1Device d2 = new V1Device().deviceType("appliance").name("ap-001");
      V1Device d3 = new V1Device().deviceType("firewall").name("fw-001");

      List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of(d1, d2, d3));

      assertThat(groups)
          .extracting(DeviceTypeGroup::deviceType)
          .containsExactly("appliance", "firewall", "router");
    }
  }
}

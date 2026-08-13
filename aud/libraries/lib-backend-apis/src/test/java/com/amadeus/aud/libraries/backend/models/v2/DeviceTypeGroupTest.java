package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.yaml.models.V1Device;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class DeviceTypeGroupTest {
  private final ObjectMapper mapper = new ObjectMapper();

  // ---- serialization -------------------------------------------------------

  @Test
  void shouldSerializeAllFields() throws Exception {
    DeviceTypeGroup group = new DeviceTypeGroup("firewall", List.of("fw-001", "fw-002"));

    String output = mapper.writeValueAsString(group);

    assertThat(output)
        .isEqualTo("{\"device_type\":\"firewall\",\"device_names\":[\"fw-001\",\"fw-002\"]}");
  }

  // ---- fromSecurityLayers --------------------------------------------------

  @Test
  void fromSecurityLayersShouldGroupByDeviceType() {
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
  void fromSecurityLayersShouldSortNamesWithinGroupCaseInsensitively() {
    List<SecurityLayerResult> layers =
        List.of(
            new SecurityLayerResult("firewall", "fw-Zebra", true, false),
            new SecurityLayerResult("firewall", "fw-apple", false, true),
            new SecurityLayerResult("firewall", "fw-Mango", false, false));

    List<DeviceTypeGroup> groups = DeviceTypeGroup.fromSecurityLayers(layers);

    assertThat(groups).hasSize(1);
    assertThat(groups.get(0).deviceNames()).containsExactly("fw-apple", "fw-Mango", "fw-Zebra");
  }

  @Test
  void fromSecurityLayersShouldSortGroupsAlphabetically() {
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
  void fromSecurityLayersShouldReturnEmptyListWhenInputIsEmpty() {
    List<DeviceTypeGroup> groups = DeviceTypeGroup.fromSecurityLayers(List.of());

    assertThat(groups).isEmpty();
  }

  // ---- fromV1Devices -------------------------------------------------------

  @Test
  void fromV1DevicesShouldReturnNullWhenInputIsNull() {
    assertThat(DeviceTypeGroup.fromV1Devices(null)).isNull();
  }

  @Test
  void fromV1DevicesShouldGroupByDeviceType() {
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
  void fromV1DevicesShouldFilterOutEntriesWithNullName() {
    V1Device withName = new V1Device().deviceType("firewall").name("fw-001");
    V1Device withoutName = new V1Device().deviceType("firewall");

    List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of(withName, withoutName));

    assertThat(groups).hasSize(1);
    assertThat(groups.get(0).deviceNames()).containsExactly("fw-001");
  }

  @Test
  void fromV1DevicesShouldFilterOutNullElements() {
    V1Device valid = new V1Device().deviceType("firewall").name("fw-001");

    List<DeviceTypeGroup> groups =
        DeviceTypeGroup.fromV1Devices(java.util.Arrays.asList(valid, null));

    assertThat(groups).hasSize(1);
    assertThat(groups.get(0).deviceNames()).containsExactly("fw-001");
  }

  @Test
  void fromV1DevicesShouldSortNamesWithinGroupCaseInsensitively() {
    V1Device d1 = new V1Device().deviceType("firewall").name("fw-Zebra");
    V1Device d2 = new V1Device().deviceType("firewall").name("fw-apple");
    V1Device d3 = new V1Device().deviceType("firewall").name("fw-Mango");

    List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of(d1, d2, d3));

    assertThat(groups).hasSize(1);
    assertThat(groups.get(0).deviceNames()).containsExactly("fw-apple", "fw-Mango", "fw-Zebra");
  }

  @Test
  void fromV1DevicesShouldReturnEmptyListWhenInputIsEmpty() {
    List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of());

    assertThat(groups).isEmpty();
  }

  @Test
  void fromV1DevicesShouldSortGroupsAlphabetically() {
    V1Device d1 = new V1Device().deviceType("router").name("rt-001");
    V1Device d2 = new V1Device().deviceType("appliance").name("ap-001");
    V1Device d3 = new V1Device().deviceType("firewall").name("fw-001");

    List<DeviceTypeGroup> groups = DeviceTypeGroup.fromV1Devices(List.of(d1, d2, d3));

    assertThat(groups)
        .extracting(DeviceTypeGroup::deviceType)
        .containsExactly("appliance", "firewall", "router");
  }
}

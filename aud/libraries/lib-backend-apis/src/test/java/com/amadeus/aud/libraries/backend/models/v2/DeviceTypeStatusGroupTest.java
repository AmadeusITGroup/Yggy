package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeviceTypeStatusGroupTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  @DisplayName("Should serialize DeviceTypeStatusGroup with nested VersionGroup")
  void shouldSerializeAllFields() throws Exception {
    DeviceTypeStatusGroup.VersionGroup v1 =
        new DeviceTypeStatusGroup.VersionGroup("AUD-1.0", List.of("dev-01", "dev-02"));
    DeviceTypeStatusGroup.VersionGroup v2 =
        new DeviceTypeStatusGroup.VersionGroup(null, List.of("dev-03"));

    DeviceTypeStatusGroup group = new DeviceTypeStatusGroup("firewall", List.of(v1, v2));

    String output = mapper.writeValueAsString(group);

    assertThat(output)
        .contains("\"device_type\":\"firewall\"")
        .contains("\"versions\":[")
        .contains("\"aud_version\":\"AUD-1.0\"")
        .contains("\"device_names\":[\"dev-01\",\"dev-02\"]")
        .contains("\"aud_version\":null")
        .contains("\"device_names\":[\"dev-03\"]");
  }

  @Test
  @DisplayName("Should maintain data integrity in records")
  void recordShouldStoreDataCorrectly() {
    DeviceTypeStatusGroup.VersionGroup vg =
        new DeviceTypeStatusGroup.VersionGroup("v1", List.of("d1"));
    DeviceTypeStatusGroup group = new DeviceTypeStatusGroup("type1", List.of(vg));

    assertThat(group.deviceType()).isEqualTo("type1");
    assertThat(group.versions()).hasSize(1);
    assertThat(group.versions().get(0).audVersion()).isEqualTo("v1");
    assertThat(group.versions().get(0).deviceNames()).containsExactly("d1");
  }
}

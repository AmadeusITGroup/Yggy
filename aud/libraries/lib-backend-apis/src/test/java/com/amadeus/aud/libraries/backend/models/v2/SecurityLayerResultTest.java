package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SecurityLayerResultTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void shouldSerializeAllFields() throws Exception {
    SecurityLayerResult result = new SecurityLayerResult("firewall", "fw-001", true, false);

    String output = mapper.writeValueAsString(result);

    assertThat(output)
        .isEqualTo(
            "{\"device_type\":\"firewall\",\"device_name\":\"fw-001\",\"has_additions\":true,\"has_deletions\":false}");
  }

  @Test
  void withActionShouldSetAddedFlag() {
    SecurityLayerResult result =
        SecurityLayerResult.withAction("firewall", "fw-001", SecurityLayerResult.ACTION_ADDED);

    assertThat(result.deviceType()).isEqualTo("firewall");
    assertThat(result.deviceName()).isEqualTo("fw-001");
    assertThat(result.hasAdditions()).isTrue();
    assertThat(result.hasDeletions()).isFalse();
  }

  @Test
  void withActionShouldSetDeletedFlag() {
    SecurityLayerResult result =
        SecurityLayerResult.withAction("router", "rt-002", SecurityLayerResult.ACTION_DELETED);

    assertThat(result.deviceType()).isEqualTo("router");
    assertThat(result.deviceName()).isEqualTo("rt-002");
    assertThat(result.hasAdditions()).isFalse();
    assertThat(result.hasDeletions()).isTrue();
  }

  @Test
  void withActionShouldBeCaseInsensitive() {
    SecurityLayerResult addedUpper = SecurityLayerResult.withAction("fw", "fw-001", "ADDED");
    SecurityLayerResult deletedMixed = SecurityLayerResult.withAction("fw", "fw-001", "Deleted");

    assertThat(addedUpper.hasAdditions()).isTrue();
    assertThat(deletedMixed.hasDeletions()).isTrue();
  }

  @Test
  void mergeShouldCombineFlags() {
    SecurityLayerResult first = new SecurityLayerResult("firewall", "fw-001", true, false);
    SecurityLayerResult second = new SecurityLayerResult("firewall", "fw-001", false, true);

    SecurityLayerResult merged = first.merge(second);

    assertThat(merged.deviceType()).isEqualTo("firewall");
    assertThat(merged.deviceName()).isEqualTo("fw-001");
    assertThat(merged.hasAdditions()).isTrue();
    assertThat(merged.hasDeletions()).isTrue();
  }

  @Test
  void mergeShouldShortCircuitWhenThisHasAdditionsIsTrue() {
    SecurityLayerResult first = new SecurityLayerResult("fw", "fw-001", true, false);
    SecurityLayerResult second = new SecurityLayerResult("fw", "fw-001", false, false);

    SecurityLayerResult merged = first.merge(second);

    assertThat(merged.hasAdditions()).isTrue();
  }

  @Test
  void mergeShouldEvaluateOtherWhenThisHasAdditionsIsFalse() {
    SecurityLayerResult first = new SecurityLayerResult("fw", "fw-001", false, false);
    SecurityLayerResult second = new SecurityLayerResult("fw", "fw-001", true, false);

    SecurityLayerResult merged = first.merge(second);

    assertThat(merged.hasAdditions()).isTrue();
  }

  @Test
  void mergeShouldShortCircuitWhenThisHasDeletionsIsTrue() {
    SecurityLayerResult first = new SecurityLayerResult("fw", "fw-001", false, true);
    SecurityLayerResult second = new SecurityLayerResult("fw", "fw-001", false, false);

    SecurityLayerResult merged = first.merge(second);

    assertThat(merged.hasDeletions()).isTrue();
  }

  @Test
  void mergeShouldEvaluateOtherWhenThisHasDeletionsIsFalse() {
    SecurityLayerResult first = new SecurityLayerResult("fw", "fw-001", false, false);
    SecurityLayerResult second = new SecurityLayerResult("fw", "fw-001", false, true);

    SecurityLayerResult merged = first.merge(second);

    assertThat(merged.hasDeletions()).isTrue();
  }

  @Test
  void mergeShouldReturnFalseWhenBothAreFalse() {
    SecurityLayerResult first = new SecurityLayerResult("fw", "fw-001", false, false);
    SecurityLayerResult second = new SecurityLayerResult("fw", "fw-001", false, false);

    SecurityLayerResult merged = first.merge(second);

    assertThat(merged.hasAdditions()).isFalse(); // false || false = false
    assertThat(merged.hasDeletions()).isFalse();
  }

  @Test
  void keyShouldConcatenateTypeAndName() {
    SecurityLayerResult result = new SecurityLayerResult("firewall", "fw-001", true, false);

    assertThat(result.key()).isEqualTo("firewall:fw-001");
  }

  @Test
  void equalsShouldCompareOnlyDeviceTypeAndName() {
    SecurityLayerResult result1 = new SecurityLayerResult("firewall", "fw-001", true, false);
    SecurityLayerResult result2 = new SecurityLayerResult("firewall", "fw-001", false, true);
    SecurityLayerResult result3 = new SecurityLayerResult("router", "fw-001", true, false);

    assertThat(result1).isEqualTo(result2).isNotEqualTo(result3);
  }

  @Test
  void hashCodeShouldBeConsistentWithEquals() {
    SecurityLayerResult result1 = new SecurityLayerResult("firewall", "fw-001", true, false);
    SecurityLayerResult result2 = new SecurityLayerResult("firewall", "fw-001", false, true);

    assertThat(result1).hasSameHashCodeAs(result2);
  }

  @Test
  void equalsShouldReturnTrueForSameInstance() {
    SecurityLayerResult result = new SecurityLayerResult("firewall", "fw-001", true, false);
    assertThat(result).isEqualTo(result);
  }

  @Test
  void equalsShouldReturnFalseForNull() {
    SecurityLayerResult result = new SecurityLayerResult("firewall", "fw-001", true, false);
    assertThat(result).isNotEqualTo(null);
  }

  @Test
  void equalsShouldReturnFalseForDifferentType() {
    SecurityLayerResult result = new SecurityLayerResult("firewall", "fw-001", true, false);
    assertThat(result).isNotEqualTo("not a SecurityLayerResult");
  }

  @Test
  void equalsShouldHandleNullDeviceType() {
    SecurityLayerResult result1 = new SecurityLayerResult(null, "fw-001", true, false);
    SecurityLayerResult result2 = new SecurityLayerResult(null, "fw-001", false, true);
    SecurityLayerResult result3 = new SecurityLayerResult("firewall", "fw-001", true, false);

    assertThat(result1).isEqualTo(result2).isNotEqualTo(result3);
  }

  @Test
  void equalsShouldHandleNullDeviceName() {
    SecurityLayerResult result1 = new SecurityLayerResult("firewall", null, true, false);
    SecurityLayerResult result2 = new SecurityLayerResult("firewall", null, false, true);
    SecurityLayerResult result3 = new SecurityLayerResult("firewall", "fw-001", true, false);

    assertThat(result1).isEqualTo(result2).isNotEqualTo(result3);
  }

  @Test
  void withActionShouldSetNoFlagsForUnknownAction() {
    SecurityLayerResult result = SecurityLayerResult.withAction("firewall", "fw-001", "unknown");

    assertThat(result.hasAdditions()).isFalse();
    assertThat(result.hasDeletions()).isFalse();
  }
}

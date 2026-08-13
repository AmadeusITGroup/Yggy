package com.amadeus.aud.libraries.graphql.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LabelFilterTest {
  @Test
  void emtpyFilterMatches() {
    assertThat(LabelFilter.matchesAllLabels(Map.of(), List.of())).isTrue();
  }

  @Test
  void emtpyFilterMatchesNothing() {
    LabelFilter filter = new LabelFilter();

    assertThat(LabelFilter.matchesAllLabels(Map.of(), List.of(filter))).isFalse();
  }

  @Test
  void emtpyFilterMatchesIfNonEmptyMap() {
    LabelFilter filter = new LabelFilter();

    assertThat(LabelFilter.matchesAllLabels(Map.of("my key", "my value"), List.of(filter)))
        .isTrue();
  }

  @Test
  void filterCannotMatchIfNothing() {
    LabelFilter filter = new LabelFilter();
    filter.setKey("my key");
    filter.setValue("my value");

    assertThat(filter.getKey()).isEqualTo("my key");
    assertThat(filter.getValue()).isEqualTo("my value");
    assertThat(LabelFilter.matchesAllLabels(Map.of(), List.of(filter))).isFalse();
  }

  @Test
  void filterNotMatching() {
    LabelFilter filter = new LabelFilter();
    filter.setKey("my key");
    filter.setValue("my value");

    assertThat(filter.getKey()).isEqualTo("my key");
    assertThat(filter.getValue()).isEqualTo("my value");
    assertThat(LabelFilter.matchesAllLabels(Map.of("other key", "other value"), List.of(filter)))
        .isFalse();
    assertThat(LabelFilter.matchesAllLabels(Map.of("my key", "other value"), List.of(filter)))
        .isFalse();
  }

  @Test
  void filterMatching() {
    LabelFilter filter = new LabelFilter();
    filter.setKey("my key");
    filter.setValue("my value");

    assertThat(LabelFilter.matchesAllLabels(Map.of("my key", "my value"), List.of(filter)))
        .isTrue();
  }
}

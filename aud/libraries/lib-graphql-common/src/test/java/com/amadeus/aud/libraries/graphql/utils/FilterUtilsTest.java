package com.amadeus.aud.libraries.graphql.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FilterUtilsTest {

  @Nested
  class StringMatching {

    @Test
    void containsIgnoresCaseAndHandlesNulls() {
      assertThat(FilterUtils.containsIgnoreCase(null, null)).isTrue();
      assertThat(FilterUtils.containsIgnoreCase("VALUE", "a value")).isTrue();
      assertThat(FilterUtils.containsIgnoreCase("missing", "a value")).isFalse();
      assertThat(FilterUtils.containsIgnoreCase("value", null)).isFalse();
    }

    @Test
    void equalityIgnoresCaseAndHandlesNulls() {
      assertThat(FilterUtils.equalsIgnoreCase(null, null)).isTrue();
      assertThat(FilterUtils.equalsIgnoreCase("VALUE", "value")).isTrue();
      assertThat(FilterUtils.equalsIgnoreCase("other", "value")).isFalse();
      assertThat(FilterUtils.equalsIgnoreCase("value", null)).isFalse();
    }

    @Test
    void booleanMatchingTreatsNullAsNoFilter() {
      assertThat(FilterUtils.matchesBoolean(null, true)).isTrue();
      assertThat(FilterUtils.matchesBoolean(true, true)).isTrue();
      assertThat(FilterUtils.matchesBoolean(false, true)).isFalse();
    }
  }

  @Nested
  class CollectionMatching {

    @Test
    void stringFilterMatchesObjectsAndHandlesMissingInputs() {
      StringFilter filter = new StringFilter();
      filter.setEq("expected");

      assertThat(FilterUtils.stringFilterMatches(null, null, Object::toString)).isTrue();
      assertThat(FilterUtils.stringFilterMatches(filter, null, Object::toString)).isFalse();
      assertThat(FilterUtils.stringFilterMatches(filter, "expected", value -> value)).isTrue();
    }

    @Test
    void anyStringFilterMatchesAtLeastOneItem() {
      StringFilter filter = new StringFilter();
      filter.setLike("match");

      assertThat(FilterUtils.<String>anyStringFilterMatches(null, null, value -> value)).isTrue();
      assertThat(FilterUtils.<String>anyStringFilterMatches(filter, null, value -> value))
          .isFalse();
      assertThat(
              FilterUtils.anyStringFilterMatches(
                  filter, List.of("no", "matching value"), value -> value))
          .isTrue();
      assertThat(
              FilterUtils.anyStringFilterMatches(
                  filter, List.of("first", "second"), value -> value))
          .isFalse();
    }

    @Test
    void allFiltersMustMatchAnItem() {
      assertThat(FilterUtils.allFilterMatch(null, null, Object::equals)).isTrue();
      assertThat(FilterUtils.allFilterMatch(List.of(), null, Object::equals)).isTrue();
      assertThat(FilterUtils.allFilterMatch(List.of("a"), null, Object::equals)).isFalse();
      assertThat(FilterUtils.allFilterMatch(List.of("a", "b"), List.of("b", "a"), String::equals))
          .isTrue();
      assertThat(FilterUtils.allFilterMatch(List.of("a", "c"), List.of("a", "b"), String::equals))
          .isFalse();
    }
  }

  @Nested
  class LabelMatching {

    @Test
    void allLabelsMustMatchCaseInsensitively() {
      LabelFilter matchingFilter = labelFilter("environment", "PRODUCTION");

      assertThat(FilterUtils.allLabelsMatch(null, null)).isTrue();
      assertThat(FilterUtils.allLabelsMatch(List.of(), null)).isTrue();
      assertThat(FilterUtils.allLabelsMatch(List.of(matchingFilter), null)).isFalse();
      assertThat(
              FilterUtils.allLabelsMatch(
                  List.of(matchingFilter), Map.of("environment", "production")))
          .isTrue();
      assertThat(
              FilterUtils.allLabelsMatch(
                  List.of(matchingFilter), Map.of("environment", "development")))
          .isFalse();
    }

    @Test
    void incompleteLabelFiltersAreIgnored() {
      LabelFilter missingKey = labelFilter(null, "value");
      LabelFilter missingValue = labelFilter("key", null);

      assertThat(
              FilterUtils.allLabelsMatch(
                  List.of(missingKey, missingValue), Map.of("other", "value")))
          .isTrue();
    }

    private LabelFilter labelFilter(String key, String value) {
      LabelFilter filter = new LabelFilter();
      filter.setKey(key);
      filter.setValue(value);
      return filter;
    }
  }
}

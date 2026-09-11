package com.amadeus.aud.libraries.models.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VersionUtilsTest {

  @Nested
  class ToLong {
    @Test
    void validVersion() {
      assertThat(VersionUtils.toLong("1.0.0")).isEqualTo(1000000000000L);
      assertThat(VersionUtils.toLong("1.0.1")).isEqualTo(1000000000001L);
      assertThat(VersionUtils.toLong("1.1.0")).isEqualTo(1001000000000L);
      assertThat(VersionUtils.toLong("2.0.0")).isEqualTo(2000000000000L);
      assertThat(VersionUtils.toLong("1.234.567890")).isEqualTo(1234000567890L);
    }

    @Test
    void invalidVersion() {
      assertThatThrownBy(() -> VersionUtils.toLong("1.0"))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> VersionUtils.toLong("1.0.0.0"))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> VersionUtils.toLong("1..0"))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> VersionUtils.toLong("a.b.c"))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> VersionUtils.toLong("1.0.a"))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class ToString {
    @Test
    void validVersion() {
      assertThat(VersionUtils.toString(1000000000000L)).isEqualTo("1.0.0");
      assertThat(VersionUtils.toString(1000000000001L)).isEqualTo("1.0.1");
      assertThat(VersionUtils.toString(1001000000000L)).isEqualTo("1.1.0");
      assertThat(VersionUtils.toString(2000000000000L)).isEqualTo("2.0.0");
      assertThat(VersionUtils.toString(1234000567890L)).isEqualTo("1.234.567890");
    }
  }

  @Nested
  class IsSemver {
    @Test
    void validSemver() {
      assertThat(VersionUtils.isSemver("0.0.0")).isTrue();
      assertThat(VersionUtils.isSemver("1.2.3")).isTrue();
      assertThat(VersionUtils.isSemver("123.456.789")).isTrue();
    }

    @Test
    void invalidSemver() {
      assertThat(VersionUtils.isSemver(null)).isFalse();
      assertThat(VersionUtils.isSemver("")).isFalse();
      assertThat(VersionUtils.isSemver("1")).isFalse();
      assertThat(VersionUtils.isSemver("1.2")).isFalse();
      assertThat(VersionUtils.isSemver("1.2.3.4")).isFalse();
      assertThat(VersionUtils.isSemver("a.b.c")).isFalse();
      assertThat(VersionUtils.isSemver("1..0")).isFalse();
    }
  }

  @Nested
  class ToRange {
    @Test
    void singleValueRange() {
      long vLong = VersionUtils.toLong("1.2.3");
      var range = VersionUtils.toRange("1.2.3");
      assertThat(range.lowerEndpoint()).isEqualTo(vLong);
      assertThat(range.upperEndpoint()).isEqualTo(vLong + 1);
      assertThat(range.contains(vLong)).isTrue();
      assertThat(range.contains(vLong - 1)).isFalse();
      assertThat(range.contains(vLong + 1)).isFalse();
    }
  }

  @Nested
  class FindLatestVersion {
    @Test
    void findsLatestAmongValid() {
      List<String> versions = List.of("1.0.0", "2.0.0", "1.5.10", "1.10.0", "2.0.1");
      assertThat(VersionUtils.findLatestVersion(versions)).isEqualTo("2.0.1");
    }

    @Test
    void ignoresInvalidVersions() {
      List<String> versions = List.of("invalid", "1.0.0", "1.0", "1.0.1", "1.0.0.0");
      assertThat(VersionUtils.findLatestVersion(versions)).isEqualTo("1.0.1");
    }

    @Test
    void allInvalidReturnsNull() {
      List<String> versions = List.of("", "abc", "1", "1.0", "1.0.0.0");
      assertThat(VersionUtils.findLatestVersion(versions)).isNull();
    }

    @Test
    void emptyListReturnsNull() {
      assertThat(VersionUtils.findLatestVersion(Collections.emptyList())).isNull();
    }
  }

  @Nested
  class CompareSemanticPartWithStrings {
    @Test
    void majorComparison() {
      assertThat(
              VersionUtils.compareSemanticPart("1.0.0", "2.0.0", VersionUtils.SemanticPart.MAJOR))
          .isNegative();
      assertThat(
              VersionUtils.compareSemanticPart("2.0.0", "1.0.0", VersionUtils.SemanticPart.MAJOR))
          .isPositive();
      assertThat(
              VersionUtils.compareSemanticPart("2.5.7", "2.9.1", VersionUtils.SemanticPart.MAJOR))
          .isZero();
    }

    @Test
    void minorComparisonSameMajor() {
      assertThat(
              VersionUtils.compareSemanticPart("1.1.0", "1.2.0", VersionUtils.SemanticPart.MINOR))
          .isNegative();
      assertThat(
              VersionUtils.compareSemanticPart("1.2.0", "1.1.0", VersionUtils.SemanticPart.MINOR))
          .isPositive();
      assertThat(
              VersionUtils.compareSemanticPart("1.2.3", "1.2.9", VersionUtils.SemanticPart.MINOR))
          .isZero();
    }

    @Test
    void patchComparison() {
      assertThat(
              VersionUtils.compareSemanticPart("1.0.1", "1.0.2", VersionUtils.SemanticPart.PATCH))
          .isNegative();
      assertThat(
              VersionUtils.compareSemanticPart("1.0.2", "1.0.1", VersionUtils.SemanticPart.PATCH))
          .isPositive();
      assertThat(
              VersionUtils.compareSemanticPart("2.3.4", "2.3.4", VersionUtils.SemanticPart.PATCH))
          .isZero();
    }
  }

  @Nested
  class CompareSemanticPartWithLongs {
    @Test
    void allParts() {
      long v1 = VersionUtils.toLong("3.4.5");
      long v2 = VersionUtils.toLong("4.0.0");
      assertThat(VersionUtils.compareSemanticPart(v1, v2, VersionUtils.SemanticPart.MAJOR))
          .isNegative();
      long v3 = VersionUtils.toLong("3.5.0");
      assertThat(VersionUtils.compareSemanticPart(v1, v3, VersionUtils.SemanticPart.MINOR))
          .isNegative();
      long v4 = VersionUtils.toLong("3.4.6");
      assertThat(VersionUtils.compareSemanticPart(v1, v4, VersionUtils.SemanticPart.PATCH))
          .isNegative();
    }
  }
}

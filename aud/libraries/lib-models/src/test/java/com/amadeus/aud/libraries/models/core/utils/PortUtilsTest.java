package com.amadeus.aud.libraries.models.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PortUtilsTest {

  @Nested
  class CreateRangeFromString {
    @Test
    void wildcard() {
      Range<Integer> expectedRange =
          Range.closed(PortUtils.LOWER_BOUND, PortUtils.UPPER_BOUND)
              .canonical(DiscreteDomain.integers());
      Range<Integer> actualRange = PortUtils.toRange("*");
      assertThat(actualRange).isEqualTo(expectedRange);
    }

    @Test
    void singlePort() {
      Range<Integer> expectedRange = Range.singleton(8080).canonical(DiscreteDomain.integers());
      Range<Integer> actualRange = PortUtils.toRange("8080");
      assertThat(actualRange).isEqualTo(expectedRange);
    }

    @Test
    void withRange() {
      Range<Integer> expectedRange = Range.closedOpen(1000, 2001);
      Range<Integer> actualRange = PortUtils.toRange("1000-2000");
      assertThat(actualRange).isEqualTo(expectedRange);
    }

    @Test
    void withInvalidInput() {
      assertThatThrownBy(() -> PortUtils.toRange("abc")).isInstanceOf(NumberFormatException.class);
    }
  }

  @Nested
  class CreateStringFromRange {
    @Test
    void wildcard() {
      Range<Integer> fullRange = Range.closedOpen(1, 65536);
      String expectedPortString = PortUtils.ALL;
      String actualPortString = PortUtils.toString(fullRange);
      assertThat(actualPortString).isEqualTo(expectedPortString);
    }

    @Test
    void singlePort() {
      Range<Integer> singlePortRange = Range.singleton(8080);
      String expectedPortString = "8080";
      String actualPortString = PortUtils.toString(singlePortRange);
      assertThat(actualPortString).isEqualTo(expectedPortString);
    }

    @Test
    void portRange() {
      Range<Integer> portRange = Range.closed(1000, 2000);
      String expectedPortString = "1000-2000";
      String actualPortString = PortUtils.toString(portRange);
      assertThat(actualPortString).isEqualTo(expectedPortString);
    }

    @Test
    void invalidRange() {
      assertThatThrownBy(
              () -> {
                Range<Integer> invalidRange = Range.closed(2000, 1000);
                PortUtils.toString(invalidRange);
              })
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class CreateRangeFromStringList {
    @Test
    void multipleSinglePorts() {
      Set<String> portsExpected =
          IntStream.rangeClosed(200, 300).mapToObj(String::valueOf).collect(Collectors.toSet());
      RangeSet<Integer> portsRange = TreeRangeSet.create();
      for (String port : portsExpected) {
        portsRange.add(PortUtils.toRange(port));
      }
      Set<Range<Integer>> ranges = portsRange.asRanges();
      assertThat(ranges).isNotEmpty().hasSize(1);
      Range<Integer> range = ranges.iterator().next();
      assertThat(PortUtils.toString(range)).isEqualTo("200-300");
    }

    @Test
    void adjacentSegmentsCoverAllPorts() {
      RangeSet<Integer> portsRange = TreeRangeSet.create();
      portsRange.add(PortUtils.toRange(PortUtils.LOWER_BOUND));
      portsRange.add(
          PortUtils.toRange((PortUtils.LOWER_BOUND + 1) + "-" + (PortUtils.UPPER_BOUND - 1)));
      portsRange.add(PortUtils.toRange(PortUtils.UPPER_BOUND));

      Set<Range<Integer>> ranges = portsRange.asRanges();
      assertThat(ranges).hasSize(1);
      Range<Integer> range = ranges.iterator().next();
      assertThat(PortUtils.toString(range)).isEqualTo(PortUtils.ALL);
    }
  }

  @Nested
  class ExpandCollapse {
    @Test
    void expand() {
      Set<String> ports = PortUtils.expand("7000-21000");

      Set<String> portsExpected =
          IntStream.rangeClosed(7000, 21000).mapToObj(String::valueOf).collect(Collectors.toSet());

      assertThat(ports).isEqualTo(portsExpected);
    }

    @Test
    void collapse() {
      String portCollapsed =
          PortUtils.collapse(
              IntStream.rangeClosed(7000, 21000)
                  .mapToObj(String::valueOf)
                  .collect(Collectors.toSet()));

      assertThat(portCollapsed).isEqualTo("7000-21000");
    }

    @Test
    void expandStar() {
      Set<String> ports = PortUtils.expand("*");

      String[] allPortsRangeSplit = PortUtils.ALL.split("-");

      Set<String> portsExpected =
          IntStream.rangeClosed(
                  Integer.parseInt(allPortsRangeSplit[0]), Integer.parseInt(allPortsRangeSplit[1]))
              .mapToObj(String::valueOf)
              .collect(Collectors.toSet());

      assertThat(ports).isEqualTo(portsExpected);
    }
  }

  @Nested
  class ToStrings {
    @Test
    void singlePort() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of("80"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("80");
    }

    @Test
    void multiplePorts() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of("80", "443", "8080"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("80", "443", "8080");
    }

    @Test
    void portRange() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of("1000-1003"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("1000-1003");
    }

    @Test
    void mixedSingleAndRange() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of("80", "1000-1002", "1002", "1003"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("80", "1000-1003");
    }

    @Test
    void overlappingRanges() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of("1000-1003", "1002-1005"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("1000-1005");
    }

    @Test
    void unsortedInput() {
      RangeSet<Integer> result =
          PortUtils.toRangeSet(List.of("1005", "1001", "1003", "1002", "1004"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("1001-1005");
    }

    @Test
    void withWhitespace() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of(" 80 ", "  81", "82 ", "83-85"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("80-85");
    }

    @Test
    void allWildcard() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of("*"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("1-65535");
    }

    @Test
    void allRange() {
      RangeSet<Integer> result = PortUtils.toRangeSet(List.of("1-65535"));

      List<String> ports = PortUtils.toStrings(result);

      assertThat(ports).containsExactly("1-65535");
    }

    @Test
    void emptyRangeSet() {
      RangeSet<Integer> emptySet = TreeRangeSet.create();

      List<String> ports = PortUtils.toStrings(emptySet);

      assertThat(ports).isEmpty();
    }

    @Test
    void multipleRanges() {
      RangeSet<Integer> set = TreeRangeSet.create();
      set.add(PortUtils.toRange("80"));
      set.add(PortUtils.toRange("1000-1002"));
      set.add(PortUtils.toRange("1004"));

      List<String> ports = PortUtils.toStrings(set);

      assertThat(ports).containsExactly("80", "1000-1002", "1004");
    }
  }

  @Nested
  class FindDuplicates {
    @Test
    void noneSingle() {
      List<String> data = List.of("80", "443", "8080");

      List<String> duplicates = PortUtils.findDuplicates(data);

      assertThat(duplicates).isEmpty();
    }

    @Test
    void singleDuplicates() {
      List<String> data = List.of("80", "443", "80");

      List<String> duplicates = PortUtils.findDuplicates(data);

      assertThat(duplicates).containsExactly("80");
    }

    @Test
    void noneRange() {
      List<String> data = List.of("80-81", "222-443");

      List<String> duplicates = PortUtils.findDuplicates(data);

      assertThat(duplicates).isEmpty();
    }

    @Test
    void duplicateSingleAndRange() {
      List<String> data = List.of("80-82", "81", "81");

      List<String> duplicates = PortUtils.findDuplicates(data);

      assertThat(duplicates).containsExactly("81");
    }

    @Test
    void duplicateMixed() {
      List<String> data = List.of("80-82", "43", "22-100");

      List<String> duplicates = PortUtils.findDuplicates(data);

      assertThat(duplicates).containsExactly("43", "80-82");
    }

    @Test
    void duplicateMixedPartialOverlap() {
      List<String> data = List.of("80-82", "43", "22-81");

      List<String> duplicates = PortUtils.findDuplicates(data);

      assertThat(duplicates).containsExactly("43", "80-81");
    }

    @Test
    void duplicateMixedMultipleIntervals() {
      List<String> data = List.of("80-82", "43", "22-100", "55-58");

      List<String> duplicates = PortUtils.findDuplicates(data);

      assertThat(duplicates).containsExactly("43", "55-58", "80-82");
    }
  }
}

package com.amadeus.aud.libraries.models.core.utils;

import com.google.common.collect.BoundType;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This class is used to create a range of Ports from a string. We can have a range of ports or a
 * single port. We can also have all ports, which is represented by "*". Ports range from what is
 * defined in the ALL_PORTS_RANGE constant.
 */
public final class PortUtils {
  public static final String ALL = "1-65535";
  public static final String ALL_WILDCARD = "*";
  public static final List<String> AZURE_PASSTHRU_PORTS =
      Arrays.asList("1-1023", "1024-49151", "49152-65535");

  public static final List<String> ALL_LIST;
  public static final Range<Integer> ALL_RANGE;
  public static final RangeSet<Integer> ALL_RANGE_SET;
  public static final int LOWER_BOUND;
  public static final int UPPER_BOUND;

  static {
    String[] parts = ALL.split("-");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid ports range: " + ALL);
    }
    LOWER_BOUND = Integer.parseInt(parts[0]);
    UPPER_BOUND = Integer.parseInt(parts[1]);
    if (LOWER_BOUND > UPPER_BOUND || LOWER_BOUND < 0 || UPPER_BOUND > 65535) {
      throw new IllegalArgumentException("Invalid ports range: " + ALL);
    }

    ALL_LIST = List.of(ALL);
    ALL_RANGE = toRange(ALL);
    ALL_RANGE_SET = TreeRangeSet.create();
    ALL_RANGE_SET.add(ALL_RANGE);
  }

  private PortUtils() {}

  public static @Nullable Range<Integer> toRange(@Nullable Integer port) {
    if (port == null) {
      return null;
    }

    return Range.singleton(port).canonical(DiscreteDomain.integers());
  }

  public static @Nullable Range<Integer> toRange(@Nullable String ports) {
    if (ports == null || ports.isEmpty()) {
      return null;
    }

    Range<Integer> range;
    if (ALL_WILDCARD.equals(ports)) {
      range = Range.closed(LOWER_BOUND, UPPER_BOUND);
    } else {
      int dashIndex = ports.indexOf('-');
      range =
          (dashIndex == -1)
              ? Range.singleton(Integer.parseInt(ports.trim()))
              : Range.closed(
                  Integer.parseInt(ports.substring(0, dashIndex).trim()),
                  Integer.parseInt(ports.substring(dashIndex + 1).trim()));
    }

    return range.canonical(DiscreteDomain.integers());
  }

  public static @Nullable RangeSet<Integer> toRangeSet(@Nullable Collection<String> ports) {
    if (ports == null) {
      return null;
    }

    RangeSet<Integer> rangeSet = TreeRangeSet.create();
    for (String port : ports) {
      rangeSet.addAll(toRangeSet(port));
    }
    return rangeSet;
  }

  public static @Nullable RangeSet<Integer> toRangeSet(String ports) {
    return toRangeSet(ports, ",");
  }

  public static @Nullable RangeSet<Integer> toRangeSet(@Nullable String ports, String separator) {
    if (ports == null) {
      return null;
    }

    RangeSet<Integer> rangeSet = TreeRangeSet.create();
    for (String s : ports.split(separator)) {
      rangeSet.add(toRange(s));
    }
    return rangeSet;
  }

  public static @Nullable String toString(@Nullable Range<Integer> portRange) {
    if (portRange == null) {
      return null;
    }

    Integer startPort = portRange.lowerEndpoint();
    if (BoundType.OPEN == portRange.lowerBoundType()) {
      startPort++;
    }
    if (portRange.hasUpperBound()) {
      Integer endPort = portRange.upperEndpoint();
      if (BoundType.OPEN == portRange.upperBoundType()) {
        endPort--;
      }

      if (startPort <= LOWER_BOUND && endPort >= UPPER_BOUND) {
        return ALL;
      }
      if (!startPort.equals(endPort)) {
        return startPort + "-" + endPort;
      }
    }
    return startPort.toString();
  }

  public static String toString(RangeSet<Integer> portRangeSet) {
    return String.join(",", toStrings(portRangeSet));
  }

  public static String toString(RangeSet<Integer> portRangeSet, String separator) {
    return String.join(separator, toStrings(portRangeSet));
  }

  public static @Nullable List<String> toStrings(@Nullable RangeSet<Integer> portRangeSet) {
    if (portRangeSet == null) {
      return null;
    }

    List<String> result = new ArrayList<>();
    for (Range<Integer> range : portRangeSet.asRanges()) {
      result.add(toString(range));
    }
    return result;
  }

  /**
   * This function takes in a string, which is in a ranged form i.e. "1,2,3-6,8" and converts it to
   * and expanded form i.e. [1,2,3,4,5,6,8]
   *
   * @param range String of ranges to be expanded i.e. "1,2,3-6,8"
   * @return Set of strings. Expanded ranges i.e. [1,2,3,4,5,6,8]
   */
  public static @Nullable Set<String> expand(@Nullable String range) {
    if (range == null) {
      return null;
    }

    Set<String> result = new HashSet<>();

    // Trim the input string
    String content = range.trim();

    if (Objects.equals(content, ALL_WILDCARD)) {
      content = ALL;
    }

    // Split the string by commas
    String[] tokens = content.split(",");

    for (String token : tokens) {
      token = token.trim();
      int dashIndex = token.indexOf('-');
      if (dashIndex != -1) {
        // Handle ranges
        int start = Integer.parseInt(token.substring(0, dashIndex).trim());
        int end = Integer.parseInt(token.substring(dashIndex + 1).trim());
        for (int i = start; i <= end; i++) {
          result.add(String.valueOf(i));
        }
      } else {
        // Handle single numbers
        int value = Integer.parseInt(token);
        result.add(String.valueOf(value));
      }
    }

    return result;
  }

  /**
   * This function takes in a set of strings representing individual ports and converts it to a
   * collapsed form i.e. [1,2,3,4,5,6,8] to "1-6,8"
   *
   * @param ports Set of strings representing individual ports i.e. [1,2,3,4,5,6,8]
   * @return String of collapsed ranges i.e. "1-6,8"
   */
  public static String collapse(@Nullable Set<String> ports) {
    if (ports == null || ports.isEmpty()) {
      return "";
    }

    List<Integer> portList = new ArrayList<>();
    for (String port : ports) {
      portList.add(Integer.parseInt(port));
    }
    Collections.sort(portList);

    StringBuilder collapsed = new StringBuilder();
    int start = portList.getFirst();
    int end = start;

    for (int i = 1; i < portList.size(); i++) {
      int current = portList.get(i);
      if (current != end + 1) {
        if (start == end) {
          collapsed.append(start).append(",");
        } else {
          collapsed.append(start).append("-").append(end).append(",");
        }
        start = current;
      }
      end = current;
    }

    if (start == end) {
      collapsed.append(start);
    } else {
      collapsed.append(start).append("-").append(end);
    }

    return collapsed.toString();
  }

  /**
   * Find duplicate or overlapping ports in a list of port strings.
   *
   * @param ports must be a list a string, each string being either a single port (e.g. "80") or a
   *     range of ports (e.g. "1000-2000"). No comma-separated values allowed.
   * @return duplicate or overlapping ports as a list of strings.
   */
  public static @Nullable List<String> findDuplicates(Iterable<String> ports) {
    RangeSet<Integer> seen = TreeRangeSet.create();
    RangeSet<Integer> duplicates = TreeRangeSet.create();

    for (String port : ports) {
      Range<Integer> range = toRange(port);
      RangeSet<Integer> intersection = seen.subRangeSet(range);

      if (!intersection.isEmpty()) {
        duplicates.addAll(intersection);
      }

      seen.add(range);
    }

    return toStrings(duplicates);
  }
}

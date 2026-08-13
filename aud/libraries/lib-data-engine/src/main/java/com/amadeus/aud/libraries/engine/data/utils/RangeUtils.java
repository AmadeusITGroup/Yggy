package com.amadeus.aud.libraries.engine.data.utils;

import com.google.common.collect.Range;
import jakarta.annotation.Nullable;

public final class RangeUtils {
  private RangeUtils() {}

  /**
   * The method computes the intersection between two ranges. It is doing so by leveraging the Guava
   * library. Unlike the Guava Library, the method is not throwing an exception when there is no
   * intersection between the two ranges. Instead, it is returning null.
   *
   * @param range1 The first range
   * @param range2 The second range
   * @param <T> The type of the range
   * @return The intersection between the two ranges or null if the intersection is empty
   */
  public static <T extends Comparable<T>> @Nullable Range<T> intersection(
      @Nullable Range<T> range1, @Nullable Range<T> range2) {
    if (range1 != null && range2 != null) {
      try {
        Range<T> intersection = range1.intersection(range2);
        if (intersection.isEmpty()) {
          return null;
        }
        return intersection;
      } catch (IllegalArgumentException e) {
        return null;
      }
    }
    return null;
  }
}

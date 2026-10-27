package com.amadeus.aud.libraries.common;

/** Utility class for StringBuilder and StringBuffer operations. */
public final class SBUtils {
  private SBUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void reset(StringBuilder s) {
    s.setLength(0);
  }

  public static void reset(StringBuffer s) {
    s.setLength(0);
  }

  public static void removeLastChar(StringBuilder s) {
    if (!s.isEmpty()) {
      s.setLength(s.length() - 1);
    }
  }

  public static void removeLastChar(StringBuffer s) {
    if (!s.isEmpty()) {
      s.setLength(s.length() - 1);
    }
  }
}

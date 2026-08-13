package com.amadeus.aud.libraries.engine.graph.utils;

import jakarta.annotation.Nullable;

public final class StringUtils {

  private StringUtils() {}

  /**
   * Properly escape CSV values to handle commas, quotes, and newlines According to RFC 4180 CSV
   * specification
   */
  public static String escapeCsvValue(@Nullable String value) {
    if (value == null) {
      return "";
    }

    // If the value contains comma, quote, or newline, we need to quote and escape it
    if (value.contains(",")
        || value.contains("\"")
        || value.contains("\n")
        || value.contains("\r")) {
      // Escape any existing quotes by doubling them
      String escaped = value.replace("\"", "\"\"");
      // Wrap the entire value in quotes
      return "\"" + escaped + "\"";
    }

    return value;
  }

  /**
   * Escape values for PostgreSQL TEXT format (tab-delimited) Only needs to escape tabs, newlines,
   * carriage returns, and backslashes Much more efficient than CSV escaping
   */
  public static String escapeTextValue(@Nullable String value) {
    if (value == null) {
      // PostgreSQL NULL representation in TEXT format
      return "\\N";
    }

    // Only escape if the value contains special characters
    if (value.contains("\t")
        || value.contains("\n")
        || value.contains("\r")
        || value.contains("\\")) {
      return value
          .replace("\\", "\\\\")
          .replace("\t", "\\t")
          .replace("\n", "\\n")
          .replace("\r", "\\r");
    }

    return value;
  }
}

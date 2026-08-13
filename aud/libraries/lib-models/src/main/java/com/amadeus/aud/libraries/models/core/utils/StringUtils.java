package com.amadeus.aud.libraries.models.core.utils;

import jakarta.annotation.Nullable;
import java.util.UUID;

public final class StringUtils {
  public static final UUID rootUUID = UUID.fromString("4369b435-9028-430b-a3f2-2927967a5841");

  private StringUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Normalizes the input string by converting it to lowercase, preserving hyphens ('-'), replacing
   * other non-alphanumeric characters with underscores, and collapsing consecutive underscores into
   * a single underscore. Leading and trailing underscores are removed.
   *
   * <p>The logic that is applied is equivalent to the following regex-based implementation:
   *
   * <pre>{@code
   * String tmpStr = Pattern.compile("(^_+)|(_+$)|_+|[^a-zA-Z0-9_-]").matcher(inputStr).replaceAll("_");
   * // Collapse multiple underscores to one
   * tmpStr = Pattern.compile("_+").matcher(tmpStr).replaceAll("_");
   * // Remove leading/trailing underscores
   * tmpStr = Pattern.compile("(^_+)|(_+$)").matcher(tmpStr).replaceAll("");
   * return tmpStr.toLowerCase(Locale.ROOT);
   * }</pre>
   *
   * IMPORTANT: This implementation avoids using regular expressions for performance reasons.
   *
   * @param inputStr the input string to normalize
   * @return the normalized string
   */
  public static String normalize(String inputStr) {
    if (inputStr.isEmpty()) {
      return inputStr;
    }

    StringBuilder result = new StringBuilder(inputStr.length());
    boolean lastWasUnderscore = false;

    for (int i = 0; i < inputStr.length(); i++) {
      char ch = inputStr.charAt(i);

      if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
        // Lowercase letter or digit - append as-is
        result.append(ch);
        lastWasUnderscore = false;
      } else if (ch >= 'A' && ch <= 'Z') {
        // Uppercase letter - convert to lowercase and append
        result.append((char) (ch + 32));
        lastWasUnderscore = false;
      } else if (ch == '-') {
        // Hyphen is preserved verbatim (old behavior)
        result.append('-');
        lastWasUnderscore = false;
      } else if (ch == '_') {
        // Underscore - collapse sequences and avoid leading underscore
        if (!lastWasUnderscore && !result.isEmpty()) {
          result.append('_');
          lastWasUnderscore = true;
        }
      } else {
        // Non-alphanumeric & not hyphen/underscore - map to underscore separator (skip if leading)
        if (!lastWasUnderscore && !result.isEmpty()) {
          result.append('_');
          lastWasUnderscore = true;
        }
      }
    }

    // Remove trailing underscore if present
    if (!result.isEmpty() && result.charAt(result.length() - 1) == '_') {
      result.setLength(result.length() - 1);
    }

    return result.toString();
  }

  /**
   * Protects a string for safe inclusion in a CSV file by escaping special characters.
   *
   * <p>The following transformations are applied:
   *
   * <ul>
   *   <li>Commas (`,`) are escaped by enclosing the entire string in double quotes (`"`).
   *   <li>Double quotes (`"`) are replaced with single quotes (`'`).
   *   <li>Tabs (`\t`), newlines (`\n`), and carriage returns (`\r`) are replaced with spaces.
   * </ul>
   *
   * @param data the input string to protect
   * @return the protected string suitable for CSV inclusion
   */
  public static String csvProtect(@Nullable String data) {
    if (data == null || data.isEmpty()) {
      return "";
    }
    int len = data.length();
    int i = 0;
    // Fast path: detect if any character needs replacement
    for (; i < len; i++) {
      char c = data.charAt(i);
      if (c == ',' || c == '"' || c == '\t' || c == '\n' || c == '\r') {
        break;
      }
    }
    if (i == len) {
      return data; // No changes needed; reuse original String
    }

    boolean quoted = false;
    StringBuilder sb = new StringBuilder(len);
    sb.append(data, 0, i);
    for (; i < len; i++) {
      char c = data.charAt(i);
      switch (c) {
        case ',':
          if (!quoted) {
            sb.insert(0, '"');
            quoted = true;
          }
          sb.append(c);
          break;
        case '"':
          sb.append('\'');
          break;
        case '\t':
        case '\n':
        case '\r':
          sb.append(' ');
          break;
        default:
          sb.append(c);
      }
    }
    if (quoted) {
      sb.append('"');
    }
    return sb.toString();
  }
}

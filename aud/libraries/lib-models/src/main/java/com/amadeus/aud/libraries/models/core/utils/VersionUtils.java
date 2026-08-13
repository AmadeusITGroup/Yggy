package com.amadeus.aud.libraries.models.core.utils;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import io.quarkus.logging.Log;
import java.util.List;
import java.util.regex.Pattern;

public final class VersionUtils {

  public enum SemanticPart {
    MAJOR,
    MINOR,
    PATCH
  }

  private VersionUtils() {}

  private static final Pattern pattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
  private static final String format = "%d.%d.%d";
  private static final long MAJOR_MULTIPLIER = 1_000_000_000_000L;
  private static final long MINOR_MULTIPLIER = 1_000_000_000L;

  public static boolean isSemver(String v) {
    return v != null && pattern.matcher(v).matches();
  }

  public static Range<Long> toRange(String version) {
    long versionLong = toLong(version);
    return Range.closed(versionLong, versionLong).canonical(DiscreteDomain.longs());
  }

  public static long toLong(String version) {
    if (!isSemver(version)) {
      throw new IllegalArgumentException("Invalid version format");
    }

    int firstDot = version.indexOf('.');
    int secondDot = version.indexOf('.', firstDot + 1);

    long major = Long.parseLong(version.substring(0, firstDot));
    long minor = Long.parseLong(version.substring(firstDot + 1, secondDot));
    long patch = Long.parseLong(version.substring(secondDot + 1));

    return (major * MAJOR_MULTIPLIER) + (minor * MINOR_MULTIPLIER) + patch;
  }

  public static String toString(long version) {
    return String.format(
        format,
        version / MAJOR_MULTIPLIER,
        (version % MAJOR_MULTIPLIER) / MINOR_MULTIPLIER,
        version % MINOR_MULTIPLIER);
  }

  /**
   * Find the latest version from a list of versions.
   *
   * @param versions The list of available versions
   * @return The latest version, or null if no valid versions found
   */
  public static String findLatestVersion(List<String> versions) {
    String latestVersion = null;
    long latestVersionLong = Long.MIN_VALUE;
    for (String version : versions) {
      try {
        long versionLong = toLong(version);
        if (versionLong > latestVersionLong) {
          latestVersionLong = versionLong;
          latestVersion = version;
        }
      } catch (IllegalArgumentException e) {
        Log.warnf("Skipping invalid version: %s", version);
      }
    }
    return latestVersion;
  }

  public static int compareSemanticPart(String v1, String v2, SemanticPart part) {
    return compareSemanticPart(toLong(v1), toLong(v2), part);
  }

  public static int compareSemanticPart(long v1, long v2, SemanticPart part) {
    return switch (part) {
      case MAJOR -> compareSemanticPart(v1, v2, 0, MAJOR_MULTIPLIER);
      case MINOR -> compareSemanticPart(v1, v2, MAJOR_MULTIPLIER, MINOR_MULTIPLIER);
      case PATCH -> compareSemanticPart(v1, v2, MINOR_MULTIPLIER, 1);
    };
  }

  private static int compareSemanticPart(long v1, long v2, long modulus, long divisor) {
    long mod = modulus == 0 ? Long.MAX_VALUE : modulus;
    return Long.compare((v1 % mod) / divisor, (v2 % mod) / divisor);
  }
}

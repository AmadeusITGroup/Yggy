package com.amadeus.aud.libraries.engine.data.utils;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import jakarta.annotation.Nullable;
import java.util.Locale;

/**
 * This method is used to create a range of protocol ports from a string. We map each protocol to a
 * number. Since we have only 3 protocols, we can map them to the following numbers:
 *
 * <ul>
 *   <li>"tcp" -> 0
 *   <li>"udp" -> 1
 *   <li>"icmp" -> 2
 *   <li>"*" -> [0, 2]
 * </ul>
 */
public final class ProtocolUtils {
  public static final String ALL_WILDCARD = "*";

  private static final Range<Integer> TCP_RANGE = Range.singleton(0);
  private static final Range<Integer> UDP_RANGE = Range.singleton(1);
  private static final Range<Integer> ICMP_RANGE = Range.singleton(2);
  private static final Range<Integer> ALL_RANGE = Range.closed(0, 2);

  private ProtocolUtils() {}

  public static @Nullable Range<Integer> toRange(@Nullable String protocol) {
    if (protocol == null || protocol.isEmpty()) {
      return null;
    }

    return switch (protocol.toLowerCase(Locale.ROOT)) {
      case "tcp" -> TCP_RANGE;
      case "udp" -> UDP_RANGE;
      case "icmp" -> ICMP_RANGE;
      case ALL_WILDCARD -> ALL_RANGE;
      default -> throw new IllegalArgumentException("Invalid protocol: " + protocol);
    };
  }

  public static @Nullable String toString(@Nullable Range<Integer> protocolRange) {
    if (protocolRange == null) {
      return null;
    }

    Integer startProtocol = protocolRange.lowerEndpoint();
    if (protocolRange.hasUpperBound()) {
      Integer endProtocol = protocolRange.upperEndpoint();
      if (BoundType.OPEN.equals(protocolRange.upperBoundType())) {
        endProtocol--;
      }

      if (startProtocol == 0 && endProtocol >= 2) {
        return ALL_WILDCARD;
      }
      if (!startProtocol.equals(endProtocol)) {
        throw new IllegalArgumentException("Invalid protocol range: " + protocolRange);
      }
    }
    return switch (startProtocol) {
      case 0 -> "Tcp";
      case 1 -> "Udp";
      case 2 -> "Icmp";
      default -> throw new IllegalArgumentException("Invalid protocol: " + startProtocol);
    };
  }
}

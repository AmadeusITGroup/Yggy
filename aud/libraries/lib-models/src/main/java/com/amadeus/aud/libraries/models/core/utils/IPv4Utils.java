package com.amadeus.aud.libraries.models.core.utils;

import com.amadeus.aud.libraries.models.core.Constants;
import com.google.common.collect.BoundType;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import com.google.common.net.InetAddresses;
import jakarta.annotation.Nullable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.net.util.SubnetUtils;

public final class IPv4Utils {
  public static final String ALL = "0.0.0.0/0";
  public static final String ANY = "ANY";
  public static final String ALL_WILDCARD = "*";
  private static final Range<BigInteger> ALL_RANGE;
  public static final RangeSet<BigInteger> ALL_RANGE_SET;

  private static final BigInteger BIG_INTEGER_255 = BigInteger.valueOf(0xFF);
  private static final BigInteger BIG_INTEGER_MAX_IPv4 =
      BigInteger.valueOf(2).pow(32).subtract(BigInteger.ONE);

  private static final Map<Integer, BigInteger> MASKS = new HashMap<>();
  private static final Map<Integer, BigInteger> MASKS_NOT = new HashMap<>();

  private IPv4Utils() {}

  static {
    for (int i = 0; i <= 32; i++) {
      BigInteger mask = BigInteger.ONE.shiftLeft(i).subtract(BigInteger.ONE);
      MASKS.put(i, mask);
      MASKS_NOT.put(i, mask.not());
    }
    ALL_RANGE = toRange(ALL);
    ALL_RANGE_SET = TreeRangeSet.create();
    ALL_RANGE_SET.add(ALL_RANGE);
  }

  private static BigInteger ipToBigInteger(InetAddress ip) {
    if (ip == null) {
      return null;
    }

    return new BigInteger(1, ip.getAddress());
  }

  public static @Nullable Range<BigInteger> toRange(@Nullable String cidr) {
    if (cidr == null || cidr.isEmpty()) {
      return null;
    }

    if (ALL_WILDCARD.equals(cidr)) {
      return Range.closed(BigInteger.ZERO, BIG_INTEGER_MAX_IPv4)
          .canonical(DiscreteDomain.bigIntegers());
    }

    CharSequence ipString;
    int prefixLength;
    int slashIndex = cidr.indexOf('/');
    if (slashIndex != -1) {
      ipString = cidr.subSequence(0, slashIndex);
      prefixLength = Integer.parseInt(cidr.substring(slashIndex + 1));
    } else {
      ipString = cidr;
      prefixLength = 32;
    }
    InetAddress inetAddress = InetAddresses.forUriString(ipString.toString());
    int addressLength = inetAddress.getAddress().length * Byte.SIZE;
    if (addressLength != 32 || prefixLength < 0 || prefixLength > addressLength) {
      throw new IllegalArgumentException("Invalid IPv4 CIDR: " + cidr);
    }
    int maskLength = addressLength - prefixLength;

    BigInteger baseIP = IPv4Utils.ipToBigInteger(inetAddress);
    BigInteger startIP = baseIP.and(MASKS_NOT.get(maskLength));
    BigInteger endIP = startIP.add(MASKS.get(maskLength));

    return Range.closed(startIP, endIP).canonical(DiscreteDomain.bigIntegers());
  }

  public static List<String> sortedCidrs(Collection<String> cidrs) {
    int size = cidrs.size();
    if (size < 2) {
      return new ArrayList<>(cidrs);
    }

    List<SortableCidr> sortableCidrs = new ArrayList<>(size);
    for (String cidr : cidrs) {
      sortableCidrs.add(new SortableCidr(cidr, parseRange(cidr)));
    }
    sortableCidrs.sort(IPv4Utils::compareCidrs);

    List<String> sortedCidrs = new ArrayList<>(size);
    for (SortableCidr sortableCidr : sortableCidrs) {
      sortedCidrs.add(sortableCidr.cidr());
    }
    return sortedCidrs;
  }

  private static @Nullable Range<BigInteger> parseRange(String cidr) {
    try {
      return toRange(cidr);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static int compareCidrs(SortableCidr left, SortableCidr right) {
    Range<BigInteger> leftRange = left.range();
    Range<BigInteger> rightRange = right.range();
    if (leftRange == null || rightRange == null) {
      if (leftRange == null && rightRange == null) {
        return 0;
      }
      return leftRange == null ? -1 : 1;
    }

    int startComparison = leftRange.lowerEndpoint().compareTo(rightRange.lowerEndpoint());
    if (startComparison != 0) {
      return startComparison;
    }
    int endComparison = rightRange.upperEndpoint().compareTo(leftRange.upperEndpoint());
    if (endComparison != 0) {
      return endComparison;
    }
    return String.CASE_INSENSITIVE_ORDER.compare(left.cidr(), right.cidr());
  }

  private record SortableCidr(String cidr, @Nullable Range<BigInteger> range) {}

  public static @Nullable RangeSet<BigInteger> toRangeSet(@Nullable Collection<String> cidr) {
    if (cidr == null || cidr.isEmpty()) {
      return null;
    }

    RangeSet<BigInteger> rangeSet = TreeRangeSet.create();
    for (String cidrString : cidr) {
      rangeSet.add(toRange(cidrString));
    }

    return rangeSet;
  }

  public static @Nullable List<String> toStrings(@Nullable Range<BigInteger> ipRange) {
    if (ipRange == null) {
      return null;
    }

    BigInteger startIP = ipRange.lowerEndpoint();
    if (BoundType.OPEN == ipRange.lowerBoundType()) {
      startIP = startIP.add(BigInteger.ONE);
    }
    BigInteger endIP = ipRange.upperEndpoint();
    if (BoundType.OPEN == ipRange.upperBoundType()) {
      endIP = endIP.subtract(BigInteger.ONE);
    }

    if (startIP.compareTo(endIP) > 0) {
      return Collections.emptyList();
    }

    List<String> resultList = new ArrayList<>();

    // Ensure the endIP does not exceed the max IPv4
    if (endIP.compareTo(BIG_INTEGER_MAX_IPv4) > 0) {
      endIP = BIG_INTEGER_MAX_IPv4;
    }

    while (startIP.compareTo(endIP) <= 0) {
      // Find the max size of the block starting at startIP that is aligned
      int maxBlockSize = startIP.getLowestSetBit();
      if (maxBlockSize == -1) {
        maxBlockSize = 32;
      }
      int maxAlignedPrefixLength = 32 - maxBlockSize;

      // Calculate the maximum prefix length allowed by the range size
      BigInteger maxIPs = endIP.subtract(startIP).add(BigInteger.ONE);
      int maxRangePrefixLength =
          32 - (int) (Math.floor(Math.log(maxIPs.doubleValue()) / Math.log(2)));

      // Choose the largest prefix length
      int prefixLength = Math.max(maxAlignedPrefixLength, maxRangePrefixLength);
      resultList.add(getCidrString(startIP, prefixLength));

      // Update startIP to the next block
      BigInteger blockSize = BigInteger.ONE.shiftLeft(32 - prefixLength);
      startIP = startIP.add(blockSize);
    }

    return resultList;
  }

  /**
   * Converts a Range to a single CIDR string representation. Similar to the old createCIDRFromRange
   * but in IPv4Utils.
   *
   * @param ipRange the IP range to convert
   * @return single CIDR string or "*" for full range
   */
  public static @Nullable String toString(@Nullable Range<BigInteger> ipRange) {
    if (ipRange == null) {
      return null;
    }

    BigInteger startIP = ipRange.lowerEndpoint();
    if (BoundType.OPEN == ipRange.lowerBoundType()) {
      startIP = startIP.add(BigInteger.ONE);
    }
    BigInteger endIP = ipRange.upperEndpoint();
    if (BoundType.OPEN == ipRange.upperBoundType()) {
      endIP = endIP.subtract(BigInteger.ONE);
    }

    if (startIP.compareTo(endIP) > 0) {
      return null;
    }

    String result;
    if (startIP.compareTo(BigInteger.ZERO) == 0 && endIP.compareTo(BIG_INTEGER_MAX_IPv4) == 0) {
      result = ALL_WILDCARD;
    } else {
      int totalBits = 32;
      BigInteger diff = endIP.subtract(startIP);

      // Find the minimum prefix length that covers the range
      int prefixLength = totalBits;
      while (diff.compareTo(BigInteger.ZERO) > 0) {
        diff = diff.shiftRight(1);
        prefixLength--;
      }

      // Ensure the CIDR starts at the network boundary
      BigInteger mask = MASKS_NOT.get(32 - prefixLength);
      BigInteger networkStart = startIP.and(mask);

      result = getCidrString(networkStart, prefixLength);
    }
    return result;
  }

  public static boolean isValidIpAddress(String ipString) {
    // Exclude CIDR and hostnames
    return InetAddresses.isInetAddress(ipString);
  }

  public static boolean isIpInEndpoint(@Nullable String ipAddress, @Nullable String endpointStr) {
    ipAddress = ipAddress == null ? null : ipAddress.trim();
    endpointStr = endpointStr == null ? null : endpointStr.trim();
    if (ipAddress == null || endpointStr == null || ipAddress.isEmpty() || endpointStr.isEmpty()) {
      return false;
    }

    if (!isValidIpAddress(ipAddress)) {
      return false;
    }

    if (endpointStr.contains("/")) {
      InetAddress targetIp = InetAddresses.forUriString(ipAddress);
      BigInteger targetIpBigInt = ipToBigInteger(targetIp);
      Range<BigInteger> endpointRange = toRange(endpointStr);
      return endpointRange != null && endpointRange.contains(targetIpBigInt);
    } else {
      if (!isValidIpAddress(endpointStr)) {
        return false;
      }
      return ipAddress.equals(endpointStr);
    }
  }

  private static String getCidrString(BigInteger startIP, int prefixLength) {
    final int macIPv4CidrLength = 18;
    final int blockSize = 8;
    StringBuilder cidr = new StringBuilder(macIPv4CidrLength);
    cidr.append(startIP.shiftRight(blockSize * 3).and(BIG_INTEGER_255))
        .append('.')
        .append(startIP.shiftRight(blockSize * 2).and(BIG_INTEGER_255))
        .append('.')
        .append(startIP.shiftRight(blockSize * 1).and(BIG_INTEGER_255))
        .append('.')
        .append(startIP.and(BIG_INTEGER_255));
    cidr.append('/').append(prefixLength);
    return cidr.toString();
  }

  public static Optional<SubnetUtils.SubnetInfo> parseCidr(@Nullable String cidr) {
    if (cidr == null || cidr.isEmpty()) {
      return Optional.empty();
    }
    try {
      SubnetUtils subnetUtils = new SubnetUtils(cidr);
      subnetUtils.setInclusiveHostCount(true);
      SubnetUtils.SubnetInfo info = subnetUtils.getInfo();

      String[] parts = cidr.split("/");
      boolean isValid = parts[0].equals(info.getNetworkAddress());

      return isValid ? Optional.of(info) : Optional.empty();
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static boolean cidrsOverlap(SubnetUtils.SubnetInfo cidr1, SubnetUtils.SubnetInfo cidr2) {
    return cidr1.isInRange(cidr2.getNetworkAddress())
        || cidr1.isInRange(cidr2.getBroadcastAddress())
        || cidr2.isInRange(cidr1.getNetworkAddress())
        || cidr2.isInRange(cidr1.getBroadcastAddress());
  }

  public static boolean isCidrPrivate(String cidr) {
    return parseCidr(cidr).map(IPv4Utils::isCidrPrivate).orElse(false);
  }

  public static boolean isCidrPrivate(SubnetUtils.SubnetInfo cidr) {
    return cidrsOverlap(Constants.PrivateIpRanges.SUBNET_10, cidr)
        || cidrsOverlap(Constants.PrivateIpRanges.SUBNET_172, cidr)
        || cidrsOverlap(Constants.PrivateIpRanges.SUBNET_192, cidr);
  }

  public static boolean isIpPrivate(String ipAddress) {
    return IPv4Utils.isValidIpAddress(ipAddress)
        && (IPv4Utils.isIpInEndpoint(ipAddress, Constants.PrivateIpRanges.CIDR_10)
            || IPv4Utils.isIpInEndpoint(ipAddress, Constants.PrivateIpRanges.CIDR_172)
            || IPv4Utils.isIpInEndpoint(ipAddress, Constants.PrivateIpRanges.CIDR_192));
  }
}

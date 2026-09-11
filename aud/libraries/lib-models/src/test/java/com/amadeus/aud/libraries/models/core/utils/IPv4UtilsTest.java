package com.amadeus.aud.libraries.models.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/// All addresses here are synthetic fixtures chosen only for their arithmetic:
/// 10.1.x for the /22-/23 family, 10.2.x for the /25-/26 split family and 10.3.x
/// for the boundary-overlap family. They are not, and must not be replaced by,
/// addresses from any real network. Where a test pairs a decimal `BigInteger` with
/// an expected CIDR string, the two have to be changed together.
class IPv4UtilsTest {

  @Nested
  class CreateRangeFromCIDR {
    @Test
    void withWildcard() {
      Range<BigInteger> expectedRange =
          Range.closed(BigInteger.ZERO, BigInteger.valueOf(2).pow(32).subtract(BigInteger.ONE))
              .canonical(DiscreteDomain.bigIntegers());

      Range<BigInteger> actualRange = IPv4Utils.toRange("*");

      assertThat(actualRange).isEqualTo(expectedRange);
    }

    @Test
    void withValidCIDR() {
      String cidr = "192.168.1.0/24";
      BigInteger startIP = new BigInteger("3232235776"); // 192.168.1.0
      BigInteger endIP = new BigInteger("3232236032"); // 192.168.1.255
      Range<BigInteger> expectedRange = Range.closedOpen(startIP, endIP);

      Range<BigInteger> actualRange = IPv4Utils.toRange(cidr);

      assertThat(actualRange).isEqualTo(expectedRange);
    }

    @Test
    void withInvalidCIDR() {
      assertThatThrownBy(() -> IPv4Utils.toRange("256.256.256.256/32"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withInvalidPrefix() {
      assertThatThrownBy(() -> IPv4Utils.toRange("10.0.0.0/33"))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality() {
      // Both mask to the same /22 network (10.1.0.0/22), so the ranges are equal
      // even though the strings differ.
      Range<BigInteger> range1 = IPv4Utils.toRange("10.1.2.0/22");
      Range<BigInteger> range2 = IPv4Utils.toRange("10.1.0.0/22");

      assertThat(range2).isEqualTo(range1);
    }

    @Test
    void singleIp() {
      Range<BigInteger> singleIp = IPv4Utils.toRange("10.1.2.0/32");
      Range<BigInteger> singleIp2 = IPv4Utils.toRange("10.1.2.0");

      assertThat(singleIp2).isEqualTo(singleIp);
    }
  }

  @Nested
  class SortCidrs {
    @Test
    void sortsByNumericAddressAndRangeSize() {
      List<String> cidrs = List.of("10.0.10.0/24", "10.0.2.0/24", "10.0.2.0/16", "10.0.1.0/24");

      List<String> result = IPv4Utils.sortedCidrs(cidrs);

      assertThat(result)
          .containsExactly("10.0.2.0/16", "10.0.1.0/24", "10.0.2.0/24", "10.0.10.0/24");
    }

    @Test
    void preservesInvalidValueOrder() {
      List<String> cidrs = List.of("10.0.1.0/24", "10.0.0.0/33", "invalid");

      List<String> result = IPv4Utils.sortedCidrs(cidrs);

      assertThat(result).containsExactly("10.0.0.0/33", "invalid", "10.0.1.0/24");
    }
  }

  @Nested
  class CreateCidrsFromRange {
    @Test
    void test() {
      BigInteger startIP = new BigInteger("3232235776"); // 192.168.1.0
      BigInteger endIP = new BigInteger("3232236031"); // 192.168.1.255
      Range<BigInteger> ipRange = Range.closed(startIP, endIP);

      List<String> actualCIDR = IPv4Utils.toStrings(ipRange);

      assertThat(actualCIDR).first().isEqualTo("192.168.1.0/24");
    }

    @Test
    void test2() {
      BigInteger startIP = new BigInteger("167903232"); // 10.2.0.0
      BigInteger endIP = new BigInteger("167903360"); // 10.2.0.128
      Range<BigInteger> fullRange = Range.closed(startIP, endIP);

      List<String> actualCIDR = IPv4Utils.toStrings(fullRange);

      assertThat(actualCIDR).first().isEqualTo("10.2.0.0/25");
    }

    @Test
    void test3() {
      BigInteger startIP = new BigInteger("167903359"); // 10.2.0.127
      BigInteger endIP = new BigInteger("167903487"); // 10.2.0.255
      Range<BigInteger> fullRange = Range.openClosed(startIP, endIP);

      List<String> actualCIDR = IPv4Utils.toStrings(fullRange);

      assertThat(actualCIDR).first().isEqualTo("10.2.0.128/25");
    }

    @Test
    void singleIP() {
      BigInteger ip = new BigInteger("3232235776"); // 192.168.1.0
      Range<BigInteger> ipRange = Range.closed(ip, ip);

      List<String> actualCIDR = IPv4Utils.toStrings(ipRange);

      assertThat(actualCIDR).first().isEqualTo("192.168.1.0/32");
    }

    @Test
    void invalidRange() {
      BigInteger startIP = new BigInteger("3232236032"); // 192.168.2.0
      BigInteger endIP = new BigInteger("3232235776"); // 192.168.1.0

      assertThatThrownBy(
              () -> {
                Range<BigInteger> ipRange = Range.closed(startIP, endIP);
                IPv4Utils.toStrings(ipRange);
              })
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fullRange() {
      BigInteger startIP = BigInteger.ZERO;
      BigInteger endIP = BigInteger.valueOf(2).pow(32).subtract(BigInteger.ONE);
      Range<BigInteger> fullRange = Range.closed(startIP, endIP);

      List<String> actualCIDR = IPv4Utils.toStrings(fullRange);

      assertThat(actualCIDR).first().isEqualTo("0.0.0.0/0");
    }

    @Test
    void noRange() {
      Range<BigInteger> ipRange =
          // 10.1.4.0, empty range so the value itself is immaterial
          Range.closedOpen(BigInteger.valueOf(167838720L), BigInteger.valueOf(167838720L));

      List<String> actualCIDR = IPv4Utils.toStrings(ipRange);

      assertThat(actualCIDR).isEmpty();
    }

    @Test
    void splitCidr() {
      BigInteger startIP = new BigInteger("167903232"); // 10.2.0.0
      BigInteger endIP = new BigInteger("167903424"); // 10.2.0.192

      assertThat(IPv4Utils.toStrings(Range.closedOpen(startIP, endIP)))
          .containsExactly("10.2.0.0/25", "10.2.0.128/26");
    }
  }

  @Test
  void conversionConsistency() throws Exception {
    // 10.1.2.0 is a valid /23 network address, so this must round-trip unchanged.
    String cidr = "10.1.2.0/23";

    Range<BigInteger> range1 = IPv4Utils.toRange(cidr);
    List<String> cidrFromRange = IPv4Utils.toStrings(range1);

    assertThat(cidrFromRange).first().isEqualTo(cidr);
  }

  @Nested
  class isValidIpAddress {
    @ParameterizedTest
    @ValueSource(strings = {"10.0.0.240", "192.168.1.0"})
    void valid(String ipAddress) {

      assertThat(IPv4Utils.isValidIpAddress(ipAddress)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.0.0.256", "192.168.1.0/0", "amadeus.com"})
    void invalid(String ipAddress) {

      assertThat(IPv4Utils.isValidIpAddress(ipAddress)).isFalse();
    }
  }

  @Nested
  class ParseCidr {
    @Test
    void validCidrWithSlash24() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("10.0.0.0/24");

      assertThat(result).isPresent();
      assertThat(result.get().getNetworkAddress()).isEqualTo("10.0.0.0");
      assertThat(result.get().getBroadcastAddress()).isEqualTo("10.0.0.255");
    }

    @Test
    void validCidrWithSlash16() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("192.168.0.0/16");

      assertThat(result).isPresent();
      assertThat(result.get().getNetworkAddress()).isEqualTo("192.168.0.0");
      assertThat(result.get().getBroadcastAddress()).isEqualTo("192.168.255.255");
    }

    @Test
    void validCidrWithSlash32() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("10.0.0.1/32");

      assertThat(result).isPresent();
      assertThat(result.get().getNetworkAddress()).isEqualTo("10.0.0.1");
      assertThat(result.get().getBroadcastAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    void validCidrWithSlash25() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("10.0.0.0/25");

      assertThat(result).isPresent();
      assertThat(result.get().getNetworkAddress()).isEqualTo("10.0.0.0");
      assertThat(result.get().getBroadcastAddress()).isEqualTo("10.0.0.127");
    }

    @Test
    void invalidCidrFormat() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("invalid-cidr");

      assertThat(result).isEmpty();
    }

    @Test
    void invalidCidrWithInvalidOctet() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("256.256.256.256/24");

      assertThat(result).isEmpty();
    }

    @Test
    void invalidCidrWithInvalidPrefix() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("10.0.0.0/33");

      assertThat(result).isEmpty();
    }

    @Test
    void invalidCidrWithNegativePrefix() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("10.0.0.0/-1");

      assertThat(result).isEmpty();
    }

    @Test
    void nullCidr() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr(null);

      assertThat(result).isEmpty();
    }

    @Test
    void emptyCidr() {
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("");

      assertThat(result).isEmpty();
    }

    @Test
    void cidrWithHostBits() {
      // CIDR with host bits set - IP doesn't match network address
      Optional<SubnetUtils.SubnetInfo> result = IPv4Utils.parseCidr("10.0.1.5/24");

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class CidrsOverlap {
    @Test
    void nonOverlappingCidrs() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.0/24").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("192.168.0.0/24").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isFalse();
    }

    @Test
    void identicalCidrs() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.0/24").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.0.0.0/24").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isTrue();
    }

    @Test
    void oneCidrContainsAnother() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.0/16").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.0.1.0/24").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isTrue();
    }

    @Test
    void oneCidrContainsAnotherReversed() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.1.0/24").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.0.0.0/16").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isTrue();
    }

    @Test
    void adjacentNonOverlappingCidrs() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.0/25").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.0.0.128/25").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isFalse();
    }

    @Test
    void overlappingCidrsPartialOverlap() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.0/24").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.0.0.128/25").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isTrue();
    }

    @Test
    void overlappingCidrsAtBoundary() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.3.0.0/25").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.3.0.0/22").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isTrue();
    }

    @Test
    void nonOverlappingDifferentNetworks() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("172.16.0.0/16").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("192.168.0.0/16").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isFalse();
    }

    @Test
    void singleIpOverlap() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.1/32").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.0.0.1/32").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isTrue();
    }

    @Test
    void singleIpNoOverlap() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.1/32").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.0.0.2/32").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isFalse();
    }

    @Test
    void largeNetworkContainsSmallerNetwork() {
      SubnetUtils.SubnetInfo cidr1 = IPv4Utils.parseCidr("10.0.0.0/8").get();
      SubnetUtils.SubnetInfo cidr2 = IPv4Utils.parseCidr("10.3.0.0/25").get();

      assertThat(IPv4Utils.cidrsOverlap(cidr1, cidr2)).isTrue();
    }
  }

  @Test
  void merge() {
    Range<BigInteger> firstRange = IPv4Utils.toRange("10.2.0.0/25");
    Range<BigInteger> secondRange = IPv4Utils.toRange("10.2.0.128/26");

    RangeSet<BigInteger> mergedRange = TreeRangeSet.create();
    mergedRange.add(firstRange);
    mergedRange.add(secondRange);

    Range<BigInteger> actualRange = mergedRange.asRanges().iterator().next();
    List<String> actualRangeString = IPv4Utils.toStrings(actualRange);

    assertThat(actualRangeString).containsExactly("10.2.0.0/25", "10.2.0.128/26");
  }
}

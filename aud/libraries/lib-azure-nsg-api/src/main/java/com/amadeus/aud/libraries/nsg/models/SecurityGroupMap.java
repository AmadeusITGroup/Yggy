package com.amadeus.aud.libraries.nsg.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@JsonPropertyOrder({
  SecurityGroupMap.JSON_PROPERTY_INBOUND,
  SecurityGroupMap.JSON_PROPERTY_OUTBOUND
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityGroupMap {

  public static final String JSON_PROPERTY_INBOUND = "inbound";
  public static final String JSON_PROPERTY_OUTBOUND = "outbound";

  @JsonProperty(JSON_PROPERTY_INBOUND)
  private Set<NetworkRule> inbound;

  @JsonProperty(JSON_PROPERTY_OUTBOUND)
  private List<ServiceTagOutboundRule> outbound = new ArrayList<>();

  @Override
  public boolean equals(Object o) {
    return this == o
        || ((o instanceof SecurityGroupMap that)
            && Objects.equals(inbound, that.inbound)
            && Objects.equals(outbound, that.outbound));
  }

  @Override
  public int hashCode() {
    return Objects.hash(inbound, outbound);
  }

  @Override
  public String toString() {
    return "SecurityGroupMap{" + "inbound=" + inbound + ", outbound=" + outbound + '}';
  }

  public Set<NetworkRule> getInbound() {
    return inbound;
  }

  public void setInbound(Set<NetworkRule> inbound) {
    this.inbound = inbound;
  }

  public void addInbound(NetworkRule inboundItem) {
    if (this.inbound == null) {
      this.inbound = new HashSet<>();
    }
    this.inbound.add(inboundItem);
  }

  public void addInbounds(Set<NetworkRule> inboundItems) {
    if (this.inbound == null) {
      this.inbound = new HashSet<>();
    }
    this.inbound.addAll(inboundItems);
  }

  @SuppressWarnings("java:S3776")
  @Deprecated
  private boolean searchAndMerge(NetworkRule networkRule, Set<NetworkRule> networkRules) {
    Protocol protocol = networkRule.getProtocol();
    RangeSet<Integer> portsRange = networkRule.getPortsRangeSet();
    RangeSet<BigInteger> cidrRange = networkRule.getCidrRangeSet();
    RangeSet<BigInteger> dstCidrRange = networkRule.getDstCidrRangeSet();

    for (NetworkRule sp : networkRules) {
      RangeSet<BigInteger> spDstCidrRange = sp.getDstCidrRangeSet();
      RangeSet<BigInteger> spCidrRange = sp.getCidrRangeSet();
      RangeSet<Integer> spPortsRange = sp.getPortsRangeSet();

      boolean equalsDstCidr = Objects.equals(spDstCidrRange, dstCidrRange);
      boolean equalsCidr = Objects.equals(spCidrRange, cidrRange);

      if (Objects.equals(protocol, sp.getProtocol())) {
        if (spPortsRange.equals(portsRange)) {
          if (spCidrRange != null && equalsDstCidr && cidrRange != null) {
            // 1. Case in which the protocol, the ports and the DstCidrRange are equals
            //    -> merge the cidrRange
            // We need to be careful to not merge the rules that have cidr = null; those are
            // ServiceTags
            spCidrRange.addAll(cidrRange);
            return true;
          }
          if (equalsCidr) {
            // 2. Case in which the protocol, the ports and the cidrRange are equals
            //    -> merge the DstCidrRange
            spDstCidrRange.addAll(dstCidrRange);
            return true;
          }
        }

        if (equalsCidr && equalsDstCidr) {
          // 3. Case in which the protocol, the cidrRange and the DstCidrRange are equals
          //    -> merge ports
          spPortsRange.addAll(portsRange);
          return true;
        }
      }

      // 4. Case in which the cidrRange, the ports and the DstCidrRange are equals
      //    -> merge the protocol
    }
    return false;
  }

  @Deprecated
  private boolean searchAndMergeInboundServiceTag(
      NetworkRule networkRule, Set<NetworkRule> networkRules) {
    RangeSet<Integer> portsRange = networkRule.getPortsRangeSet();
    RangeSet<BigInteger> dstCidrRange = networkRule.getDstCidrRangeSet();
    String serviceTag = networkRule.getServiceTag();

    for (NetworkRule sp : networkRules) {
      String spServiceTag = sp.getServiceTag();
      RangeSet<Integer> spPortsRange = sp.getPortsRangeSet();
      RangeSet<BigInteger> spDstCidrRange = sp.getDstCidrRangeSet();

      if (Objects.equals(spServiceTag, serviceTag)) {
        spDstCidrRange.addAll(dstCidrRange);
        spPortsRange.addAll(portsRange);
        return true;
      }
    }
    return false;
  }

  public List<ServiceTagOutboundRule> getOutbound() {
    return outbound;
  }

  public void setOutbound(List<ServiceTagOutboundRule> outbound) {
    this.outbound = outbound;
  }

  public void addOutboundItem(ServiceTagOutboundRule outboundItem) {
    if (this.outbound == null) {
      this.outbound = new ArrayList<>();
    }
    this.outbound.add(outboundItem);
  }

  public void optimizeInboundNetworkRules() {
    Map<ProtocolServiceTagKey, List<NetworkRule>> groupedRules =
        groupByProtocolAndServiceTag(this.inbound);

    Map<ProtocolServiceTagKey, List<NetworkRule>> optimizedGroupedRules = new HashMap<>();

    for (Map.Entry<ProtocolServiceTagKey, List<NetworkRule>> entry : groupedRules.entrySet()) {
      ProtocolServiceTagKey key = entry.getKey();
      List<NetworkRule> rulesInGroup = entry.getValue();

      // Perform merging by fixing different dimensions
      List<NetworkRule> mergedRules = mergeByDimension(rulesInGroup, Dimension.PORT_DSTCIDR);
      mergedRules = mergeByDimension(mergedRules, Dimension.PORT_CIDR);
      mergedRules = mergeByDimension(mergedRules, Dimension.CIDR_DSTCIDR);

      optimizedGroupedRules.put(key, mergedRules);
    }
    Set<NetworkRule> optimizedNetworkRules = collectOptimizedRules(optimizedGroupedRules);

    // Replace the original set with the optimized rules
    if (this.inbound != null) {
      this.inbound.clear();
    }
    addInbounds(optimizedNetworkRules);
  }

  private Map<ProtocolServiceTagKey, List<NetworkRule>> groupByProtocolAndServiceTag(
      Set<NetworkRule> rules) {
    Map<ProtocolServiceTagKey, List<NetworkRule>> groupedRules = new HashMap<>();
    if (rules == null) {
      return groupedRules;
    }

    for (NetworkRule rule : rules) {
      Protocol protocol = rule.getProtocol();
      String serviceTag = rule.getServiceTag();

      ProtocolServiceTagKey key = new ProtocolServiceTagKey(protocol, serviceTag);

      groupedRules.computeIfAbsent(key, k -> new ArrayList<>()).add(rule);
    }

    return groupedRules;
  }

  private List<NetworkRule> mergeByDimension(List<NetworkRule> rules, Dimension dimension) {
    // Map to group rules by fixed dimensions
    Map<DimensionsKey, List<NetworkRule>> groupMap = new HashMap<>();

    for (NetworkRule rule : rules) {
      DimensionsKey key =
          switch (dimension) {
            case PORT_DSTCIDR ->
                // Fix portsRange and dstCidrRange
                new DimensionsKey(rule.getPortsRangeSet(), rule.getDstCidrRangeSet(), null);
            case PORT_CIDR ->
                // Fix portsRange and cidrRange
                new DimensionsKey(rule.getPortsRangeSet(), null, rule.getCidrRangeSet());
            case CIDR_DSTCIDR ->
                // Fix cidrRange and dstCidrRange
                new DimensionsKey(null, rule.getDstCidrRangeSet(), rule.getCidrRangeSet());
          };

      groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(rule);
    }

    // Merge rules within each group
    List<NetworkRule> mergedRules = new ArrayList<>();

    for (List<NetworkRule> group : groupMap.values()) {
      NetworkRule mergedRule = mergeRulesInGroup(group, dimension);
      mergedRules.add(mergedRule);
    }

    return mergedRules;
  }

  private NetworkRule mergeRulesInGroup(List<NetworkRule> group, Dimension dimension) {
    if (group.isEmpty()) {
      return null;
    }

    NetworkRule firstRule = group.getFirst();
    if (group.size() == 1) {
      return firstRule;
    }

    // Initialize merged ranges
    RangeSet<Integer> mergedPortsRange = TreeRangeSet.create();
    RangeSet<BigInteger> mergedDstCidrRange = TreeRangeSet.create();
    RangeSet<BigInteger> mergedCidrRange = TreeRangeSet.create();

    // Apply dimensions from the first rule
    initializeMerge(firstRule, mergedPortsRange, mergedDstCidrRange, mergedCidrRange, dimension);

    // Merge non-fixed dimensions from all rules
    merge(group, mergedPortsRange, mergedDstCidrRange, mergedCidrRange, dimension);

    // Create a new merged NetworkRule
    return new NetworkRule(
        firstRule.getName(),
        mergedCidrRange,
        mergedDstCidrRange,
        mergedPortsRange,
        firstRule.getProtocol(),
        firstRule.getServiceTag());
  }

  private void initializeMerge(
      NetworkRule rule,
      RangeSet<Integer> mergedPortsRange,
      RangeSet<BigInteger> mergedDstCidrRange,
      RangeSet<BigInteger> mergedCidrRange,
      Dimension dimension) {

    if (dimension == Dimension.PORT_DSTCIDR) {
      copyIfNotNull(rule.getPortsRangeSet(), mergedPortsRange);
      copyIfNotNull(rule.getDstCidrRangeSet(), mergedDstCidrRange);
    } else if (dimension == Dimension.PORT_CIDR) {
      copyIfNotNull(rule.getPortsRangeSet(), mergedPortsRange);
      copyIfNotNull(rule.getCidrRangeSet(), mergedCidrRange);
    } else if (dimension == Dimension.CIDR_DSTCIDR) {
      copyIfNotNull(rule.getCidrRangeSet(), mergedCidrRange);
      copyIfNotNull(rule.getDstCidrRangeSet(), mergedDstCidrRange);
    } else {
      throw new IllegalArgumentException("Unknown Dimension: " + dimension);
    }
  }

  private <T extends Comparable<T>> void copyIfNotNull(RangeSet<T> source, RangeSet<T> target) {
    if (source != null) {
      target.addAll(source);
    }
  }

  private void merge(
      List<NetworkRule> group,
      RangeSet<Integer> mergedPortsRange,
      RangeSet<BigInteger> mergedDstCidrRange,
      RangeSet<BigInteger> mergedCidrRange,
      Dimension dimension) {

    for (NetworkRule rule : group) {
      if (dimension == Dimension.CIDR_DSTCIDR) {
        copyIfNotNull(rule.getPortsRangeSet(), mergedPortsRange);
      } else if (dimension == Dimension.PORT_CIDR) {
        copyIfNotNull(rule.getDstCidrRangeSet(), mergedDstCidrRange);
      } else if (dimension == Dimension.PORT_DSTCIDR) {
        copyIfNotNull(rule.getCidrRangeSet(), mergedCidrRange);
      }
    }
  }

  private Set<NetworkRule> collectOptimizedRules(
      Map<ProtocolServiceTagKey, List<NetworkRule>> groupedRules) {
    Set<NetworkRule> optimizedRules = new HashSet<>();

    for (List<NetworkRule> rules : groupedRules.values()) {
      optimizedRules.addAll(rules);
    }

    return optimizedRules;
  }

  public void normalizeNetworkRuleNames() {
    for (NetworkRule rule : inbound) {
      rule.normalizeName();
    }
  }

  private enum Dimension {
    PORT_DSTCIDR,
    PORT_CIDR,
    CIDR_DSTCIDR
  }

  private record ProtocolServiceTagKey(Protocol protocol, String serviceTag) {}

  private record DimensionsKey(
      RangeSet<Integer> portsRange,
      RangeSet<BigInteger> dstCidrRange,
      RangeSet<BigInteger> cidrRange) {

    private DimensionsKey(
        RangeSet<Integer> portsRange,
        RangeSet<BigInteger> dstCidrRange,
        RangeSet<BigInteger> cidrRange) {
      this.portsRange = portsRange == null ? null : ImmutableRangeSet.copyOf(portsRange);
      this.dstCidrRange = dstCidrRange == null ? null : ImmutableRangeSet.copyOf(dstCidrRange);
      this.cidrRange = cidrRange == null ? null : ImmutableRangeSet.copyOf(cidrRange);
    }
  }
}

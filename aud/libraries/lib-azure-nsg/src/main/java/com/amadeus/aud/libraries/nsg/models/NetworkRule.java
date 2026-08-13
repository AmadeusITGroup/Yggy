package com.amadeus.aud.libraries.nsg.models;

import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.amadeus.aud.libraries.models.core.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import jakarta.annotation.Nullable;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({
  NetworkRule.JSON_PROPERTY_NAME,
  NetworkRule.JSON_PROPERTY_CIDRS,
  NetworkRule.JSON_PROPERTY_DST_CIDRS,
  NetworkRule.JSON_PROPERTY_PORTS,
  NetworkRule.JSON_PROPERTY_PROTOCOL,
  NetworkRule.JSON_PROPERTY_SERVICE_TAG
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NetworkRule {

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  public static final String JSON_PROPERTY_SERVICE_TAG = "service_tag";
  public static final String JSON_PROPERTY_NAME = "name";
  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  public static final String JSON_PROPERTY_DST_CIDRS = "dst_cidrs";
  public static final String JSON_PROPERTY_PORTS = "ports";

  @JsonProperty(JSON_PROPERTY_NAME)
  private String name;

  @JsonIgnore private RangeSet<BigInteger> cidrRangeSet;
  @JsonIgnore private RangeSet<BigInteger> dstCidrRangeSet;
  @JsonIgnore private RangeSet<Integer> portsRangeSet;

  @JsonProperty(JSON_PROPERTY_PROTOCOL)
  private Protocol protocol;

  @JsonProperty(JSON_PROPERTY_SERVICE_TAG)
  private String serviceTag;

  private int hashcode;

  public NetworkRule(
      String name,
      RangeSet<BigInteger> cidrRangeSet,
      RangeSet<BigInteger> dstCidrRangeSet,
      RangeSet<Integer> portRangeSet,
      Protocol protocol,
      String serviceTag) {
    this.name = name;
    this.cidrRangeSet = cidrRangeSet;
    this.dstCidrRangeSet = dstCidrRangeSet;
    this.portsRangeSet = portRangeSet;
    this.protocol = protocol;
    this.serviceTag = serviceTag;
  }

  public NetworkRule(
      String name,
      RangeSet<BigInteger> dstCidrsRangeSet,
      RangeSet<Integer> portRangeSet,
      String protocol) {
    this.name = name;
    this.dstCidrRangeSet = dstCidrsRangeSet;
    this.portsRangeSet = portRangeSet;
    this.protocol = Protocol.fromValue(protocol);
  }

  public NetworkRule(
      String name,
      RangeSet<BigInteger> cidrsRangeSet,
      RangeSet<BigInteger> dstCidrsRangeSet,
      RangeSet<Integer> portRangeSet,
      String protocol) {
    this(name, dstCidrsRangeSet, portRangeSet, protocol);
    this.cidrRangeSet = cidrsRangeSet;
  }

  public NetworkRule(
      String name,
      String serviceTagName,
      RangeSet<BigInteger> dstCidrsRangeSet,
      RangeSet<Integer> portRangeSet,
      String protocol) {
    this(name, dstCidrsRangeSet, portRangeSet, protocol);
    this.serviceTag = serviceTagName;
  }

  @JsonCreator
  public NetworkRule() {}

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkRule subnetPort = (NetworkRule) o;
    return Objects.equals(this.name, subnetPort.name)
        && Objects.equals(this.cidrRangeSet, subnetPort.cidrRangeSet)
        && Objects.equals(this.dstCidrRangeSet, subnetPort.dstCidrRangeSet)
        && Objects.equals(this.portsRangeSet, subnetPort.portsRangeSet)
        && Objects.equals(this.protocol, subnetPort.protocol)
        && Objects.equals(this.serviceTag, subnetPort.serviceTag);
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode =
          Objects.hash(name, cidrRangeSet, dstCidrRangeSet, portsRangeSet, protocol, serviceTag);
    }
    return hashcode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RangeSet<BigInteger> getCidrRangeSet() {
    return cidrRangeSet;
  }

  public RangeSet<BigInteger> getDstCidrRangeSet() {
    return dstCidrRangeSet;
  }

  public void setDstCidrRangeSet(RangeSet<BigInteger> newDestination) {
    dstCidrRangeSet = newDestination;
  }

  public RangeSet<Integer> getPortsRangeSet() {
    return portsRangeSet;
  }

  @JsonProperty(JSON_PROPERTY_CIDRS)
  public @Nullable List<String> getCidrs() {
    if (cidrRangeSet == null) {
      return null;
    }
    List<String> result = new ArrayList<>();
    for (Range<BigInteger> range : cidrRangeSet.asRanges()) {
      result.addAll(IPv4Utils.toStrings(range));
    }
    return result;
  }

  @JsonProperty(JSON_PROPERTY_CIDRS)
  public void setCidrs(@Nullable List<String> cidrs) throws UnknownHostException {
    if (cidrs == null) {
      this.cidrRangeSet = null;
      return;
    }

    this.cidrRangeSet = TreeRangeSet.create();
    for (String cidr : cidrs) {
      this.cidrRangeSet.add(IPv4Utils.toRange(cidr));
    }
  }

  @JsonProperty(JSON_PROPERTY_DST_CIDRS)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public @Nullable List<String> getDstCidrs() {
    if (dstCidrRangeSet == null) {
      return List.of();
    }

    List<String> result = new ArrayList<>();
    for (Range<BigInteger> range : dstCidrRangeSet.asRanges()) {
      result.addAll(IPv4Utils.toStrings(range));
    }

    // /!\ WARNING /!\
    // Ugly hack to deal with the Azure Service Tags and the fact that the automation
    // interprets * -> ANY.
    // On Azure saying 0.0.0.0/0 and ANY is two different things, at least when a Service Tag
    // is used. This is because Azure also deal with IPv6 and not only IPv4.
    // We have to convert the Destination CIDR, when is 0.0.0.0/0, to * when the Source is a
    // Service Tag.
    // TODO: Investigate how to remove this hack. Perhaps move this complexity to the automation?
    if (this.cidrRangeSet == null
        && this.serviceTag != null
        && !this.serviceTag.isEmpty()
        && result.size() == 1
        && IPv4Utils.ALL.equals(result.getFirst())) {
      result.set(0, "*");
    }

    return result;
  }

  @JsonProperty(JSON_PROPERTY_DST_CIDRS)
  public void setDstCidrs(@Nullable List<String> dstCidrs) throws UnknownHostException {
    if (dstCidrs == null) {
      this.dstCidrRangeSet = null;
      return;
    }

    this.dstCidrRangeSet = TreeRangeSet.create();
    for (String dstCidr : dstCidrs) {
      this.dstCidrRangeSet.add(IPv4Utils.toRange(dstCidr));
    }
  }

  @JsonProperty(JSON_PROPERTY_PORTS)
  public List<String> getPorts() {
    return PortUtils.toStrings(portsRangeSet);
  }

  @JsonProperty(JSON_PROPERTY_PORTS)
  public void setPorts(@Nullable List<String> ports) {
    this.portsRangeSet = PortUtils.toRangeSet(ports);
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = Protocol.fromValue(protocol);
  }

  public String getServiceTag() {
    return serviceTag;
  }

  public void setServiceTag(String serviceTag) {
    this.serviceTag = serviceTag;
  }

  @JsonIgnore
  public boolean isServiceTag() {
    return serviceTag != null && cidrRangeSet == null;
  }

  public void normalizeName() {
    if (name != null) {
      name = StringUtils.normalize(name);
    }
  }
}

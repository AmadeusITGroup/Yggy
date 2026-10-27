package com.amadeus.aud.libraries.ipreport.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** IpReportSubnet specification */
@JsonPropertyOrder({
  IpReportSubnet.JSON_PROPERTY_CIDR,
  IpReportSubnet.JSON_PROPERTY_NAME,
  IpReportSubnet.JSON_PROPERTY_SCOPE,
  IpReportSubnet.JSON_PROPERTY_WINAPPROACH_ASSIGNEE,
  IpReportSubnet.JSON_PROPERTY_PROVIDER,
  IpReportSubnet.JSON_PROPERTY_CONTINENT,
  IpReportSubnet.JSON_PROPERTY_REGION,
  IpReportSubnet.JSON_PROPERTY_TARGET,
  IpReportSubnet.JSON_PROPERTY_FIREWALLGROUP,
  IpReportSubnet.JSON_PROPERTY_LANDINGZONE_TYPE,
  IpReportSubnet.JSON_PROPERTY_LANDINGZONE,
  IpReportSubnet.JSON_PROPERTY_MACROPHASE,
  IpReportSubnet.JSON_PROPERTY_SUBNET_TYPE,
  IpReportSubnet.JSON_PROPERTY_SUBNET_SHARED,
  IpReportSubnet.JSON_PROPERTY_IMPLEMENTATION_ZONE,
  IpReportSubnet.JSON_PROPERTY_SECURITY_ZONE,
  IpReportSubnet.JSON_PROPERTY_SN_URL_HASH,
  IpReportSubnet.JSON_PROPERTY_RE_URL_HASH,
  IpReportSubnet.JSON_PROPERTY_IZ_URL_HASH,
  IpReportSubnet.JSON_PROPERTY_AS_INCOMING,
  IpReportSubnet.JSON_PROPERTY_AS_OUTGOING
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpReportSubnet {
  public static final String JSON_PROPERTY_CIDR = "cidr";
  private String cidr;

  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_SCOPE = "scope";
  private String scope;

  public static final String JSON_PROPERTY_WINAPPROACH_ASSIGNEE = "winapproach_assignee";
  private String winapproachAssignee;

  public static final String JSON_PROPERTY_PROVIDER = "provider";
  private String provider;

  public static final String JSON_PROPERTY_CONTINENT = "continent";
  private String continent;

  public static final String JSON_PROPERTY_REGION = "region";
  private String region;

  public static final String JSON_PROPERTY_TARGET = "target";
  private String target;

  public static final String JSON_PROPERTY_FIREWALLGROUP = "firewallgroup";
  private String firewallgroup;

  public static final String JSON_PROPERTY_LANDINGZONE_TYPE = "landingzone_type";
  private String landingzoneType;

  public static final String JSON_PROPERTY_LANDINGZONE = "landingzone";
  private String landingzone;

  public static final String JSON_PROPERTY_MACROPHASE = "macrophase";
  private String macrophase;

  public static final String JSON_PROPERTY_SUBNET_TYPE = "subnet_type";
  private String subnetType;

  public static final String JSON_PROPERTY_SUBNET_SHARED = "subnet_shared";
  private Boolean subnetShared;

  public static final String JSON_PROPERTY_IMPLEMENTATION_ZONE = "implementation_zone";
  private String implementationZone;

  public static final String JSON_PROPERTY_SECURITY_ZONE = "security_zone";
  private String securityZone;

  public static final String JSON_PROPERTY_SN_URL_HASH = "sn_url_hash";
  private String snUrlHash;

  public static final String JSON_PROPERTY_RE_URL_HASH = "re_url_hash";
  private String reUrlHash;

  public static final String JSON_PROPERTY_IZ_URL_HASH = "iz_url_hash";
  private String izUrlHash;

  public static final String JSON_PROPERTY_AS_INCOMING = "as_incoming";
  private List<@Valid IpReportAs> asIncoming;

  public static final String JSON_PROPERTY_AS_OUTGOING = "as_outgoing";
  private List<@Valid IpReportAs> asOutgoing;

  public IpReportSubnet() {}

  public IpReportSubnet cidr(String cidr) {
    this.cidr = cidr;
    return this;
  }

  /**
   * Get cidr
   *
   * @return cidr
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_CIDR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCidr() {
    return cidr;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDR, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCidr(String cidr) {
    this.cidr = cidr;
  }

  public IpReportSubnet name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getName() {
    return name;
  }

  @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setName(String name) {
    this.name = name;
  }

  public IpReportSubnet scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   *
   * @return scope
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getScope() {
    return scope;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScope(String scope) {
    this.scope = scope;
  }

  public IpReportSubnet winapproachAssignee(String winapproachAssignee) {
    this.winapproachAssignee = winapproachAssignee;
    return this;
  }

  /**
   * Get winapproachAssignee
   *
   * @return winapproachAssignee
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_WINAPPROACH_ASSIGNEE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getWinapproachAssignee() {
    return winapproachAssignee;
  }

  @JsonProperty(value = JSON_PROPERTY_WINAPPROACH_ASSIGNEE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setWinapproachAssignee(String winapproachAssignee) {
    this.winapproachAssignee = winapproachAssignee;
  }

  public IpReportSubnet provider(String provider) {
    this.provider = provider;
    return this;
  }

  /**
   * Get provider
   *
   * @return provider
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_PROVIDER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getProvider() {
    return provider;
  }

  @JsonProperty(value = JSON_PROPERTY_PROVIDER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProvider(String provider) {
    this.provider = provider;
  }

  public IpReportSubnet continent(String continent) {
    this.continent = continent;
    return this;
  }

  /**
   * Get continent
   *
   * @return continent
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_CONTINENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getContinent() {
    return continent;
  }

  @JsonProperty(value = JSON_PROPERTY_CONTINENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setContinent(String continent) {
    this.continent = continent;
  }

  public IpReportSubnet region(String region) {
    this.region = region;
    return this;
  }

  /**
   * Get region
   *
   * @return region
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_REGION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getRegion() {
    return region;
  }

  @JsonProperty(value = JSON_PROPERTY_REGION, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRegion(String region) {
    this.region = region;
  }

  public IpReportSubnet target(String target) {
    this.target = target;
    return this;
  }

  /**
   * Get target
   *
   * @return target
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_TARGET, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTarget() {
    return target;
  }

  @JsonProperty(value = JSON_PROPERTY_TARGET, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTarget(String target) {
    this.target = target;
  }

  public IpReportSubnet firewallgroup(String firewallgroup) {
    this.firewallgroup = firewallgroup;
    return this;
  }

  /**
   * Get firewallgroup
   *
   * @return firewallgroup
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_FIREWALLGROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getFirewallgroup() {
    return firewallgroup;
  }

  @JsonProperty(value = JSON_PROPERTY_FIREWALLGROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFirewallgroup(String firewallgroup) {
    this.firewallgroup = firewallgroup;
  }

  public IpReportSubnet landingzoneType(String landingzoneType) {
    this.landingzoneType = landingzoneType;
    return this;
  }

  /**
   * Get landingzoneType
   *
   * @return landingzoneType
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLandingzoneType() {
    return landingzoneType;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLandingzoneType(String landingzoneType) {
    this.landingzoneType = landingzoneType;
  }

  public IpReportSubnet landingzone(String landingzone) {
    this.landingzone = landingzone;
    return this;
  }

  /**
   * Get landingzone
   *
   * @return landingzone
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLandingzone() {
    return landingzone;
  }

  @JsonProperty(value = JSON_PROPERTY_LANDINGZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLandingzone(String landingzone) {
    this.landingzone = landingzone;
  }

  public IpReportSubnet macrophase(String macrophase) {
    this.macrophase = macrophase;
    return this;
  }

  /**
   * Get macrophase
   *
   * @return macrophase
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_MACROPHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getMacrophase() {
    return macrophase;
  }

  @JsonProperty(value = JSON_PROPERTY_MACROPHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setMacrophase(String macrophase) {
    this.macrophase = macrophase;
  }

  public IpReportSubnet subnetType(String subnetType) {
    this.subnetType = subnetType;
    return this;
  }

  /**
   * Get subnetType
   *
   * @return subnetType
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_SUBNET_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSubnetType() {
    return subnetType;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNET_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnetType(String subnetType) {
    this.subnetType = subnetType;
  }

  public IpReportSubnet subnetShared(Boolean subnetShared) {
    this.subnetShared = subnetShared;
    return this;
  }

  /**
   * Get subnetShared
   *
   * @return subnetShared
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_SUBNET_SHARED, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getSubnetShared() {
    return subnetShared;
  }

  @JsonProperty(value = JSON_PROPERTY_SUBNET_SHARED, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSubnetShared(Boolean subnetShared) {
    this.subnetShared = subnetShared;
  }

  public IpReportSubnet implementationZone(String implementationZone) {
    this.implementationZone = implementationZone;
    return this;
  }

  /**
   * Get implementationZone
   *
   * @return implementationZone
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_IMPLEMENTATION_ZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getImplementationZone() {
    return implementationZone;
  }

  @JsonProperty(value = JSON_PROPERTY_IMPLEMENTATION_ZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setImplementationZone(String implementationZone) {
    this.implementationZone = implementationZone;
  }

  public IpReportSubnet securityZone(String securityZone) {
    this.securityZone = securityZone;
    return this;
  }

  /**
   * Get securityZone
   *
   * @return securityZone
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_SECURITY_ZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSecurityZone() {
    return securityZone;
  }

  @JsonProperty(value = JSON_PROPERTY_SECURITY_ZONE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSecurityZone(String securityZone) {
    this.securityZone = securityZone;
  }

  public IpReportSubnet snUrlHash(String snUrlHash) {
    this.snUrlHash = snUrlHash;
    return this;
  }

  /**
   * Get snUrlHash
   *
   * @return snUrlHash
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_SN_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSnUrlHash() {
    return snUrlHash;
  }

  @JsonProperty(value = JSON_PROPERTY_SN_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSnUrlHash(String snUrlHash) {
    this.snUrlHash = snUrlHash;
  }

  public IpReportSubnet reUrlHash(String reUrlHash) {
    this.reUrlHash = reUrlHash;
    return this;
  }

  /**
   * Get reUrlHash
   *
   * @return reUrlHash
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_RE_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getReUrlHash() {
    return reUrlHash;
  }

  @JsonProperty(value = JSON_PROPERTY_RE_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setReUrlHash(String reUrlHash) {
    this.reUrlHash = reUrlHash;
  }

  public IpReportSubnet izUrlHash(String izUrlHash) {
    this.izUrlHash = izUrlHash;
    return this;
  }

  /**
   * Get izUrlHash
   *
   * @return izUrlHash
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_IZ_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getIzUrlHash() {
    return izUrlHash;
  }

  @JsonProperty(value = JSON_PROPERTY_IZ_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIzUrlHash(String izUrlHash) {
    this.izUrlHash = izUrlHash;
  }

  public IpReportSubnet asIncoming(List<@Valid IpReportAs> asIncoming) {
    this.asIncoming = asIncoming;
    return this;
  }

  public IpReportSubnet addAsIncomingItem(IpReportAs asIncomingItem) {
    if (this.asIncoming == null) {
      this.asIncoming = new ArrayList<>();
    }
    this.asIncoming.add(asIncomingItem);
    return this;
  }

  /**
   * Get asIncoming
   *
   * @return asIncoming
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_AS_INCOMING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportAs> getAsIncoming() {
    return asIncoming;
  }

  @JsonProperty(value = JSON_PROPERTY_AS_INCOMING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAsIncoming(List<@Valid IpReportAs> asIncoming) {
    this.asIncoming = asIncoming;
  }

  public IpReportSubnet asOutgoing(List<@Valid IpReportAs> asOutgoing) {
    this.asOutgoing = asOutgoing;
    return this;
  }

  public IpReportSubnet addAsOutgoingItem(IpReportAs asOutgoingItem) {
    if (this.asOutgoing == null) {
      this.asOutgoing = new ArrayList<>();
    }
    this.asOutgoing.add(asOutgoingItem);
    return this;
  }

  /**
   * Get asOutgoing
   *
   * @return asOutgoing
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_AS_OUTGOING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid IpReportAs> getAsOutgoing() {
    return asOutgoing;
  }

  @JsonProperty(value = JSON_PROPERTY_AS_OUTGOING, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setAsOutgoing(List<@Valid IpReportAs> asOutgoing) {
    this.asOutgoing = asOutgoing;
  }

  /** Return true if this IpReportSubnet object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpReportSubnet ipReportSubnet = (IpReportSubnet) o;
    return Objects.equals(this.cidr, ipReportSubnet.cidr)
        && Objects.equals(this.name, ipReportSubnet.name)
        && Objects.equals(this.scope, ipReportSubnet.scope)
        && Objects.equals(this.winapproachAssignee, ipReportSubnet.winapproachAssignee)
        && Objects.equals(this.provider, ipReportSubnet.provider)
        && Objects.equals(this.continent, ipReportSubnet.continent)
        && Objects.equals(this.region, ipReportSubnet.region)
        && Objects.equals(this.target, ipReportSubnet.target)
        && Objects.equals(this.firewallgroup, ipReportSubnet.firewallgroup)
        && Objects.equals(this.landingzoneType, ipReportSubnet.landingzoneType)
        && Objects.equals(this.landingzone, ipReportSubnet.landingzone)
        && Objects.equals(this.macrophase, ipReportSubnet.macrophase)
        && Objects.equals(this.subnetType, ipReportSubnet.subnetType)
        && Objects.equals(this.subnetShared, ipReportSubnet.subnetShared)
        && Objects.equals(this.implementationZone, ipReportSubnet.implementationZone)
        && Objects.equals(this.securityZone, ipReportSubnet.securityZone)
        && Objects.equals(this.snUrlHash, ipReportSubnet.snUrlHash)
        && Objects.equals(this.reUrlHash, ipReportSubnet.reUrlHash)
        && Objects.equals(this.izUrlHash, ipReportSubnet.izUrlHash)
        && Objects.equals(this.asIncoming, ipReportSubnet.asIncoming)
        && Objects.equals(this.asOutgoing, ipReportSubnet.asOutgoing);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        cidr,
        name,
        scope,
        winapproachAssignee,
        provider,
        continent,
        region,
        target,
        firewallgroup,
        landingzoneType,
        landingzone,
        macrophase,
        subnetType,
        subnetShared,
        implementationZone,
        securityZone,
        snUrlHash,
        reUrlHash,
        izUrlHash,
        asIncoming,
        asOutgoing);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IpReportSubnet {\n");
    sb.append("    cidr: ").append(toIndentedString(cidr)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    winapproachAssignee: ")
        .append(toIndentedString(winapproachAssignee))
        .append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    continent: ").append(toIndentedString(continent)).append("\n");
    sb.append("    region: ").append(toIndentedString(region)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    firewallgroup: ").append(toIndentedString(firewallgroup)).append("\n");
    sb.append("    landingzoneType: ").append(toIndentedString(landingzoneType)).append("\n");
    sb.append("    landingzone: ").append(toIndentedString(landingzone)).append("\n");
    sb.append("    macrophase: ").append(toIndentedString(macrophase)).append("\n");
    sb.append("    subnetType: ").append(toIndentedString(subnetType)).append("\n");
    sb.append("    subnetShared: ").append(toIndentedString(subnetShared)).append("\n");
    sb.append("    implementationZone: ").append(toIndentedString(implementationZone)).append("\n");
    sb.append("    securityZone: ").append(toIndentedString(securityZone)).append("\n");
    sb.append("    snUrlHash: ").append(toIndentedString(snUrlHash)).append("\n");
    sb.append("    reUrlHash: ").append(toIndentedString(reUrlHash)).append("\n");
    sb.append("    izUrlHash: ").append(toIndentedString(izUrlHash)).append("\n");
    sb.append("    asIncoming: ").append(toIndentedString(asIncoming)).append("\n");
    sb.append("    asOutgoing: ").append(toIndentedString(asOutgoing)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}

package com.amadeus.aud.libraries.ipreport.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** IpReportIp specification */
@JsonPropertyOrder({
  IpReportIp.JSON_PROPERTY_NAME,
  IpReportIp.JSON_PROPERTY_HOST,
  IpReportIp.JSON_PROPERTY_SCOPE,
  IpReportIp.JSON_PROPERTY_WINAPPROACH_ASSIGNEE,
  IpReportIp.JSON_PROPERTY_PROVIDER,
  IpReportIp.JSON_PROPERTY_CONTINENT,
  IpReportIp.JSON_PROPERTY_REGION,
  IpReportIp.JSON_PROPERTY_TARGET,
  IpReportIp.JSON_PROPERTY_FIREWALLGROUP,
  IpReportIp.JSON_PROPERTY_LANDINGZONE,
  IpReportIp.JSON_PROPERTY_MACROPHASE,
  IpReportIp.JSON_PROPERTY_COMPONENT,
  IpReportIp.JSON_PROPERTY_COMPONENT_INSTANCE,
  IpReportIp.JSON_PROPERTY_APPLICATIONPHASE,
  IpReportIp.JSON_PROPERTY_IMPLEMENTATION_ZONE,
  IpReportIp.JSON_PROPERTY_IPA_URL_HASH,
  IpReportIp.JSON_PROPERTY_RE_URL_HASH,
  IpReportIp.JSON_PROPERTY_CI_URL_HASH,
  IpReportIp.JSON_PROPERTY_IZ_URL_HASH
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpReportIp {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_HOST = "host";
  private String host;

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

  public static final String JSON_PROPERTY_LANDINGZONE = "landingzone";
  private String landingzone;

  public static final String JSON_PROPERTY_MACROPHASE = "macrophase";
  private String macrophase;

  public static final String JSON_PROPERTY_COMPONENT = "component";
  private String component;

  public static final String JSON_PROPERTY_COMPONENT_INSTANCE = "component_instance";
  private String componentInstance;

  public static final String JSON_PROPERTY_APPLICATIONPHASE = "applicationphase";
  private String applicationphase;

  public static final String JSON_PROPERTY_IMPLEMENTATION_ZONE = "implementation_zone";
  private String implementationZone;

  public static final String JSON_PROPERTY_IPA_URL_HASH = "ipa_url_hash";
  private String ipaUrlHash;

  public static final String JSON_PROPERTY_RE_URL_HASH = "re_url_hash";
  private String reUrlHash;

  public static final String JSON_PROPERTY_CI_URL_HASH = "ci_url_hash";
  private String ciUrlHash;

  public static final String JSON_PROPERTY_IZ_URL_HASH = "iz_url_hash";
  private String izUrlHash;

  public IpReportIp() {}

  public IpReportIp name(String name) {
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

  public IpReportIp host(String host) {
    this.host = host;
    return this;
  }

  /**
   * Get host
   *
   * @return host
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_HOST, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getHost() {
    return host;
  }

  @JsonProperty(value = JSON_PROPERTY_HOST, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setHost(String host) {
    this.host = host;
  }

  public IpReportIp scope(String scope) {
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

  public IpReportIp winapproachAssignee(String winapproachAssignee) {
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

  public IpReportIp provider(String provider) {
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

  public IpReportIp continent(String continent) {
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

  public IpReportIp region(String region) {
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

  public IpReportIp target(String target) {
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

  public IpReportIp firewallgroup(String firewallgroup) {
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

  public IpReportIp landingzone(String landingzone) {
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

  public IpReportIp macrophase(String macrophase) {
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

  public IpReportIp component(String component) {
    this.component = component;
    return this;
  }

  /**
   * Get component
   *
   * @return component
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getComponent() {
    return component;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponent(String component) {
    this.component = component;
  }

  public IpReportIp componentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
    return this;
  }

  /**
   * Get componentInstance
   *
   * @return componentInstance
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getComponentInstance() {
    return componentInstance;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
  }

  public IpReportIp applicationphase(String applicationphase) {
    this.applicationphase = applicationphase;
    return this;
  }

  /**
   * Get applicationphase
   *
   * @return applicationphase
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_APPLICATIONPHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getApplicationphase() {
    return applicationphase;
  }

  @JsonProperty(value = JSON_PROPERTY_APPLICATIONPHASE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setApplicationphase(String applicationphase) {
    this.applicationphase = applicationphase;
  }

  public IpReportIp implementationZone(String implementationZone) {
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

  public IpReportIp ipaUrlHash(String ipaUrlHash) {
    this.ipaUrlHash = ipaUrlHash;
    return this;
  }

  /**
   * Get ipaUrlHash
   *
   * @return ipaUrlHash
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_IPA_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getIpaUrlHash() {
    return ipaUrlHash;
  }

  @JsonProperty(value = JSON_PROPERTY_IPA_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIpaUrlHash(String ipaUrlHash) {
    this.ipaUrlHash = ipaUrlHash;
  }

  public IpReportIp reUrlHash(String reUrlHash) {
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

  public IpReportIp ciUrlHash(String ciUrlHash) {
    this.ciUrlHash = ciUrlHash;
    return this;
  }

  /**
   * Get ciUrlHash
   *
   * @return ciUrlHash
   */
  @NotNull
  @JsonProperty(value = JSON_PROPERTY_CI_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getCiUrlHash() {
    return ciUrlHash;
  }

  @JsonProperty(value = JSON_PROPERTY_CI_URL_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCiUrlHash(String ciUrlHash) {
    this.ciUrlHash = ciUrlHash;
  }

  public IpReportIp izUrlHash(String izUrlHash) {
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

  /** Return true if this IpReportIp object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpReportIp ipReportIp = (IpReportIp) o;
    return Objects.equals(this.name, ipReportIp.name)
        && Objects.equals(this.host, ipReportIp.host)
        && Objects.equals(this.scope, ipReportIp.scope)
        && Objects.equals(this.winapproachAssignee, ipReportIp.winapproachAssignee)
        && Objects.equals(this.provider, ipReportIp.provider)
        && Objects.equals(this.continent, ipReportIp.continent)
        && Objects.equals(this.region, ipReportIp.region)
        && Objects.equals(this.target, ipReportIp.target)
        && Objects.equals(this.firewallgroup, ipReportIp.firewallgroup)
        && Objects.equals(this.landingzone, ipReportIp.landingzone)
        && Objects.equals(this.macrophase, ipReportIp.macrophase)
        && Objects.equals(this.component, ipReportIp.component)
        && Objects.equals(this.componentInstance, ipReportIp.componentInstance)
        && Objects.equals(this.applicationphase, ipReportIp.applicationphase)
        && Objects.equals(this.implementationZone, ipReportIp.implementationZone)
        && Objects.equals(this.ipaUrlHash, ipReportIp.ipaUrlHash)
        && Objects.equals(this.reUrlHash, ipReportIp.reUrlHash)
        && Objects.equals(this.ciUrlHash, ipReportIp.ciUrlHash)
        && Objects.equals(this.izUrlHash, ipReportIp.izUrlHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        host,
        scope,
        winapproachAssignee,
        provider,
        continent,
        region,
        target,
        firewallgroup,
        landingzone,
        macrophase,
        component,
        componentInstance,
        applicationphase,
        implementationZone,
        ipaUrlHash,
        reUrlHash,
        ciUrlHash,
        izUrlHash);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IpReportIp {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    host: ").append(toIndentedString(host)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    winapproachAssignee: ")
        .append(toIndentedString(winapproachAssignee))
        .append("\n");
    sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
    sb.append("    continent: ").append(toIndentedString(continent)).append("\n");
    sb.append("    region: ").append(toIndentedString(region)).append("\n");
    sb.append("    target: ").append(toIndentedString(target)).append("\n");
    sb.append("    firewallgroup: ").append(toIndentedString(firewallgroup)).append("\n");
    sb.append("    landingzone: ").append(toIndentedString(landingzone)).append("\n");
    sb.append("    macrophase: ").append(toIndentedString(macrophase)).append("\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    componentInstance: ").append(toIndentedString(componentInstance)).append("\n");
    sb.append("    applicationphase: ").append(toIndentedString(applicationphase)).append("\n");
    sb.append("    implementationZone: ").append(toIndentedString(implementationZone)).append("\n");
    sb.append("    ipaUrlHash: ").append(toIndentedString(ipaUrlHash)).append("\n");
    sb.append("    reUrlHash: ").append(toIndentedString(reUrlHash)).append("\n");
    sb.append("    ciUrlHash: ").append(toIndentedString(ciUrlHash)).append("\n");
    sb.append("    izUrlHash: ").append(toIndentedString(izUrlHash)).append("\n");
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

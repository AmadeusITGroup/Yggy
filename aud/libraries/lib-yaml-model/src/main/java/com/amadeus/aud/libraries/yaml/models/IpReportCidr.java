package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** IpReportCidr specification */
@Schema(description = "IpReportCidr specification")
@JsonPropertyOrder({
  IpReportCidr.JSON_PROPERTY_NAME,
  IpReportCidr.JSON_PROPERTY_SCOPE,
  IpReportCidr.JSON_PROPERTY_WINAPPROACH_ASSIGNEE,
  IpReportCidr.JSON_PROPERTY_PROVIDER,
  IpReportCidr.JSON_PROPERTY_CONTINENT,
  IpReportCidr.JSON_PROPERTY_REGION,
  IpReportCidr.JSON_PROPERTY_TARGET,
  IpReportCidr.JSON_PROPERTY_FIREWALLGROUP,
  IpReportCidr.JSON_PROPERTY_LANDINGZONE,
  IpReportCidr.JSON_PROPERTY_MACROPHASE,
  IpReportCidr.JSON_PROPERTY_COMPONENT,
  IpReportCidr.JSON_PROPERTY_COMPONENT_INSTANCE,
  IpReportCidr.JSON_PROPERTY_APPLICATIONPHASE,
  IpReportCidr.JSON_PROPERTY_RE_URL_HASH,
  IpReportCidr.JSON_PROPERTY_CI_URL_HASH
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpReportCidr {
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

  public static final String JSON_PROPERTY_RE_URL_HASH = "re_url_hash";
  private String reUrlHash;

  public static final String JSON_PROPERTY_CI_URL_HASH = "ci_url_hash";
  private String ciUrlHash;

  public IpReportCidr() {}

  public IpReportCidr name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   *
   * @return scope
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr winapproachAssignee(String winapproachAssignee) {
    this.winapproachAssignee = winapproachAssignee;
    return this;
  }

  /**
   * Get winapproachAssignee
   *
   * @return winapproachAssignee
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr provider(String provider) {
    this.provider = provider;
    return this;
  }

  /**
   * Get provider
   *
   * @return provider
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr continent(String continent) {
    this.continent = continent;
    return this;
  }

  /**
   * Get continent
   *
   * @return continent
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr region(String region) {
    this.region = region;
    return this;
  }

  /**
   * Get region
   *
   * @return region
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr target(String target) {
    this.target = target;
    return this;
  }

  /**
   * Get target
   *
   * @return target
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr firewallgroup(String firewallgroup) {
    this.firewallgroup = firewallgroup;
    return this;
  }

  /**
   * Get firewallgroup
   *
   * @return firewallgroup
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr landingzone(String landingzone) {
    this.landingzone = landingzone;
    return this;
  }

  /**
   * Get landingzone
   *
   * @return landingzone
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr macrophase(String macrophase) {
    this.macrophase = macrophase;
    return this;
  }

  /**
   * Get macrophase
   *
   * @return macrophase
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr component(String component) {
    this.component = component;
    return this;
  }

  /**
   * Get component
   *
   * @return component
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr componentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
    return this;
  }

  /**
   * Get componentInstance
   *
   * @return componentInstance
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr applicationphase(String applicationphase) {
    this.applicationphase = applicationphase;
    return this;
  }

  /**
   * Get applicationphase
   *
   * @return applicationphase
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr reUrlHash(String reUrlHash) {
    this.reUrlHash = reUrlHash;
    return this;
  }

  /**
   * Get reUrlHash
   *
   * @return reUrlHash
   */
  @NotNull
  @Schema(required = true, description = "")
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

  public IpReportCidr ciUrlHash(String ciUrlHash) {
    this.ciUrlHash = ciUrlHash;
    return this;
  }

  /**
   * Get ciUrlHash
   *
   * @return ciUrlHash
   */
  @NotNull
  @Schema(required = true, description = "")
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

  /** Return true if this IpReportCidr object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpReportCidr ipReportCidr = (IpReportCidr) o;
    return Objects.equals(this.name, ipReportCidr.name)
        && Objects.equals(this.scope, ipReportCidr.scope)
        && Objects.equals(this.winapproachAssignee, ipReportCidr.winapproachAssignee)
        && Objects.equals(this.provider, ipReportCidr.provider)
        && Objects.equals(this.continent, ipReportCidr.continent)
        && Objects.equals(this.region, ipReportCidr.region)
        && Objects.equals(this.target, ipReportCidr.target)
        && Objects.equals(this.firewallgroup, ipReportCidr.firewallgroup)
        && Objects.equals(this.landingzone, ipReportCidr.landingzone)
        && Objects.equals(this.macrophase, ipReportCidr.macrophase)
        && Objects.equals(this.component, ipReportCidr.component)
        && Objects.equals(this.componentInstance, ipReportCidr.componentInstance)
        && Objects.equals(this.applicationphase, ipReportCidr.applicationphase)
        && Objects.equals(this.reUrlHash, ipReportCidr.reUrlHash)
        && Objects.equals(this.ciUrlHash, ipReportCidr.ciUrlHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
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
        reUrlHash,
        ciUrlHash);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IpReportCidr {\n");
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
    sb.append("    landingzone: ").append(toIndentedString(landingzone)).append("\n");
    sb.append("    macrophase: ").append(toIndentedString(macrophase)).append("\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    componentInstance: ").append(toIndentedString(componentInstance)).append("\n");
    sb.append("    applicationphase: ").append(toIndentedString(applicationphase)).append("\n");
    sb.append("    reUrlHash: ").append(toIndentedString(reUrlHash)).append("\n");
    sb.append("    ciUrlHash: ").append(toIndentedString(ciUrlHash)).append("\n");
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

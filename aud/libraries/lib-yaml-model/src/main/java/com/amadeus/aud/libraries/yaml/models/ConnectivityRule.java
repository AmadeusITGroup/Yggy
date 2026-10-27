package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({
  ConnectivityRule.JSON_PROPERTY_NAME,
  ConnectivityRule.JSON_PROPERTY_UID,
  ConnectivityRule.JSON_PROPERTY_SRC_OBJECT_GROUP,
  ConnectivityRule.JSON_PROPERTY_DST_OBJECT_GROUP,
  ConnectivityRule.JSON_PROPERTY_PORTS,
  ConnectivityRule.JSON_PROPERTY_PROTOCOL,
  ConnectivityRule.JSON_PROPERTY_OWNER,
  ConnectivityRule.JSON_PROPERTY_SCOPE,
  ConnectivityRule.JSON_PROPERTY_IN_TRANSIT_FIREWALL_GROUPS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConnectivityRule extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_UID = "uid";
  private String uid;

  public static final String JSON_PROPERTY_SRC_OBJECT_GROUP = "srcObjectGroup";
  private ObjectGroup srcObjectGroup;

  public static final String JSON_PROPERTY_DST_OBJECT_GROUP = "dstObjectGroup";
  private ObjectGroup dstObjectGroup;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private ProtocolEnum protocol;

  public static final String JSON_PROPERTY_OWNER = "owner";
  private String owner;

  public static final String JSON_PROPERTY_SCOPE = "scope";
  @Nullable private String scope;

  public static final String JSON_PROPERTY_IN_TRANSIT_FIREWALL_GROUPS = "inTransitFirewallGroups";
  @Nullable private List<@Valid FirewallGroup> inTransitFirewallGroups;

  public ConnectivityRule() {}

  public ConnectivityRule name(String name) {
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

  public ConnectivityRule uid(String uid) {
    this.uid = uid;
    return this;
  }

  /**
   * Get uid
   *
   * @return uid
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_UID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getUid() {
    return uid;
  }

  @JsonProperty(value = JSON_PROPERTY_UID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUid(String uid) {
    this.uid = uid;
  }

  public ConnectivityRule srcObjectGroup(ObjectGroup srcObjectGroup) {
    this.srcObjectGroup = srcObjectGroup;
    return this;
  }

  /**
   * Get srcObjectGroup
   *
   * @return srcObjectGroup
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SRC_OBJECT_GROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ObjectGroup getSrcObjectGroup() {
    return srcObjectGroup;
  }

  @JsonProperty(value = JSON_PROPERTY_SRC_OBJECT_GROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSrcObjectGroup(ObjectGroup srcObjectGroup) {
    this.srcObjectGroup = srcObjectGroup;
  }

  public ConnectivityRule dstObjectGroup(ObjectGroup dstObjectGroup) {
    this.dstObjectGroup = dstObjectGroup;
    return this;
  }

  /**
   * Get dstObjectGroup
   *
   * @return dstObjectGroup
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DST_OBJECT_GROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ObjectGroup getDstObjectGroup() {
    return dstObjectGroup;
  }

  @JsonProperty(value = JSON_PROPERTY_DST_OBJECT_GROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDstObjectGroup(ObjectGroup dstObjectGroup) {
    this.dstObjectGroup = dstObjectGroup;
  }

  public ConnectivityRule ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public ConnectivityRule addPortsItem(String portsItem) {
    if (this.ports == null) {
      this.ports = new ArrayList<>();
    }
    this.ports.add(portsItem);
    return this;
  }

  /**
   * Get ports
   *
   * @return ports
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_PORTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getPorts() {
    return ports;
  }

  @JsonProperty(value = JSON_PROPERTY_PORTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPorts(List<String> ports) {
    this.ports = ports;
  }

  public ConnectivityRule protocol(ProtocolEnum protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * Get protocol
   *
   * @return protocol
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public ProtocolEnum getProtocol() {
    return protocol;
  }

  @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setProtocol(ProtocolEnum protocol) {
    this.protocol = protocol;
  }

  public ConnectivityRule owner(String owner) {
    this.owner = owner;
    return this;
  }

  /**
   * Get owner
   *
   * @return owner
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_OWNER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getOwner() {
    return owner;
  }

  @JsonProperty(value = JSON_PROPERTY_OWNER, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setOwner(String owner) {
    this.owner = owner;
  }

  public ConnectivityRule scope(@Nullable String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   *
   * @return scope
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_SCOPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getScope() {
    return scope;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setScope(@Nullable String scope) {
    this.scope = scope;
  }

  public ConnectivityRule inTransitFirewallGroups(
      @Nullable List<@Valid FirewallGroup> inTransitFirewallGroups) {
    this.inTransitFirewallGroups = inTransitFirewallGroups;
    return this;
  }

  public ConnectivityRule addInTransitFirewallGroupsItem(
      FirewallGroup inTransitFirewallGroupsItem) {
    if (this.inTransitFirewallGroups == null) {
      this.inTransitFirewallGroups = new ArrayList<>();
    }
    this.inTransitFirewallGroups.add(inTransitFirewallGroupsItem);
    return this;
  }

  /**
   * Get inTransitFirewallGroups
   *
   * @return inTransitFirewallGroups
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_IN_TRANSIT_FIREWALL_GROUPS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<@Valid FirewallGroup> getInTransitFirewallGroups() {
    return inTransitFirewallGroups;
  }

  @JsonProperty(value = JSON_PROPERTY_IN_TRANSIT_FIREWALL_GROUPS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setInTransitFirewallGroups(
      @Nullable List<@Valid FirewallGroup> inTransitFirewallGroups) {
    this.inTransitFirewallGroups = inTransitFirewallGroups;
  }

  @Override
  public ConnectivityRule labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this ConnectivityRule object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectivityRule connectivityRule = (ConnectivityRule) o;
    return Objects.equals(this.name, connectivityRule.name)
        && Objects.equals(this.uid, connectivityRule.uid)
        && Objects.equals(this.srcObjectGroup, connectivityRule.srcObjectGroup)
        && Objects.equals(this.dstObjectGroup, connectivityRule.dstObjectGroup)
        && Objects.equals(this.ports, connectivityRule.ports)
        && Objects.equals(this.protocol, connectivityRule.protocol)
        && Objects.equals(this.owner, connectivityRule.owner)
        && Objects.equals(this.scope, connectivityRule.scope)
        && Objects.equals(this.inTransitFirewallGroups, connectivityRule.inTransitFirewallGroups)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        uid,
        srcObjectGroup,
        dstObjectGroup,
        ports,
        protocol,
        owner,
        scope,
        inTransitFirewallGroups,
        super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectivityRule {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    srcObjectGroup: ").append(toIndentedString(srcObjectGroup)).append("\n");
    sb.append("    dstObjectGroup: ").append(toIndentedString(dstObjectGroup)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    inTransitFirewallGroups: ")
        .append(toIndentedString(inTransitFirewallGroups))
        .append("\n");
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

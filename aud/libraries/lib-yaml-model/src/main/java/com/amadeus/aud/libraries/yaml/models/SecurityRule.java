package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
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
  SecurityRule.JSON_PROPERTY_NAME,
  SecurityRule.JSON_PROPERTY_UID,
  SecurityRule.JSON_PROPERTY_SCOPE,
  SecurityRule.JSON_PROPERTY_OWNER,
  SecurityRule.JSON_PROPERTY_DEPENDENCY_INSTANCE_NAME,
  SecurityRule.JSON_PROPERTY_DEPENDENCY_INSTANCE_HASH,
  SecurityRule.JSON_PROPERTY_DEPENDENCY_INSTANCE_ID,
  SecurityRule.JSON_PROPERTY_SRC_OBJECT_GROUP,
  SecurityRule.JSON_PROPERTY_DST_OBJECT_GROUP,
  SecurityRule.JSON_PROPERTY_PORTS,
  SecurityRule.JSON_PROPERTY_PROTOCOL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SecurityRule extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_UID = "uid";
  private String uid;

  public static final String JSON_PROPERTY_SCOPE = "scope";
  private String scope;

  public static final String JSON_PROPERTY_OWNER = "owner";
  private String owner;

  public static final String JSON_PROPERTY_DEPENDENCY_INSTANCE_NAME = "dependencyInstanceName";
  private String dependencyInstanceName;

  public static final String JSON_PROPERTY_DEPENDENCY_INSTANCE_HASH = "dependencyInstanceHash";
  private String dependencyInstanceHash;

  public static final String JSON_PROPERTY_DEPENDENCY_INSTANCE_ID = "dependencyInstanceId";
  private String dependencyInstanceId;

  public static final String JSON_PROPERTY_SRC_OBJECT_GROUP = "srcObjectGroup";
  private ObjectGroupExtract srcObjectGroup;

  public static final String JSON_PROPERTY_DST_OBJECT_GROUP = "dstObjectGroup";
  private ObjectGroupExtract dstObjectGroup;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  /** Gets or Sets protocol */
  public enum ProtocolEnum {
    TCP(String.valueOf("Tcp")),

    UDP(String.valueOf("Udp")),

    ICMP(String.valueOf("Icmp")),

    STAR(String.valueOf("*"));

    private String value;

    ProtocolEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ProtocolEnum fromValue(String value) {
      for (ProtocolEnum b : ProtocolEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  private ProtocolEnum protocol;

  public SecurityRule() {}

  public SecurityRule name(String name) {
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

  public SecurityRule uid(String uid) {
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

  public SecurityRule scope(String scope) {
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

  public SecurityRule owner(String owner) {
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

  public SecurityRule dependencyInstanceName(String dependencyInstanceName) {
    this.dependencyInstanceName = dependencyInstanceName;
    return this;
  }

  /**
   * Get dependencyInstanceName
   *
   * @return dependencyInstanceName
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCE_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDependencyInstanceName() {
    return dependencyInstanceName;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCE_NAME, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDependencyInstanceName(String dependencyInstanceName) {
    this.dependencyInstanceName = dependencyInstanceName;
  }

  public SecurityRule dependencyInstanceHash(String dependencyInstanceHash) {
    this.dependencyInstanceHash = dependencyInstanceHash;
    return this;
  }

  /**
   * Get dependencyInstanceHash
   *
   * @return dependencyInstanceHash
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCE_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDependencyInstanceHash() {
    return dependencyInstanceHash;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCE_HASH, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDependencyInstanceHash(String dependencyInstanceHash) {
    this.dependencyInstanceHash = dependencyInstanceHash;
  }

  public SecurityRule dependencyInstanceId(String dependencyInstanceId) {
    this.dependencyInstanceId = dependencyInstanceId;
    return this;
  }

  /**
   * Get dependencyInstanceId
   *
   * @return dependencyInstanceId
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCE_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDependencyInstanceId() {
    return dependencyInstanceId;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCE_ID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDependencyInstanceId(String dependencyInstanceId) {
    this.dependencyInstanceId = dependencyInstanceId;
  }

  public SecurityRule srcObjectGroup(ObjectGroupExtract srcObjectGroup) {
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
  public ObjectGroupExtract getSrcObjectGroup() {
    return srcObjectGroup;
  }

  @JsonProperty(value = JSON_PROPERTY_SRC_OBJECT_GROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSrcObjectGroup(ObjectGroupExtract srcObjectGroup) {
    this.srcObjectGroup = srcObjectGroup;
  }

  public SecurityRule dstObjectGroup(ObjectGroupExtract dstObjectGroup) {
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
  public ObjectGroupExtract getDstObjectGroup() {
    return dstObjectGroup;
  }

  @JsonProperty(value = JSON_PROPERTY_DST_OBJECT_GROUP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDstObjectGroup(ObjectGroupExtract dstObjectGroup) {
    this.dstObjectGroup = dstObjectGroup;
  }

  public SecurityRule ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public SecurityRule addPortsItem(String portsItem) {
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

  public SecurityRule protocol(ProtocolEnum protocol) {
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

  @Override
  public SecurityRule labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this SecurityRule object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SecurityRule securityRule = (SecurityRule) o;
    return Objects.equals(this.name, securityRule.name)
        && Objects.equals(this.uid, securityRule.uid)
        && Objects.equals(this.scope, securityRule.scope)
        && Objects.equals(this.owner, securityRule.owner)
        && Objects.equals(this.dependencyInstanceName, securityRule.dependencyInstanceName)
        && Objects.equals(this.dependencyInstanceHash, securityRule.dependencyInstanceHash)
        && Objects.equals(this.dependencyInstanceId, securityRule.dependencyInstanceId)
        && Objects.equals(this.srcObjectGroup, securityRule.srcObjectGroup)
        && Objects.equals(this.dstObjectGroup, securityRule.dstObjectGroup)
        && Objects.equals(this.ports, securityRule.ports)
        && Objects.equals(this.protocol, securityRule.protocol)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        name,
        uid,
        scope,
        owner,
        dependencyInstanceName,
        dependencyInstanceHash,
        dependencyInstanceId,
        srcObjectGroup,
        dstObjectGroup,
        ports,
        protocol,
        super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SecurityRule {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    dependencyInstanceName: ")
        .append(toIndentedString(dependencyInstanceName))
        .append("\n");
    sb.append("    dependencyInstanceHash: ")
        .append(toIndentedString(dependencyInstanceHash))
        .append("\n");
    sb.append("    dependencyInstanceId: ")
        .append(toIndentedString(dependencyInstanceId))
        .append("\n");
    sb.append("    srcObjectGroup: ").append(toIndentedString(srcObjectGroup)).append("\n");
    sb.append("    dstObjectGroup: ").append(toIndentedString(dstObjectGroup)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
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

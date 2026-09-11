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
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Service specification */
@Schema(description = "Service specification")
@JsonPropertyOrder({
  V1ServiceSpec.JSON_PROPERTY_COMPONENT,
  V1ServiceSpec.JSON_PROPERTY_IS_PRIVATE,
  V1ServiceSpec.JSON_PROPERTY_IS_P_C_I,
  V1ServiceSpec.JSON_PROPERTY_IS_P_I_I,
  V1ServiceSpec.JSON_PROPERTY_IS_ENCRYPTED,
  V1ServiceSpec.JSON_PROPERTY_IS_D_B_SYNCHRO,
  V1ServiceSpec.JSON_PROPERTY_APPLICATION_ID,
  V1ServiceSpec.JSON_PROPERTY_PORTS,
  V1ServiceSpec.JSON_PROPERTY_PROTOCOL,
  V1ServiceSpec.JSON_PROPERTY_DEPRECATED_STATUS,
  V1ServiceSpec.JSON_PROPERTY_DEPRECATED_MESSAGE,
  V1ServiceSpec.JSON_PROPERTY_SECURITY_JUSTIFICATION
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ServiceSpec {
  public static final String JSON_PROPERTY_COMPONENT = "component";
  private String component;

  public static final String JSON_PROPERTY_IS_PRIVATE = "isPrivate";
  @Nullable private Boolean isPrivate = false;

  public static final String JSON_PROPERTY_IS_P_C_I = "isPCI";
  private Boolean isPCI = false;

  public static final String JSON_PROPERTY_IS_P_I_I = "isPII";
  private Boolean isPII = false;

  public static final String JSON_PROPERTY_IS_ENCRYPTED = "isEncrypted";
  private Boolean isEncrypted = false;

  public static final String JSON_PROPERTY_IS_D_B_SYNCHRO = "isDBSynchro";
  private Boolean isDBSynchro = false;

  public static final String JSON_PROPERTY_APPLICATION_ID = "applicationId";
  @Nullable private String applicationId;

  public static final String JSON_PROPERTY_PORTS = "ports";
  private List<String> ports;

  /** The protocol e.g. TCP, UDP, Icmp. */
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

  public static final String JSON_PROPERTY_DEPRECATED_STATUS = "deprecatedStatus";
  @Nullable private DeprecatedStatus deprecatedStatus = DeprecatedStatus.NONE;

  public static final String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecatedMessage";
  @Nullable private String deprecatedMessage;

  public static final String JSON_PROPERTY_SECURITY_JUSTIFICATION = "securityJustification";
  @Nullable private String securityJustification;

  public V1ServiceSpec() {}

  public V1ServiceSpec component(String component) {
    this.component = component;
    return this;
  }

  /**
   * Name of component
   *
   * @return component
   */
  @NotNull
  @Schema(required = true, description = "Name of component")
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

  public V1ServiceSpec isPrivate(@Nullable Boolean isPrivate) {
    this.isPrivate = isPrivate;
    return this;
  }

  /**
   * is it private to your scope?
   *
   * @return isPrivate
   */
  @Schema(required = false, description = "is it private to your scope?")
  @JsonProperty(value = JSON_PROPERTY_IS_PRIVATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Boolean getIsPrivate() {
    return isPrivate;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_PRIVATE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsPrivate(@Nullable Boolean isPrivate) {
    this.isPrivate = isPrivate;
  }

  public V1ServiceSpec isPCI(Boolean isPCI) {
    this.isPCI = isPCI;
    return this;
  }

  /**
   * Is PCI Information involved in the flow?
   *
   * @return isPCI
   */
  @NotNull
  @Schema(required = true, description = "Is PCI Information involved in the flow?")
  @JsonProperty(value = JSON_PROPERTY_IS_P_C_I, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsPCI() {
    return isPCI;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_P_C_I, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsPCI(Boolean isPCI) {
    this.isPCI = isPCI;
  }

  public V1ServiceSpec isPII(Boolean isPII) {
    this.isPII = isPII;
    return this;
  }

  /**
   * Does it contain Personally identifiable information (PII)?
   *
   * @return isPII
   */
  @NotNull
  @Schema(
      required = true,
      description = "Does it contain Personally identifiable information (PII)?")
  @JsonProperty(value = JSON_PROPERTY_IS_P_I_I, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsPII() {
    return isPII;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_P_I_I, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsPII(Boolean isPII) {
    this.isPII = isPII;
  }

  public V1ServiceSpec isEncrypted(Boolean isEncrypted) {
    this.isEncrypted = isEncrypted;
    return this;
  }

  /**
   * is it Encrypted?
   *
   * @return isEncrypted
   */
  @NotNull
  @Schema(required = true, description = "is it Encrypted?")
  @JsonProperty(value = JSON_PROPERTY_IS_ENCRYPTED, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsEncrypted() {
    return isEncrypted;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_ENCRYPTED, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsEncrypted(Boolean isEncrypted) {
    this.isEncrypted = isEncrypted;
  }

  public V1ServiceSpec isDBSynchro(Boolean isDBSynchro) {
    this.isDBSynchro = isDBSynchro;
    return this;
  }

  /**
   * is it used for DB Synchronization?
   *
   * @return isDBSynchro
   */
  @NotNull
  @Schema(required = true, description = "is it used for DB Synchronization?")
  @JsonProperty(value = JSON_PROPERTY_IS_D_B_SYNCHRO, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsDBSynchro() {
    return isDBSynchro;
  }

  @JsonProperty(value = JSON_PROPERTY_IS_D_B_SYNCHRO, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setIsDBSynchro(Boolean isDBSynchro) {
    this.isDBSynchro = isDBSynchro;
  }

  public V1ServiceSpec applicationId(@Nullable String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  /**
   * application Id
   *
   * @return applicationId
   */
  @Schema(required = false, description = "application Id")
  @JsonProperty(value = JSON_PROPERTY_APPLICATION_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getApplicationId() {
    return applicationId;
  }

  @JsonProperty(value = JSON_PROPERTY_APPLICATION_ID, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setApplicationId(@Nullable String applicationId) {
    this.applicationId = applicationId;
  }

  public V1ServiceSpec ports(List<String> ports) {
    this.ports = ports;
    return this;
  }

  public V1ServiceSpec addPortsItem(String portsItem) {
    if (this.ports == null) {
      this.ports = new ArrayList<>();
    }
    this.ports.add(portsItem);
    return this;
  }

  /**
   * The port information (list of port or port-range)
   *
   * @return ports
   */
  @NotNull
  @Schema(required = true, description = "The port information (list of port or port-range)")
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

  public V1ServiceSpec protocol(ProtocolEnum protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * The protocol e.g. TCP, UDP, Icmp.
   *
   * @return protocol
   */
  @NotNull
  @Schema(required = true, description = "The protocol e.g. TCP, UDP, Icmp.")
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

  public V1ServiceSpec deprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
    return this;
  }

  /**
   * Get deprecatedStatus
   *
   * @return deprecatedStatus
   */
  @Valid
  @Schema(required = false, description = "Deprecated status of the service.")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return deprecatedStatus;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_STATUS, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedStatus(@Nullable DeprecatedStatus deprecatedStatus) {
    this.deprecatedStatus = deprecatedStatus;
  }

  public V1ServiceSpec deprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
    return this;
  }

  /**
   * Additional info on the deprecated Service (e.g. replacement information/ owner contact details)
   *
   * @return deprecatedMessage
   */
  @Schema(
      required = false,
      description =
          "Additional info on the deprecated Service (e.g. replacement information/ owner contact details)")
  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDeprecatedMessage() {
    return deprecatedMessage;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPRECATED_MESSAGE, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeprecatedMessage(@Nullable String deprecatedMessage) {
    this.deprecatedMessage = deprecatedMessage;
  }

  public V1ServiceSpec securityJustification(@Nullable String securityJustification) {
    this.securityJustification = securityJustification;
    return this;
  }

  /**
   * Each service should describe what is it for.
   *
   * @return securityJustification
   */
  @Schema(required = false, description = "Each service should describe what is it for.")
  @JsonProperty(value = JSON_PROPERTY_SECURITY_JUSTIFICATION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getSecurityJustification() {
    return securityJustification;
  }

  @JsonProperty(value = JSON_PROPERTY_SECURITY_JUSTIFICATION, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSecurityJustification(@Nullable String securityJustification) {
    this.securityJustification = securityJustification;
  }

  /** Return true if this v1.ServiceSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ServiceSpec v1ServiceSpec = (V1ServiceSpec) o;
    return Objects.equals(this.component, v1ServiceSpec.component)
        && Objects.equals(this.isPrivate, v1ServiceSpec.isPrivate)
        && Objects.equals(this.isPCI, v1ServiceSpec.isPCI)
        && Objects.equals(this.isPII, v1ServiceSpec.isPII)
        && Objects.equals(this.isEncrypted, v1ServiceSpec.isEncrypted)
        && Objects.equals(this.isDBSynchro, v1ServiceSpec.isDBSynchro)
        && Objects.equals(this.applicationId, v1ServiceSpec.applicationId)
        && Objects.equals(this.ports, v1ServiceSpec.ports)
        && Objects.equals(this.protocol, v1ServiceSpec.protocol)
        && Objects.equals(this.deprecatedStatus, v1ServiceSpec.deprecatedStatus)
        && Objects.equals(this.deprecatedMessage, v1ServiceSpec.deprecatedMessage)
        && Objects.equals(this.securityJustification, v1ServiceSpec.securityJustification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        component,
        isPrivate,
        isPCI,
        isPII,
        isEncrypted,
        isDBSynchro,
        applicationId,
        ports,
        protocol,
        deprecatedStatus,
        deprecatedMessage,
        securityJustification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ServiceSpec {\n");
    sb.append("    component: ").append(toIndentedString(component)).append("\n");
    sb.append("    isPrivate: ").append(toIndentedString(isPrivate)).append("\n");
    sb.append("    isPCI: ").append(toIndentedString(isPCI)).append("\n");
    sb.append("    isPII: ").append(toIndentedString(isPII)).append("\n");
    sb.append("    isEncrypted: ").append(toIndentedString(isEncrypted)).append("\n");
    sb.append("    isDBSynchro: ").append(toIndentedString(isDBSynchro)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    ports: ").append(toIndentedString(ports)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    deprecatedStatus: ").append(toIndentedString(deprecatedStatus)).append("\n");
    sb.append("    deprecatedMessage: ").append(toIndentedString(deprecatedMessage)).append("\n");
    sb.append("    securityJustification: ")
        .append(toIndentedString(securityJustification))
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

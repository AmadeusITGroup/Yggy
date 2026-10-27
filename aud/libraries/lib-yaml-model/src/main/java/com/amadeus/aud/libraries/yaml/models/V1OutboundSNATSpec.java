package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1OutboundSNATSpec */
@JsonPropertyOrder({
  V1OutboundSNATSpec.JSON_PROPERTY_TRANSLATEDIP,
  V1OutboundSNATSpec.JSON_PROPERTY_MACROPHASE,
  V1OutboundSNATSpec.JSON_PROPERTY_CONTINENT,
  V1OutboundSNATSpec.JSON_PROPERTY_ZONETYPE,
  V1OutboundSNATSpec.JSON_PROPERTY_COMPONENTS,
  V1OutboundSNATSpec.JSON_PROPERTY_COMPONENT_INSTANCES,
  V1OutboundSNATSpec.JSON_PROPERTY_CIDRS,
  V1OutboundSNATSpec.JSON_PROPERTY_DESTINATION_CIDRS,
  V1OutboundSNATSpec.JSON_PROPERTY_DESTINATION_FQDNS,
  V1OutboundSNATSpec.JSON_PROPERTY_PROTOCOL
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1OutboundSNATSpec {
  public static final String JSON_PROPERTY_TRANSLATEDIP = "translatedip";
  private String translatedip;

  public static final String JSON_PROPERTY_MACROPHASE = "macrophase";
  private String macrophase;

  public static final String JSON_PROPERTY_CONTINENT = "continent";
  private String continent;

  public static final String JSON_PROPERTY_ZONETYPE = "zonetype";
  private String zonetype;

  public static final String JSON_PROPERTY_COMPONENTS = "components";
  @Nullable private List<String> components;

  public static final String JSON_PROPERTY_COMPONENT_INSTANCES = "component_instances";
  @Nullable private List<String> componentInstances;

  public static final String JSON_PROPERTY_CIDRS = "cidrs";
  @Nullable private List<String> cidrs;

  public static final String JSON_PROPERTY_DESTINATION_CIDRS = "destination_cidrs";
  @Nullable private List<String> destinationCidrs;

  public static final String JSON_PROPERTY_DESTINATION_FQDNS = "destination_fqdns";
  @Nullable private List<String> destinationFqdns;

  /** protocol */
  public enum ProtocolEnum {
    ANY("Any"),
    TCP("Tcp"),
    UDP("Udp");

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

  public V1OutboundSNATSpec() {}

  public V1OutboundSNATSpec translatedip(String translatedip) {
    this.translatedip = translatedip;
    return this;
  }

  /**
   * IP used for SNAT result
   *
   * @return translatedip
   */
  @NotNull
  @Schema(required = true, description = "IP used for SNAT result")
  @JsonProperty(value = JSON_PROPERTY_TRANSLATEDIP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTranslatedip() {
    return translatedip;
  }

  @JsonProperty(value = JSON_PROPERTY_TRANSLATEDIP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTranslatedip(String translatedip) {
    this.translatedip = translatedip;
  }

  public V1OutboundSNATSpec macrophase(String macrophase) {
    this.macrophase = macrophase;
    return this;
  }

  /**
   * Name of macrophase.
   *
   * @return macrophase
   */
  @NotNull
  @Schema(required = true, description = "Name of macrophase.")
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

  public V1OutboundSNATSpec continent(String continent) {
    this.continent = continent;
    return this;
  }

  /**
   * Name of continent.
   *
   * @return continent
   */
  @NotNull
  @Schema(required = true, description = "Name of continent.")
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

  public V1OutboundSNATSpec zonetype(String zonetype) {
    this.zonetype = zonetype;
    return this;
  }

  /**
   * Name of securityzone.
   *
   * @return zonetype
   */
  @NotNull
  @Schema(required = true, description = "Name of securityzone.")
  @JsonProperty(value = JSON_PROPERTY_ZONETYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getZonetype() {
    return zonetype;
  }

  @JsonProperty(value = JSON_PROPERTY_ZONETYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setZonetype(String zonetype) {
    this.zonetype = zonetype;
  }

  public V1OutboundSNATSpec components(@Nullable List<String> components) {
    this.components = components;
    return this;
  }

  public V1OutboundSNATSpec addComponentsItem(String componentsItem) {
    if (this.components == null) {
      this.components = new ArrayList<>();
    }
    this.components.add(componentsItem);
    return this;
  }

  /**
   * List of component
   *
   * @return components
   */
  @Schema(description = "List of component")
  @JsonProperty(value = JSON_PROPERTY_COMPONENTS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getComponents() {
    return components;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENTS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setComponents(@Nullable List<String> components) {
    this.components = components;
  }

  public V1OutboundSNATSpec componentInstances(@Nullable List<String> componentInstances) {
    this.componentInstances = componentInstances;
    return this;
  }

  public V1OutboundSNATSpec addComponentInstancesItem(String componentInstancesItem) {
    if (this.componentInstances == null) {
      this.componentInstances = new ArrayList<>();
    }
    this.componentInstances.add(componentInstancesItem);
    return this;
  }

  /**
   * List of component_instance
   *
   * @return componentInstances
   */
  @Schema(description = "List of component_instance")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getComponentInstances() {
    return componentInstances;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setComponentInstances(@Nullable List<String> componentInstances) {
    this.componentInstances = componentInstances;
  }

  public V1OutboundSNATSpec cidrs(@Nullable List<String> cidrs) {
    this.cidrs = cidrs;
    return this;
  }

  public V1OutboundSNATSpec addCidrsItem(String cidrsItem) {
    if (this.cidrs == null) {
      this.cidrs = new ArrayList<>();
    }
    this.cidrs.add(cidrsItem);
    return this;
  }

  /**
   * List of vnet
   *
   * @return cidrs
   */
  @Schema(description = "List of vnet")
  @JsonProperty(value = JSON_PROPERTY_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getCidrs() {
    return cidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCidrs(@Nullable List<String> cidrs) {
    this.cidrs = cidrs;
  }

  public V1OutboundSNATSpec destinationCidrs(@Nullable List<String> destinationCidrs) {
    this.destinationCidrs = destinationCidrs;
    return this;
  }

  public V1OutboundSNATSpec addDestinationCidrsItem(String destinationCidrsItem) {
    if (this.destinationCidrs == null) {
      this.destinationCidrs = new ArrayList<>();
    }
    this.destinationCidrs.add(destinationCidrsItem);
    return this;
  }

  /**
   * List of vnet
   *
   * @return destinationCidrs
   */
  @Schema(description = "List of vnet")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getDestinationCidrs() {
    return destinationCidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDestinationCidrs(@Nullable List<String> destinationCidrs) {
    this.destinationCidrs = destinationCidrs;
  }

  public V1OutboundSNATSpec destinationFqdns(@Nullable List<String> destinationFqdns) {
    this.destinationFqdns = destinationFqdns;
    return this;
  }

  public V1OutboundSNATSpec addDestinationFqdnsItem(String destinationFqdnsItem) {
    if (this.destinationFqdns == null) {
      this.destinationFqdns = new ArrayList<>();
    }
    this.destinationFqdns.add(destinationFqdnsItem);
    return this;
  }

  /**
   * List of fqdn
   *
   * @return destinationFqdns
   */
  @Schema(description = "List of fqdn")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_FQDNS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getDestinationFqdns() {
    return destinationFqdns;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_FQDNS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDestinationFqdns(@Nullable List<String> destinationFqdns) {
    this.destinationFqdns = destinationFqdns;
  }

  public V1OutboundSNATSpec protocol(ProtocolEnum protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * protocol
   *
   * @return protocol
   */
  @NotNull
  @Schema(required = true, description = "protocol")
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

  /** Return true if this v1.OutboundSNATSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1OutboundSNATSpec v1OutboundSNATSpec = (V1OutboundSNATSpec) o;
    return Objects.equals(this.translatedip, v1OutboundSNATSpec.translatedip)
        && Objects.equals(this.macrophase, v1OutboundSNATSpec.macrophase)
        && Objects.equals(this.continent, v1OutboundSNATSpec.continent)
        && Objects.equals(this.zonetype, v1OutboundSNATSpec.zonetype)
        && Objects.equals(this.components, v1OutboundSNATSpec.components)
        && Objects.equals(this.componentInstances, v1OutboundSNATSpec.componentInstances)
        && Objects.equals(this.cidrs, v1OutboundSNATSpec.cidrs)
        && Objects.equals(this.destinationCidrs, v1OutboundSNATSpec.destinationCidrs)
        && Objects.equals(this.destinationFqdns, v1OutboundSNATSpec.destinationFqdns)
        && Objects.equals(this.protocol, v1OutboundSNATSpec.protocol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        translatedip,
        macrophase,
        continent,
        zonetype,
        components,
        componentInstances,
        cidrs,
        destinationCidrs,
        destinationFqdns,
        protocol);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1OutboundSNATSpec {\n");
    sb.append("    translatedip: ").append(toIndentedString(translatedip)).append("\n");
    sb.append("    macrophase: ").append(toIndentedString(macrophase)).append("\n");
    sb.append("    continent: ").append(toIndentedString(continent)).append("\n");
    sb.append("    zonetype: ").append(toIndentedString(zonetype)).append("\n");
    sb.append("    components: ").append(toIndentedString(components)).append("\n");
    sb.append("    componentInstances: ").append(toIndentedString(componentInstances)).append("\n");
    sb.append("    cidrs: ").append(toIndentedString(cidrs)).append("\n");
    sb.append("    destinationCidrs: ").append(toIndentedString(destinationCidrs)).append("\n");
    sb.append("    destinationFqdns: ").append(toIndentedString(destinationFqdns)).append("\n");
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

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

/** Unified blacklist specification for FQDN or CIDR */
@Schema(description = "Unified blacklist specification for FQDN or CIDR")
@JsonPropertyOrder({
  V1BlacklistSpec.JSON_PROPERTY_SOURCE_COMPONENT,
  V1BlacklistSpec.JSON_PROPERTY_DESTINATION_HOST_FQDNS,
  V1BlacklistSpec.JSON_PROPERTY_DESTINATION_HOST_CIDRS,
  V1BlacklistSpec.JSON_PROPERTY_SOURCE_ZONES,
  V1BlacklistSpec.JSON_PROPERTY_SOURCE_MACROPHASES,
  V1BlacklistSpec.JSON_PROPERTY_SOURCE_TARGETS,
  V1BlacklistSpec.JSON_PROPERTY_DESTINATION_APPLICATION_PROTOCOLS,
  V1BlacklistSpec.JSON_PROPERTY_DESTINATION_TRANSPORT_PROTOCOLS,
  V1BlacklistSpec.JSON_PROPERTY_DESTINATION_PORT_RANGES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1BlacklistSpec {
  public static final String JSON_PROPERTY_SOURCE_COMPONENT = "source_component";
  public static final String JSON_PROPERTY_DESTINATION_HOST_FQDNS = "destination_host_fqdns";
  public static final String JSON_PROPERTY_DESTINATION_HOST_CIDRS = "destination_host_cidrs";
  public static final String JSON_PROPERTY_SOURCE_ZONES = "source_zones";
  public static final String JSON_PROPERTY_SOURCE_MACROPHASES = "source_macrophases";
  public static final String JSON_PROPERTY_SOURCE_TARGETS = "source_targets";
  public static final String JSON_PROPERTY_DESTINATION_TRANSPORT_PROTOCOLS =
      "destination_transport_protocols";
  public static final String JSON_PROPERTY_DESTINATION_PORT_RANGES = "destination_port_ranges";
  public static final String JSON_PROPERTY_DESTINATION_APPLICATION_PROTOCOLS =
      "destination_application_protocols";

  private String sourceComponent;

  @Nullable private List<String> destinationHostFqdns;

  @Nullable private List<String> destinationHostCidrs;

  private List<String> sourceZones;

  private List<String> sourceMacrophases;

  private List<String> sourceTargets;

  /** Gets or Sets destinationApplicationProtocols */
  public enum DestinationApplicationProtocolsEnum {
    HTTP(String.valueOf("http")),

    HTTPS(String.valueOf("https")),

    STAR(String.valueOf("*"));

    private String value;

    DestinationApplicationProtocolsEnum(String value) {
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
    public static DestinationApplicationProtocolsEnum fromValue(String value) {
      for (DestinationApplicationProtocolsEnum b : DestinationApplicationProtocolsEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @Nullable private List<DestinationApplicationProtocolsEnum> destinationApplicationProtocols;

  public enum DestinationTransportProtocolsEnum {
    TCP(String.valueOf("Tcp")),

    UDP(String.valueOf("Udp")),

    STAR(String.valueOf("*"));

    private String value;

    DestinationTransportProtocolsEnum(String value) {
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
    public static DestinationTransportProtocolsEnum fromValue(String value) {
      for (DestinationTransportProtocolsEnum b : DestinationTransportProtocolsEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private List<DestinationTransportProtocolsEnum> destinationTransportProtocols;

  private List<String> destinationPortRanges;

  public V1BlacklistSpec() {}

  public V1BlacklistSpec sourceComponent(String sourceComponent) {
    this.sourceComponent = sourceComponent;
    return this;
  }

  /**
   * Source component name or &#39;*&#39; for all
   *
   * @return sourceComponent
   */
  @NotNull
  @Schema(required = true, description = "Source component name or '*' for all")
  @JsonProperty(value = JSON_PROPERTY_SOURCE_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSourceComponent() {
    return sourceComponent;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE_COMPONENT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSourceComponent(String sourceComponent) {
    this.sourceComponent = sourceComponent;
  }

  public V1BlacklistSpec destinationHostFqdns(@Nullable List<String> destinationHostFqdns) {
    this.destinationHostFqdns = destinationHostFqdns;
    return this;
  }

  public V1BlacklistSpec addDestinationHostFqdnsItem(String destinationHostFqdnsItem) {
    if (this.destinationHostFqdns == null) {
      this.destinationHostFqdns = new ArrayList<>();
    }
    this.destinationHostFqdns.add(destinationHostFqdnsItem);
    return this;
  }

  /**
   * FQDN list (supports *.domain or *), mutually exclusive with destination_host_cidrs
   *
   * @return destinationHostFqdns
   */
  @Schema(
      description =
          "FQDN list (supports *.domain or *), mutually exclusive with destination_host_cidrs")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_HOST_FQDNS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getDestinationHostFqdns() {
    return destinationHostFqdns;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_HOST_FQDNS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDestinationHostFqdns(@Nullable List<String> destinationHostFqdns) {
    this.destinationHostFqdns = destinationHostFqdns;
  }

  public V1BlacklistSpec destinationHostCidrs(@Nullable List<String> destinationHostCidrs) {
    this.destinationHostCidrs = destinationHostCidrs;
    return this;
  }

  public V1BlacklistSpec addDestinationHostCidrsItem(String destinationHostCidrsItem) {
    if (this.destinationHostCidrs == null) {
      this.destinationHostCidrs = new ArrayList<>();
    }
    this.destinationHostCidrs.add(destinationHostCidrsItem);
    return this;
  }

  /**
   * CIDR list (e.g., 10.0.0.0/8 or *), mutually exclusive with destination_host_fqdns
   *
   * @return destinationHostCidrs
   */
  @Schema(
      description =
          "CIDR list (e.g., 10.0.0.0/8 or *), mutually exclusive with destination_host_fqdns")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_HOST_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getDestinationHostCidrs() {
    return destinationHostCidrs;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_HOST_CIDRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDestinationHostCidrs(@Nullable List<String> destinationHostCidrs) {
    this.destinationHostCidrs = destinationHostCidrs;
  }

  public V1BlacklistSpec sourceZones(List<String> sourceZones) {
    this.sourceZones = sourceZones;
    return this;
  }

  public V1BlacklistSpec addSourceZonesItem(String sourceZonesItem) {
    if (this.sourceZones == null) {
      this.sourceZones = new ArrayList<>();
    }
    this.sourceZones.add(sourceZonesItem);
    return this;
  }

  /**
   * Security zones (list, &#39;*&#39; for all, &#39;+&#39; for all except CDE)
   *
   * @return sourceZones
   */
  @NotNull
  @Schema(
      required = true,
      description = "Security zones (list, '*' for all, '+' for all except CDE)")
  @JsonProperty(value = JSON_PROPERTY_SOURCE_ZONES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getSourceZones() {
    return sourceZones;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE_ZONES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSourceZones(List<String> sourceZones) {
    this.sourceZones = sourceZones;
  }

  public V1BlacklistSpec sourceMacrophases(List<String> sourceMacrophases) {
    this.sourceMacrophases = sourceMacrophases;
    return this;
  }

  public V1BlacklistSpec addSourceMacrophasesItem(String sourceMacrophasesItem) {
    if (this.sourceMacrophases == null) {
      this.sourceMacrophases = new ArrayList<>();
    }
    this.sourceMacrophases.add(sourceMacrophasesItem);
    return this;
  }

  /**
   * Macrophases (list or &#39;*&#39; for all)
   *
   * @return sourceMacrophases
   */
  @NotNull
  @Schema(required = true, description = "Macrophases (list or '*' for all)")
  @JsonProperty(value = JSON_PROPERTY_SOURCE_MACROPHASES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getSourceMacrophases() {
    return sourceMacrophases;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE_MACROPHASES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSourceMacrophases(List<String> sourceMacrophases) {
    this.sourceMacrophases = sourceMacrophases;
  }

  public V1BlacklistSpec sourceTargets(List<String> sourceTargets) {
    this.sourceTargets = sourceTargets;
    return this;
  }

  public V1BlacklistSpec addSourceTargetsItem(String sourceTargetsItem) {
    if (this.sourceTargets == null) {
      this.sourceTargets = new ArrayList<>();
    }
    this.sourceTargets.add(sourceTargetsItem);
    return this;
  }

  /**
   * Targets (list or &#39;*&#39; for all)
   *
   * @return sourceTargets
   */
  @NotNull
  @Schema(required = true, description = "Targets (list or '*' for all)")
  @JsonProperty(value = JSON_PROPERTY_SOURCE_TARGETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getSourceTargets() {
    return sourceTargets;
  }

  @JsonProperty(value = JSON_PROPERTY_SOURCE_TARGETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSourceTargets(List<String> sourceTargets) {
    this.sourceTargets = sourceTargets;
  }

  public V1BlacklistSpec destinationApplicationProtocols(
      @Nullable List<DestinationApplicationProtocolsEnum> destinationApplicationProtocols) {
    this.destinationApplicationProtocols = destinationApplicationProtocols;
    return this;
  }

  public V1BlacklistSpec addDestinationApplicationProtocolsItem(
      DestinationApplicationProtocolsEnum destinationApplicationProtocolsItem) {
    if (this.destinationApplicationProtocols == null) {
      this.destinationApplicationProtocols = new ArrayList<>();
    }
    this.destinationApplicationProtocols.add(destinationApplicationProtocolsItem);
    return this;
  }

  /**
   * Application protocols (http, https, etc. Optional)
   *
   * @return destinationApplicationProtocols
   */
  @Schema(description = "Application protocols (http, https, etc. Optional)")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_APPLICATION_PROTOCOLS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<DestinationApplicationProtocolsEnum> getDestinationApplicationProtocols() {
    return destinationApplicationProtocols;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_APPLICATION_PROTOCOLS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDestinationApplicationProtocols(
      @Nullable List<DestinationApplicationProtocolsEnum> destinationApplicationProtocols) {
    this.destinationApplicationProtocols = destinationApplicationProtocols;
  }

  public V1BlacklistSpec destinationTransportProtocols(
      List<DestinationTransportProtocolsEnum> destinationTransportProtocols) {
    this.destinationTransportProtocols = destinationTransportProtocols;
    return this;
  }

  public V1BlacklistSpec addDestinationTransportProtocolsItem(
      DestinationTransportProtocolsEnum destinationTransportProtocolsItem) {
    if (this.destinationTransportProtocols == null) {
      this.destinationTransportProtocols = new ArrayList<>();
    }
    this.destinationTransportProtocols.add(destinationTransportProtocolsItem);
    return this;
  }

  /**
   * TCP, UDP, or *
   *
   * @return destinationTransportProtocols
   */
  @NotNull
  @Schema(required = true, description = "TCP, UDP, or *")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_TRANSPORT_PROTOCOLS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<DestinationTransportProtocolsEnum> getDestinationTransportProtocols() {
    return destinationTransportProtocols;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_TRANSPORT_PROTOCOLS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDestinationTransportProtocols(
      List<DestinationTransportProtocolsEnum> destinationTransportProtocols) {
    this.destinationTransportProtocols = destinationTransportProtocols;
  }

  public V1BlacklistSpec destinationPortRanges(List<String> destinationPortRanges) {
    this.destinationPortRanges = destinationPortRanges;
    return this;
  }

  public V1BlacklistSpec addDestinationPortRangesItem(String destinationPortRangesItem) {
    if (this.destinationPortRanges == null) {
      this.destinationPortRanges = new ArrayList<>();
    }
    this.destinationPortRanges.add(destinationPortRangesItem);
    return this;
  }

  /**
   * Port ranges (e.g., 443, 0-65535, *)
   *
   * @return destinationPortRanges
   */
  @NotNull
  @Schema(required = true, description = "Port ranges (e.g., 443, 0-65535, *)")
  @JsonProperty(value = JSON_PROPERTY_DESTINATION_PORT_RANGES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getDestinationPortRanges() {
    return destinationPortRanges;
  }

  @JsonProperty(value = JSON_PROPERTY_DESTINATION_PORT_RANGES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDestinationPortRanges(List<String> destinationPortRanges) {
    this.destinationPortRanges = destinationPortRanges;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1BlacklistSpec v1BlacklistSpec = (V1BlacklistSpec) o;
    return Objects.equals(this.sourceComponent, v1BlacklistSpec.sourceComponent)
        && Objects.equals(this.destinationHostFqdns, v1BlacklistSpec.destinationHostFqdns)
        && Objects.equals(this.destinationHostCidrs, v1BlacklistSpec.destinationHostCidrs)
        && Objects.equals(this.sourceZones, v1BlacklistSpec.sourceZones)
        && Objects.equals(this.sourceMacrophases, v1BlacklistSpec.sourceMacrophases)
        && Objects.equals(this.sourceTargets, v1BlacklistSpec.sourceTargets)
        && Objects.equals(
            this.destinationApplicationProtocols, v1BlacklistSpec.destinationApplicationProtocols)
        && Objects.equals(
            this.destinationTransportProtocols, v1BlacklistSpec.destinationTransportProtocols)
        && Objects.equals(this.destinationPortRanges, v1BlacklistSpec.destinationPortRanges);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        sourceComponent,
        destinationHostFqdns,
        destinationHostCidrs,
        sourceZones,
        sourceMacrophases,
        sourceTargets,
        destinationApplicationProtocols,
        destinationTransportProtocols,
        destinationPortRanges);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1BlacklistSpec {\n");
    sb.append("    sourceComponent: ").append(toIndentedString(sourceComponent)).append("\n");
    sb.append("    destinationHostFqdns: ")
        .append(toIndentedString(destinationHostFqdns))
        .append("\n");
    sb.append("    destinationHostCidrs: ")
        .append(toIndentedString(destinationHostCidrs))
        .append("\n");
    sb.append("    sourceZones: ").append(toIndentedString(sourceZones)).append("\n");
    sb.append("    sourceMacrophases: ").append(toIndentedString(sourceMacrophases)).append("\n");
    sb.append("    sourceTargets: ").append(toIndentedString(sourceTargets)).append("\n");
    sb.append("    destinationApplicationProtocols: ")
        .append(toIndentedString(destinationApplicationProtocols))
        .append("\n");
    sb.append("    destinationTransportProtocols: ")
        .append(toIndentedString(destinationTransportProtocols))
        .append("\n");
    sb.append("    destinationPortRanges: ")
        .append(toIndentedString(destinationPortRanges))
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

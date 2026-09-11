package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Blocked port specification. */
@Schema(description = "Blocked port specification.")
@JsonPropertyOrder({V1BlockedPortSpec.JSON_PROPERTY_PORT, V1BlockedPortSpec.JSON_PROPERTY_PROTOCOL})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1BlockedPortSpec {
  public static final String JSON_PROPERTY_PORT = "port";
  private Integer port;

  /** The protocol e.g. TCP, UDP, Icmp. */
  public enum ProtocolEnum {
    TCP(String.valueOf("Tcp")),

    UDP(String.valueOf("Udp")),

    ICMP(String.valueOf("Icmp"));

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

  public V1BlockedPortSpec() {}

  public V1BlockedPortSpec port(Integer port) {
    this.port = port;
    return this;
  }

  /**
   * port number to block
   *
   * @return port
   */
  @NotNull
  @Schema(required = true, description = "port number to block")
  @JsonProperty(value = JSON_PROPERTY_PORT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getPort() {
    return port;
  }

  @JsonProperty(value = JSON_PROPERTY_PORT, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPort(Integer port) {
    this.port = port;
  }

  public V1BlockedPortSpec protocol(ProtocolEnum protocol) {
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

  /** Return true if this v1.BlockedPortSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1BlockedPortSpec v1BlockedPortSpec = (V1BlockedPortSpec) o;
    return Objects.equals(this.port, v1BlockedPortSpec.port)
        && Objects.equals(this.protocol, v1BlockedPortSpec.protocol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port, protocol);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1BlockedPortSpec {\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
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

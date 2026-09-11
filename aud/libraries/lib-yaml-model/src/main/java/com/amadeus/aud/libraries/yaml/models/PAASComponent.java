package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** PaaS Component */
@Schema(description = "PaaS Component")
@JsonPropertyOrder({
  PAASComponent.JSON_PROPERTY_NAME,
  PAASComponent.JSON_PROPERTY_NAMESPACE,
  PAASComponent.JSON_PROPERTY_IDENTIFIER,
  PAASComponent.JSON_PROPERTY_IN_NETWORK,
  PAASComponent.JSON_PROPERTY_OUT_NETWORK
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PAASComponent extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_NAMESPACE = "namespace";
  private String namespace;

  public static final String JSON_PROPERTY_IDENTIFIER = "identifier";
  @Nullable private Object identifier;

  public static final String JSON_PROPERTY_IN_NETWORK = "in_network";
  @Nullable private String inNetwork;

  public static final String JSON_PROPERTY_OUT_NETWORK = "out_network";
  @Nullable private String outNetwork;

  public PAASComponent() {}

  public PAASComponent name(String name) {
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

  public PAASComponent namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Get namespace
   *
   * @return namespace
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NAMESPACE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getNamespace() {
    return namespace;
  }

  @JsonProperty(value = JSON_PROPERTY_NAMESPACE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public PAASComponent identifier(@Nullable Object identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * Get identifier
   *
   * @return identifier
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_IDENTIFIER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Object getIdentifier() {
    return identifier;
  }

  @JsonProperty(value = JSON_PROPERTY_IDENTIFIER, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIdentifier(@Nullable Object identifier) {
    this.identifier = identifier;
  }

  public PAASComponent inNetwork(@Nullable String inNetwork) {
    this.inNetwork = inNetwork;
    return this;
  }

  /**
   * Get inNetwork
   *
   * @return inNetwork
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_IN_NETWORK, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getInNetwork() {
    return inNetwork;
  }

  @JsonProperty(value = JSON_PROPERTY_IN_NETWORK, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setInNetwork(@Nullable String inNetwork) {
    this.inNetwork = inNetwork;
  }

  public PAASComponent outNetwork(@Nullable String outNetwork) {
    this.outNetwork = outNetwork;
    return this;
  }

  /**
   * Get outNetwork
   *
   * @return outNetwork
   */
  @Schema(required = false, description = "")
  @JsonProperty(value = JSON_PROPERTY_OUT_NETWORK, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getOutNetwork() {
    return outNetwork;
  }

  @JsonProperty(value = JSON_PROPERTY_OUT_NETWORK, required = false)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOutNetwork(@Nullable String outNetwork) {
    this.outNetwork = outNetwork;
  }

  @Override
  public PAASComponent labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this PAASComponent object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PAASComponent paASComponent = (PAASComponent) o;
    return Objects.equals(this.name, paASComponent.name)
        && Objects.equals(this.namespace, paASComponent.namespace)
        && Objects.equals(this.identifier, paASComponent.identifier)
        && Objects.equals(this.inNetwork, paASComponent.inNetwork)
        && Objects.equals(this.outNetwork, paASComponent.outNetwork)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, namespace, identifier, inNetwork, outNetwork, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PAASComponent {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
    sb.append("    inNetwork: ").append(toIndentedString(inNetwork)).append("\n");
    sb.append("    outNetwork: ").append(toIndentedString(outNetwork)).append("\n");
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

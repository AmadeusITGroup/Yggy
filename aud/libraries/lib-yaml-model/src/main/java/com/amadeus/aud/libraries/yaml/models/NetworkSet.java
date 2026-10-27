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

/** NetworkSet */
@Schema(description = "NetworkSet")
@JsonPropertyOrder({NetworkSet.JSON_PROPERTY_NAME, NetworkSet.JSON_PROPERTY_ENDPOINTS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NetworkSet extends ObjectMetadata {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_ENDPOINTS = "endpoints";
  private List<@Valid Endpoint> endpoints;

  public NetworkSet() {}

  public NetworkSet name(String name) {
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

  public NetworkSet endpoints(List<@Valid Endpoint> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public NetworkSet addEndpointsItem(Endpoint endpointsItem) {
    if (this.endpoints == null) {
      this.endpoints = new ArrayList<>();
    }
    this.endpoints.add(endpointsItem);
    return this;
  }

  /**
   * Get endpoints
   *
   * @return endpoints
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_ENDPOINTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid Endpoint> getEndpoints() {
    return endpoints;
  }

  @JsonProperty(value = JSON_PROPERTY_ENDPOINTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setEndpoints(List<@Valid Endpoint> endpoints) {
    this.endpoints = endpoints;
  }

  @Override
  public NetworkSet labels(@Nullable Map<String, String> labels) {
    this.setLabels(labels);
    return this;
  }

  /** Return true if this NetworkSet object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetworkSet networkSet = (NetworkSet) o;
    return Objects.equals(this.name, networkSet.name)
        && Objects.equals(this.endpoints, networkSet.endpoints)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, endpoints, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NetworkSet {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
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

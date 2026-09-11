package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** TBD */
@Schema(description = "TBD")
@JsonPropertyOrder({ServiceGroupsResponse.JSON_PROPERTY_SERVICE_GROUPS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceGroupsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_SERVICE_GROUPS = "service_groups";
  private List<ServiceGroup> serviceGroups;

  public ServiceGroupsResponse() {}

  public ServiceGroupsResponse serviceGroups(List<ServiceGroup> serviceGroups) {
    this.serviceGroups = serviceGroups;
    return this;
  }

  public ServiceGroupsResponse addServiceGroupsItem(ServiceGroup serviceGroupsItem) {
    if (this.serviceGroups == null) {
      this.serviceGroups = new ArrayList<>();
    }
    this.serviceGroups.add(serviceGroupsItem);
    return this;
  }

  /**
   * List of service groups
   *
   * @return serviceGroups
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "List of service groups")
  @JsonProperty(value = JSON_PROPERTY_SERVICE_GROUPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<ServiceGroup> getServiceGroups() {
    return serviceGroups;
  }

  @JsonProperty(value = JSON_PROPERTY_SERVICE_GROUPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setServiceGroups(List<ServiceGroup> serviceGroups) {
    this.serviceGroups = serviceGroups;
  }

  @Override
  public ServiceGroupsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public ServiceGroupsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public ServiceGroupsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public ServiceGroupsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this ServiceGroupsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceGroupsResponse serviceGroupsResponse = (ServiceGroupsResponse) o;
    return Objects.equals(this.serviceGroups, serviceGroupsResponse.serviceGroups)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceGroups, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceGroupsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    serviceGroups: ").append(toIndentedString(serviceGroups)).append("\n");
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

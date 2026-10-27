package com.amadeus.aud.libraries.ipreport.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** IpReportAs specification */
@JsonPropertyOrder({
  IpReportAs.JSON_PROPERTY_COMPONENT_INSTANCE,
  IpReportAs.JSON_PROPERTY_CI_URL_HASH
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IpReportAs {
  public static final String JSON_PROPERTY_COMPONENT_INSTANCE = "component_instance";
  private String componentInstance;

  public static final String JSON_PROPERTY_CI_URL_HASH = "ci_url_hash";
  private String ciUrlHash;

  public IpReportAs() {}

  public IpReportAs componentInstance(String componentInstance) {
    this.componentInstance = componentInstance;
    return this;
  }

  /**
   * Get componentInstance
   *
   * @return componentInstance
   */
  @NotNull
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

  public IpReportAs ciUrlHash(String ciUrlHash) {
    this.ciUrlHash = ciUrlHash;
    return this;
  }

  /**
   * Get ciUrlHash
   *
   * @return ciUrlHash
   */
  @NotNull
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

  /** Return true if this IpReportAs object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IpReportAs ipReportAs = (IpReportAs) o;
    return Objects.equals(this.componentInstance, ipReportAs.componentInstance)
        && Objects.equals(this.ciUrlHash, ipReportAs.ciUrlHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(componentInstance, ciUrlHash);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IpReportAs {\n");
    sb.append("    componentInstance: ").append(toIndentedString(componentInstance)).append("\n");
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

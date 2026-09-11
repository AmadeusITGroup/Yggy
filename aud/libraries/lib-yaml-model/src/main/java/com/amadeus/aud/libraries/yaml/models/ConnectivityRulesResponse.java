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
@JsonPropertyOrder({ConnectivityRulesResponse.JSON_PROPERTY_CONNECTIVITY_RULES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ConnectivityRulesResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_CONNECTIVITY_RULES = "connectivity_rules";
  private List<ConnectivityRule> connectivityRules;

  public ConnectivityRulesResponse() {}

  public ConnectivityRulesResponse connectivityRules(List<ConnectivityRule> connectivityRules) {
    this.connectivityRules = connectivityRules;
    return this;
  }

  public ConnectivityRulesResponse addConnectivityRulesItem(
      ConnectivityRule connectivityRulesItem) {
    if (this.connectivityRules == null) {
      this.connectivityRules = new ArrayList<>();
    }
    this.connectivityRules.add(connectivityRulesItem);
    return this;
  }

  /**
   * List of Connectivity Rules
   *
   * @return connectivityRules
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "List of Connectivity Rules")
  @JsonProperty(value = JSON_PROPERTY_CONNECTIVITY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<ConnectivityRule> getConnectivityRules() {
    return connectivityRules;
  }

  @JsonProperty(value = JSON_PROPERTY_CONNECTIVITY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setConnectivityRules(List<ConnectivityRule> connectivityRules) {
    this.connectivityRules = connectivityRules;
  }

  @Override
  public ConnectivityRulesResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public ConnectivityRulesResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public ConnectivityRulesResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public ConnectivityRulesResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this ConnectivityRulesResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConnectivityRulesResponse connectivityRulesResponse = (ConnectivityRulesResponse) o;
    return Objects.equals(this.connectivityRules, connectivityRulesResponse.connectivityRules)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connectivityRules, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectivityRulesResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    connectivityRules: ").append(toIndentedString(connectivityRules)).append("\n");
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

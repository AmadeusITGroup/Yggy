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

/** The number of different elements present in the AUD graph */
@Schema(description = "The number of different elements present in the AUD graph")
@JsonPropertyOrder({Statistics.JSON_PROPERTY_GLOBAL, Statistics.JSON_PROPERTY_SCOPES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Statistics extends ApiMetadata {
  public static final String JSON_PROPERTY_GLOBAL = "global";
  private StatisticsObject global;

  public static final String JSON_PROPERTY_SCOPES = "scopes";
  private List<@Valid StatisticsScope> scopes;

  public Statistics() {}

  public Statistics global(StatisticsObject global) {
    this.global = global;
    return this;
  }

  /**
   * Get global
   *
   * @return global
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_GLOBAL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public StatisticsObject getGlobal() {
    return global;
  }

  @JsonProperty(value = JSON_PROPERTY_GLOBAL, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setGlobal(StatisticsObject global) {
    this.global = global;
  }

  public Statistics scopes(List<@Valid StatisticsScope> scopes) {
    this.scopes = scopes;
    return this;
  }

  public Statistics addScopesItem(StatisticsScope scopesItem) {
    if (this.scopes == null) {
      this.scopes = new ArrayList<>();
    }
    this.scopes.add(scopesItem);
    return this;
  }

  /**
   * Get scopes
   *
   * @return scopes
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SCOPES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid StatisticsScope> getScopes() {
    return scopes;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScopes(List<@Valid StatisticsScope> scopes) {
    this.scopes = scopes;
  }

  @Override
  public Statistics url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public Statistics urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public Statistics version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public Statistics apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this Statistics object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Statistics statistics = (Statistics) o;
    return Objects.equals(this.global, statistics.global)
        && Objects.equals(this.scopes, statistics.scopes)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(global, scopes, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Statistics {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    global: ").append(toIndentedString(global)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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

package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** StatisticsScope */
@JsonPropertyOrder({StatisticsScope.JSON_PROPERTY_SCOPE})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StatisticsScope extends StatisticsObject {
  public static final String JSON_PROPERTY_SCOPE = "scope";
  private String scope;

  public StatisticsScope() {}

  public StatisticsScope scope(String scope) {
    this.scope = scope;
    return this;
  }

  /**
   * Get scope
   *
   * @return scope
   */
  @NotNull
  @Schema(
      examples = {"Network"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getScope() {
    return scope;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScope(String scope) {
    this.scope = scope;
  }

  @Override
  public StatisticsScope dependencies(Integer dependencies) {
    this.setDependencies(dependencies);
    return this;
  }

  @Override
  public StatisticsScope dependencyInstances(Integer dependencyInstances) {
    this.setDependencyInstances(dependencyInstances);
    return this;
  }

  @Override
  public StatisticsScope components(Integer components) {
    this.setComponents(components);
    return this;
  }

  @Override
  public StatisticsScope componentInstances(Integer componentInstances) {
    this.setComponentInstances(componentInstances);
    return this;
  }

  @Override
  public StatisticsScope services(Integer services) {
    this.setServices(services);
    return this;
  }

  @Override
  public StatisticsScope links(Integer links) {
    this.setLinks(links);
    return this;
  }

  @Override
  public StatisticsScope linksApproved(@Nullable Integer linksApproved) {
    this.setLinksApproved(linksApproved);
    return this;
  }

  @Override
  public StatisticsScope nodes(Integer nodes) {
    this.setNodes(nodes);
    return this;
  }

  /** Return true if this StatisticsScope object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StatisticsScope statisticsScope = (StatisticsScope) o;
    return Objects.equals(this.scope, statisticsScope.scope) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StatisticsScope {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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

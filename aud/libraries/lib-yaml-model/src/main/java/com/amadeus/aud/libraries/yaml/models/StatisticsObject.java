package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** StatisticsObject */
@JsonPropertyOrder({
  StatisticsObject.JSON_PROPERTY_DEPENDENCIES,
  StatisticsObject.JSON_PROPERTY_DEPENDENCY_INSTANCES,
  StatisticsObject.JSON_PROPERTY_COMPONENTS,
  StatisticsObject.JSON_PROPERTY_COMPONENT_INSTANCES,
  StatisticsObject.JSON_PROPERTY_SERVICES,
  StatisticsObject.JSON_PROPERTY_LINKS,
  StatisticsObject.JSON_PROPERTY_LINKS_APPROVED,
  StatisticsObject.JSON_PROPERTY_NODES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class StatisticsObject {
  public static final String JSON_PROPERTY_DEPENDENCIES = "dependencies";
  private Integer dependencies;

  public static final String JSON_PROPERTY_DEPENDENCY_INSTANCES = "dependency_instances";
  private Integer dependencyInstances;

  public static final String JSON_PROPERTY_COMPONENTS = "components";
  private Integer components;

  public static final String JSON_PROPERTY_COMPONENT_INSTANCES = "component_instances";
  private Integer componentInstances;

  public static final String JSON_PROPERTY_SERVICES = "services";
  private Integer services;

  public static final String JSON_PROPERTY_LINKS = "links";
  private Integer links;

  public static final String JSON_PROPERTY_LINKS_APPROVED = "links_approved";
  @Nullable private Integer linksApproved;

  public static final String JSON_PROPERTY_NODES = "nodes";
  private Integer nodes;

  public StatisticsObject() {}

  public StatisticsObject dependencies(Integer dependencies) {
    this.dependencies = dependencies;
    return this;
  }

  /**
   * Get dependencies
   *
   * @return dependencies
   */
  @NotNull
  @Schema(
      examples = {"34"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_DEPENDENCIES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getDependencies() {
    return dependencies;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPENDENCIES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDependencies(Integer dependencies) {
    this.dependencies = dependencies;
  }

  public StatisticsObject dependencyInstances(Integer dependencyInstances) {
    this.dependencyInstances = dependencyInstances;
    return this;
  }

  /**
   * Get dependencyInstances
   *
   * @return dependencyInstances
   */
  @NotNull
  @Schema(
      examples = {"42"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getDependencyInstances() {
    return dependencyInstances;
  }

  @JsonProperty(value = JSON_PROPERTY_DEPENDENCY_INSTANCES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDependencyInstances(Integer dependencyInstances) {
    this.dependencyInstances = dependencyInstances;
  }

  public StatisticsObject components(Integer components) {
    this.components = components;
    return this;
  }

  /**
   * Get components
   *
   * @return components
   */
  @NotNull
  @Schema(
      examples = {"189"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_COMPONENTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getComponents() {
    return components;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponents(Integer components) {
    this.components = components;
  }

  public StatisticsObject componentInstances(Integer componentInstances) {
    this.componentInstances = componentInstances;
    return this;
  }

  /**
   * Get componentInstances
   *
   * @return componentInstances
   */
  @NotNull
  @Schema(
      examples = {"0"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getComponentInstances() {
    return componentInstances;
  }

  @JsonProperty(value = JSON_PROPERTY_COMPONENT_INSTANCES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setComponentInstances(Integer componentInstances) {
    this.componentInstances = componentInstances;
  }

  public StatisticsObject services(Integer services) {
    this.services = services;
    return this;
  }

  /**
   * Get services
   *
   * @return services
   */
  @NotNull
  @Schema(
      examples = {"1"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_SERVICES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getServices() {
    return services;
  }

  @JsonProperty(value = JSON_PROPERTY_SERVICES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setServices(Integer services) {
    this.services = services;
  }

  public StatisticsObject links(Integer links) {
    this.links = links;
    return this;
  }

  /**
   * Get links
   *
   * @return links
   */
  @NotNull
  @Schema(
      examples = {"102"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_LINKS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getLinks() {
    return links;
  }

  @JsonProperty(value = JSON_PROPERTY_LINKS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setLinks(Integer links) {
    this.links = links;
  }

  public StatisticsObject linksApproved(@Nullable Integer linksApproved) {
    this.linksApproved = linksApproved;
    return this;
  }

  /**
   * Get linksApproved
   *
   * @return linksApproved
   */
  @Schema(
      examples = {"12"},
      description = "")
  @JsonProperty(value = JSON_PROPERTY_LINKS_APPROVED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Integer getLinksApproved() {
    return linksApproved;
  }

  @JsonProperty(value = JSON_PROPERTY_LINKS_APPROVED)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setLinksApproved(@Nullable Integer linksApproved) {
    this.linksApproved = linksApproved;
  }

  public StatisticsObject nodes(Integer nodes) {
    this.nodes = nodes;
    return this;
  }

  /**
   * Get nodes
   *
   * @return nodes
   */
  @NotNull
  @Schema(
      examples = {"77"},
      required = true,
      description = "")
  @JsonProperty(value = JSON_PROPERTY_NODES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getNodes() {
    return nodes;
  }

  @JsonProperty(value = JSON_PROPERTY_NODES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNodes(Integer nodes) {
    this.nodes = nodes;
  }

  /** Return true if this StatisticsObject object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StatisticsObject statisticsObject = (StatisticsObject) o;
    return Objects.equals(this.dependencies, statisticsObject.dependencies)
        && Objects.equals(this.dependencyInstances, statisticsObject.dependencyInstances)
        && Objects.equals(this.components, statisticsObject.components)
        && Objects.equals(this.componentInstances, statisticsObject.componentInstances)
        && Objects.equals(this.services, statisticsObject.services)
        && Objects.equals(this.links, statisticsObject.links)
        && Objects.equals(this.linksApproved, statisticsObject.linksApproved)
        && Objects.equals(this.nodes, statisticsObject.nodes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        dependencies,
        dependencyInstances,
        components,
        componentInstances,
        services,
        links,
        linksApproved,
        nodes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StatisticsObject {\n");
    sb.append("    dependencies: ").append(toIndentedString(dependencies)).append("\n");
    sb.append("    dependencyInstances: ")
        .append(toIndentedString(dependencyInstances))
        .append("\n");
    sb.append("    components: ").append(toIndentedString(components)).append("\n");
    sb.append("    componentInstances: ").append(toIndentedString(componentInstances)).append("\n");
    sb.append("    services: ").append(toIndentedString(services)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    linksApproved: ").append(toIndentedString(linksApproved)).append("\n");
    sb.append("    nodes: ").append(toIndentedString(nodes)).append("\n");
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

package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Api providing all routing */
@Schema(description = "Api providing all routing")
@JsonPropertyOrder({Routing.JSON_PROPERTY_ROUTES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Routing extends ApiMetadata {
  public static final String JSON_PROPERTY_ROUTES = "routes";
  private List<Object> routes;

  public Routing() {}

  public Routing routes(List<Object> routes) {
    this.routes = routes;
    return this;
  }

  public Routing addRoutesItem(Object routesItem) {
    if (this.routes == null) {
      this.routes = new ArrayList<>();
    }
    this.routes.add(routesItem);
    return this;
  }

  /**
   * Get routes
   *
   * @return routes
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_ROUTES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<Object> getRoutes() {
    return routes;
  }

  @JsonProperty(value = JSON_PROPERTY_ROUTES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRoutes(List<Object> routes) {
    this.routes = routes;
  }

  @Override
  public Routing url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public Routing urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public Routing version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public Routing apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this Routing object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Routing routing = (Routing) o;
    return Objects.equals(this.routes, routing.routes) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(routes, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Routing {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    routes: ").append(toIndentedString(routes)).append("\n");
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

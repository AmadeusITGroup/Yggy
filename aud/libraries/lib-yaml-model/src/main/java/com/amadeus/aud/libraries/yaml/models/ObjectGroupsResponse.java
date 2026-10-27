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
@JsonPropertyOrder({ObjectGroupsResponse.JSON_PROPERTY_OBJECT_GROUPS})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ObjectGroupsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_OBJECT_GROUPS = "object_groups";
  private List<ObjectGroup> objectGroups;

  public ObjectGroupsResponse() {}

  public ObjectGroupsResponse objectGroups(List<ObjectGroup> objectGroups) {
    this.objectGroups = objectGroups;
    return this;
  }

  public ObjectGroupsResponse addObjectGroupsItem(ObjectGroup objectGroupsItem) {
    if (this.objectGroups == null) {
      this.objectGroups = new ArrayList<>();
    }
    this.objectGroups.add(objectGroupsItem);
    return this;
  }

  /**
   * List of object groups
   *
   * @return objectGroups
   */
  @NotNull
  @Schema(required = true, description = "List of object groups")
  @JsonProperty(value = JSON_PROPERTY_OBJECT_GROUPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid ObjectGroup> getObjectGroups() {
    return objectGroups;
  }

  @JsonProperty(value = JSON_PROPERTY_OBJECT_GROUPS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setObjectGroups(List<ObjectGroup> objectGroups) {
    this.objectGroups = objectGroups;
  }

  @Override
  public ObjectGroupsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public ObjectGroupsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public ObjectGroupsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public ObjectGroupsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this ObjectGroupsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjectGroupsResponse objectGroupsResponse = (ObjectGroupsResponse) o;
    return Objects.equals(this.objectGroups, objectGroupsResponse.objectGroups) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectGroups, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ObjectGroupsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    objectGroups: ").append(toIndentedString(objectGroups)).append("\n");
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

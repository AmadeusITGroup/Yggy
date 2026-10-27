package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ObjectGroupExtract */
@JsonPropertyOrder({
  ObjectGroupExtract.JSON_PROPERTY_NAME,
  ObjectGroupExtract.JSON_PROPERTY_UID,
  ObjectGroupExtract.JSON_PROPERTY_ZONE_TYPE,
  ObjectGroupExtract.JSON_PROPERTY_TYPE,
  ObjectGroupExtract.JSON_PROPERTY_ENDPOINTS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ObjectGroupExtract {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_UID = "uid";
  private String uid;

  public static final String JSON_PROPERTY_ZONE_TYPE = "zoneType";
  private String zoneType;

  /** Gets or Sets type */
  public enum TypeEnum {
    URL_CATEGORY(String.valueOf("UrlCategory")),

    ADDRESS_GROUP(String.valueOf("AddressGroup"));

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
      for (TypeEnum b : TypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_TYPE = "type";
  private TypeEnum type;

  public static final String JSON_PROPERTY_ENDPOINTS = "endpoints";
  private List<@Valid Endpoint> endpoints;

  public ObjectGroupExtract() {}

  public ObjectGroupExtract name(String name) {
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

  public ObjectGroupExtract uid(String uid) {
    this.uid = uid;
    return this;
  }

  /**
   * Get uid
   *
   * @return uid
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_UID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getUid() {
    return uid;
  }

  @JsonProperty(value = JSON_PROPERTY_UID, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setUid(String uid) {
    this.uid = uid;
  }

  public ObjectGroupExtract zoneType(String zoneType) {
    this.zoneType = zoneType;
    return this;
  }

  /**
   * Get zoneType
   *
   * @return zoneType
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getZoneType() {
    return zoneType;
  }

  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setZoneType(String zoneType) {
    this.zoneType = zoneType;
  }

  public ObjectGroupExtract type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   *
   * @return type
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public TypeEnum getType() {
    return type;
  }

  @JsonProperty(value = JSON_PROPERTY_TYPE, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setType(TypeEnum type) {
    this.type = type;
  }

  public ObjectGroupExtract endpoints(List<@Valid Endpoint> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public ObjectGroupExtract addEndpointsItem(Endpoint endpointsItem) {
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

  /** Return true if this ObjectGroupExtract object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjectGroupExtract objectGroupExtract = (ObjectGroupExtract) o;
    return Objects.equals(this.name, objectGroupExtract.name)
        && Objects.equals(this.uid, objectGroupExtract.uid)
        && Objects.equals(this.zoneType, objectGroupExtract.zoneType)
        && Objects.equals(this.type, objectGroupExtract.type)
        && Objects.equals(this.endpoints, objectGroupExtract.endpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, uid, zoneType, type, endpoints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ObjectGroupExtract {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    zoneType: ").append(toIndentedString(zoneType)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

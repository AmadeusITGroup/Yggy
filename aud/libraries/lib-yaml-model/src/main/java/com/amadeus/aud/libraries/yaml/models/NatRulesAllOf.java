package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** NatRulesAllOf */
@JsonPropertyOrder({
  NatRulesAllOf.JSON_PROPERTY_UID,
  NatRulesAllOf.JSON_PROPERTY_NAME,
  NatRulesAllOf.JSON_PROPERTY_DESCRIPTION,
  NatRulesAllOf.JSON_PROPERTY_MACROPHASE,
  NatRulesAllOf.JSON_PROPERTY_ZONE_TYPE,
  NatRulesAllOf.JSON_PROPERTY_CONTINENT,
  NatRulesAllOf.JSON_PROPERTY_PROTOCOL,
  NatRulesAllOf.JSON_PROPERTY_ORIGINAL_PACKET,
  NatRulesAllOf.JSON_PROPERTY_TRANSLATED_PACKET
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NatRulesAllOf {
  public static final String JSON_PROPERTY_UID = "uid";
  @Nullable private String uid;

  public static final String JSON_PROPERTY_NAME = "name";
  @Nullable private String name;

  public static final String JSON_PROPERTY_DESCRIPTION = "description";
  @Nullable private String description;

  public static final String JSON_PROPERTY_MACROPHASE = "macrophase";
  @Nullable private String macrophase;

  public static final String JSON_PROPERTY_ZONE_TYPE = "zone_type";
  @Nullable private String zoneType;

  public static final String JSON_PROPERTY_CONTINENT = "continent";
  @Nullable private String continent;

  public static final String JSON_PROPERTY_PROTOCOL = "protocol";
  @Nullable private String protocol;

  public static final String JSON_PROPERTY_ORIGINAL_PACKET = "original_packet";
  @Nullable private Object originalPacket;

  public static final String JSON_PROPERTY_TRANSLATED_PACKET = "translated_packet";
  @Nullable private Object translatedPacket;

  public NatRulesAllOf() {}

  public NatRulesAllOf uid(@Nullable String uid) {
    this.uid = uid;
    return this;
  }

  /**
   * Get uid
   *
   * @return uid
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_UID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUid() {
    return uid;
  }

  @JsonProperty(value = JSON_PROPERTY_UID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUid(@Nullable String uid) {
    this.uid = uid;
  }

  public NatRulesAllOf name(@Nullable String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   *
   * @return name
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getName() {
    return name;
  }

  @JsonProperty(value = JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setName(@Nullable String name) {
    this.name = name;
  }

  public NatRulesAllOf description(@Nullable String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   *
   * @return description
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_DESCRIPTION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getDescription() {
    return description;
  }

  @JsonProperty(value = JSON_PROPERTY_DESCRIPTION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  public NatRulesAllOf macrophase(@Nullable String macrophase) {
    this.macrophase = macrophase;
    return this;
  }

  /**
   * Get macrophase
   *
   * @return macrophase
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_MACROPHASE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getMacrophase() {
    return macrophase;
  }

  @JsonProperty(value = JSON_PROPERTY_MACROPHASE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setMacrophase(@Nullable String macrophase) {
    this.macrophase = macrophase;
  }

  public NatRulesAllOf zoneType(@Nullable String zoneType) {
    this.zoneType = zoneType;
    return this;
  }

  /**
   * Get zoneType
   *
   * @return zoneType
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getZoneType() {
    return zoneType;
  }

  @JsonProperty(value = JSON_PROPERTY_ZONE_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setZoneType(@Nullable String zoneType) {
    this.zoneType = zoneType;
  }

  public NatRulesAllOf continent(@Nullable String continent) {
    this.continent = continent;
    return this;
  }

  /**
   * Get continent
   *
   * @return continent
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_CONTINENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getContinent() {
    return continent;
  }

  @JsonProperty(value = JSON_PROPERTY_CONTINENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setContinent(@Nullable String continent) {
    this.continent = continent;
  }

  public NatRulesAllOf protocol(@Nullable String protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * Get protocol
   *
   * @return protocol
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_PROTOCOL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getProtocol() {
    return protocol;
  }

  @JsonProperty(value = JSON_PROPERTY_PROTOCOL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProtocol(@Nullable String protocol) {
    this.protocol = protocol;
  }

  public NatRulesAllOf originalPacket(@Nullable Object originalPacket) {
    this.originalPacket = originalPacket;
    return this;
  }

  /**
   * Get originalPacket
   *
   * @return originalPacket
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_ORIGINAL_PACKET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Object getOriginalPacket() {
    return originalPacket;
  }

  @JsonProperty(value = JSON_PROPERTY_ORIGINAL_PACKET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setOriginalPacket(@Nullable Object originalPacket) {
    this.originalPacket = originalPacket;
  }

  public NatRulesAllOf translatedPacket(@Nullable Object translatedPacket) {
    this.translatedPacket = translatedPacket;
    return this;
  }

  /**
   * Get translatedPacket
   *
   * @return translatedPacket
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_TRANSLATED_PACKET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable Object getTranslatedPacket() {
    return translatedPacket;
  }

  @JsonProperty(value = JSON_PROPERTY_TRANSLATED_PACKET)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setTranslatedPacket(@Nullable Object translatedPacket) {
    this.translatedPacket = translatedPacket;
  }

  /** Return true if this NatRules_allOf object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NatRulesAllOf natRulesAllOf = (NatRulesAllOf) o;
    return Objects.equals(this.uid, natRulesAllOf.uid)
        && Objects.equals(this.name, natRulesAllOf.name)
        && Objects.equals(this.description, natRulesAllOf.description)
        && Objects.equals(this.macrophase, natRulesAllOf.macrophase)
        && Objects.equals(this.zoneType, natRulesAllOf.zoneType)
        && Objects.equals(this.continent, natRulesAllOf.continent)
        && Objects.equals(this.protocol, natRulesAllOf.protocol)
        && Objects.equals(this.originalPacket, natRulesAllOf.originalPacket)
        && Objects.equals(this.translatedPacket, natRulesAllOf.translatedPacket);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        uid,
        name,
        description,
        macrophase,
        zoneType,
        continent,
        protocol,
        originalPacket,
        translatedPacket);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NatRulesAllOf {\n");
    sb.append("    uid: ").append(toIndentedString(uid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    macrophase: ").append(toIndentedString(macrophase)).append("\n");
    sb.append("    zoneType: ").append(toIndentedString(zoneType)).append("\n");
    sb.append("    continent: ").append(toIndentedString(continent)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    originalPacket: ").append(toIndentedString(originalPacket)).append("\n");
    sb.append("    translatedPacket: ").append(toIndentedString(translatedPacket)).append("\n");
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

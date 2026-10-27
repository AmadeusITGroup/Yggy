package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1AutomationTypeSpec */
@JsonPropertyOrder({
  V1AutomationTypeSpec.JSON_PROPERTY_GRANULARITY,
  V1AutomationTypeSpec.JSON_PROPERTY_URL,
  V1AutomationTypeSpec.JSON_PROPERTY_CONTENT_TYPE,
  V1AutomationTypeSpec.JSON_PROPERTY_PAYLOAD_TEMPLATE,
  V1AutomationTypeSpec.JSON_PROPERTY_PREDECESSORS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1AutomationTypeSpec {
  public static final String JSON_PROPERTY_GRANULARITY = "granularity";
  @Nullable private String granularity;

  public static final String JSON_PROPERTY_URL = "url";
  @Nullable private String url;

  public static final String JSON_PROPERTY_CONTENT_TYPE = "content_type";
  @Nullable private String contentType;

  public static final String JSON_PROPERTY_PAYLOAD_TEMPLATE = "payload_template";
  @Nullable private String payloadTemplate;

  public static final String JSON_PROPERTY_PREDECESSORS = "predecessors";
  @Nullable private List<String> predecessors;

  public V1AutomationTypeSpec() {}

  public V1AutomationTypeSpec granularity(@Nullable String granularity) {
    this.granularity = granularity;
    return this;
  }

  /**
   * granularity of the automation (e.g. target, landingzone, ...)
   *
   * @return granularity
   */
  @Schema(description = "granularity of the automation (e.g. target, landingzone, ...)")
  @JsonProperty(value = JSON_PROPERTY_GRANULARITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getGranularity() {
    return granularity;
  }

  @JsonProperty(value = JSON_PROPERTY_GRANULARITY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setGranularity(@Nullable String granularity) {
    this.granularity = granularity;
  }

  public V1AutomationTypeSpec url(@Nullable String url) {
    this.url = url;
    return this;
  }

  /**
   * The url allowing to trigger this automation
   *
   * @return url
   */
  @Schema(description = "The url allowing to trigger this automation")
  @JsonProperty(value = JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getUrl() {
    return url;
  }

  @JsonProperty(value = JSON_PROPERTY_URL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUrl(@Nullable String url) {
    this.url = url;
  }

  public V1AutomationTypeSpec contentType(@Nullable String contentType) {
    this.contentType = contentType;
    return this;
  }

  /**
   * The content type of the payload
   *
   * @return contentType
   */
  @Schema(description = "The content type of the payload")
  @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getContentType() {
    return contentType;
  }

  @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setContentType(@Nullable String contentType) {
    this.contentType = contentType;
  }

  public V1AutomationTypeSpec payloadTemplate(@Nullable String payloadTemplate) {
    this.payloadTemplate = payloadTemplate;
    return this;
  }

  /**
   * The payload template that will be sent to the automation url (use Jinja2 template)
   *
   * @return payloadTemplate
   */
  @Schema(
      description =
          "The payload template that will be sent to the automation url (use Jinja2 template)")
  @JsonProperty(value = JSON_PROPERTY_PAYLOAD_TEMPLATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getPayloadTemplate() {
    return payloadTemplate;
  }

  @JsonProperty(value = JSON_PROPERTY_PAYLOAD_TEMPLATE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPayloadTemplate(@Nullable String payloadTemplate) {
    this.payloadTemplate = payloadTemplate;
  }

  public V1AutomationTypeSpec predecessors(@Nullable List<String> predecessors) {
    this.predecessors = predecessors;
    return this;
  }

  public V1AutomationTypeSpec addPredecessorsItem(String predecessorsItem) {
    if (this.predecessors == null) {
      this.predecessors = new ArrayList<>();
    }
    this.predecessors.add(predecessorsItem);
    return this;
  }

  /**
   * List of preceding automation types
   *
   * @return predecessors
   */
  @Schema(description = "List of preceding automation types")
  @JsonProperty(value = JSON_PROPERTY_PREDECESSORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getPredecessors() {
    return predecessors;
  }

  @JsonProperty(value = JSON_PROPERTY_PREDECESSORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPredecessors(@Nullable List<String> predecessors) {
    this.predecessors = predecessors;
  }

  /** Return true if this v1.AutomationTypeSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1AutomationTypeSpec v1AutomationTypeSpec = (V1AutomationTypeSpec) o;
    return Objects.equals(this.granularity, v1AutomationTypeSpec.granularity)
        && Objects.equals(this.url, v1AutomationTypeSpec.url)
        && Objects.equals(this.contentType, v1AutomationTypeSpec.contentType)
        && Objects.equals(this.payloadTemplate, v1AutomationTypeSpec.payloadTemplate)
        && Objects.equals(this.predecessors, v1AutomationTypeSpec.predecessors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(granularity, url, contentType, payloadTemplate, predecessors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1AutomationTypeSpec {\n");
    sb.append("    granularity: ").append(toIndentedString(granularity)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    contentType: ").append(toIndentedString(contentType)).append("\n");
    sb.append("    payloadTemplate: ").append(toIndentedString(payloadTemplate)).append("\n");
    sb.append("    predecessors: ").append(toIndentedString(predecessors)).append("\n");
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

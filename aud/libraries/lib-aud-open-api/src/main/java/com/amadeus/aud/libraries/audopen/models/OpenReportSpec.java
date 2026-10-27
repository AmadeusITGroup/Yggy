package com.amadeus.aud.libraries.audopen.models;

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

/** OpenReport specification */
@Schema(description = "OpenReport specification")
@JsonPropertyOrder({
  OpenReportSpec.JSON_PROPERTY_RULES,
  OpenReportSpec.JSON_PROPERTY_SRC_IS_IP,
  OpenReportSpec.JSON_PROPERTY_DST_IS_IP,
  OpenReportSpec.JSON_PROPERTY_ERRORS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OpenReportSpec {
  public static final String JSON_PROPERTY_RULES = "rules";
  private List<SecurityRule> rules;

  public static final String JSON_PROPERTY_SRC_IS_IP = "src_is_ip";
  private Boolean srcIsIp;

  public static final String JSON_PROPERTY_DST_IS_IP = "dst_is_ip";
  private Boolean dstIsIp;

  public static final String JSON_PROPERTY_ERRORS = "errors";
  @Nullable private List<String> errors;

  public OpenReportSpec() {}

  public OpenReportSpec rules(List<SecurityRule> rules) {
    this.rules = rules;
    return this;
  }

  public OpenReportSpec addRulesItem(SecurityRule rulesItem) {
    if (this.rules == null) {
      this.rules = new ArrayList<>();
    }
    this.rules.add(rulesItem);
    return this;
  }

  /**
   * Get rules
   *
   * @return rules
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid SecurityRule> getRules() {
    return rules;
  }

  @JsonProperty(value = JSON_PROPERTY_RULES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setRules(List<SecurityRule> rules) {
    this.rules = rules;
  }

  public OpenReportSpec srcIsIp(Boolean srcIsIp) {
    this.srcIsIp = srcIsIp;
    return this;
  }

  /**
   * Get srcIsIp
   *
   * @return srcIsIp
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_SRC_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getSrcIsIp() {
    return srcIsIp;
  }

  @JsonProperty(value = JSON_PROPERTY_SRC_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSrcIsIp(Boolean srcIsIp) {
    this.srcIsIp = srcIsIp;
  }

  public OpenReportSpec dstIsIp(Boolean dstIsIp) {
    this.dstIsIp = dstIsIp;
    return this;
  }

  /**
   * Get dstIsIp
   *
   * @return dstIsIp
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DST_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getDstIsIp() {
    return dstIsIp;
  }

  @JsonProperty(value = JSON_PROPERTY_DST_IS_IP, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDstIsIp(Boolean dstIsIp) {
    this.dstIsIp = dstIsIp;
  }

  public OpenReportSpec errors(@Nullable List<String> errors) {
    this.errors = errors;
    return this;
  }

  public OpenReportSpec addErrorsItem(String errorsItem) {
    if (this.errors == null) {
      this.errors = new ArrayList<>();
    }
    this.errors.add(errorsItem);
    return this;
  }

  /**
   * Get errors
   *
   * @return errors
   */
  @Schema(description = "")
  @JsonProperty(value = JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getErrors() {
    return errors;
  }

  @JsonProperty(value = JSON_PROPERTY_ERRORS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setErrors(@Nullable List<String> errors) {
    this.errors = errors;
  }

  /** Return true if this OpenReportSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpenReportSpec openReportSpec = (OpenReportSpec) o;
    return Objects.equals(this.rules, openReportSpec.rules)
        && Objects.equals(this.srcIsIp, openReportSpec.srcIsIp)
        && Objects.equals(this.dstIsIp, openReportSpec.dstIsIp)
        && Objects.equals(this.errors, openReportSpec.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rules, srcIsIp, dstIsIp, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpenReportSpec {\n");
    sb.append("    rules: ").append(toIndentedString(rules)).append("\n");
    sb.append("    srcIsIp: ").append(toIndentedString(srcIsIp)).append("\n");
    sb.append("    dstIsIp: ").append(toIndentedString(dstIsIp)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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

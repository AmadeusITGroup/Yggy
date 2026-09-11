package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** ScopeDiffs */
@JsonPropertyOrder({ScopeDiffs.JSON_PROPERTY_DIFF})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScopeDiffs {
  public static final String JSON_PROPERTY_DIFF = "diff";
  private List<@Valid ScopeDiff> diff;

  public ScopeDiffs() {}

  public ScopeDiffs diff(List<@Valid ScopeDiff> diff) {
    this.diff = diff;
    return this;
  }

  public ScopeDiffs addDiffItem(ScopeDiff diffItem) {
    if (this.diff == null) {
      this.diff = new ArrayList<>();
    }
    this.diff.add(diffItem);
    return this;
  }

  /**
   * Get diff
   *
   * @return diff
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_DIFF, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid ScopeDiff> getDiff() {
    return diff;
  }

  @JsonProperty(value = JSON_PROPERTY_DIFF, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setDiff(List<@Valid ScopeDiff> diff) {
    this.diff = diff;
  }

  /** Return true if this ScopeDiffs object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScopeDiffs scopeDiffs = (ScopeDiffs) o;
    return Objects.equals(this.diff, scopeDiffs.diff);
  }

  @Override
  public int hashCode() {
    return Objects.hash(diff);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeDiffs {\n");
    sb.append("    diff: ").append(toIndentedString(diff)).append("\n");
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

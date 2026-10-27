package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** V1ScopeReleaseNoteSpec */
@JsonPropertyOrder({
  V1ScopeReleaseNoteSpec.JSON_PROPERTY_VERSION_DATETIME,
  V1ScopeReleaseNoteSpec.JSON_PROPERTY_SCOPE_VERSION,
  V1ScopeReleaseNoteSpec.JSON_PROPERTY_COMMITS,
  V1ScopeReleaseNoteSpec.JSON_PROPERTY_PRS,
  V1ScopeReleaseNoteSpec.JSON_PROPERTY_IMPACTED_DEVICES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class V1ScopeReleaseNoteSpec {
  public static final String JSON_PROPERTY_VERSION_DATETIME = "version_datetime";
  @Nullable private String versionDatetime;

  public static final String JSON_PROPERTY_SCOPE_VERSION = "scope_version";
  @Nullable private String scopeVersion;

  public static final String JSON_PROPERTY_COMMITS = "commits";
  @Nullable private List<@Valid V1Commit> commits;

  public static final String JSON_PROPERTY_PRS = "prs";
  @Nullable private List<String> prs;

  public static final String JSON_PROPERTY_IMPACTED_DEVICES = "impacted_devices";
  @Nullable private List<@Valid V1Device> impactedDevices;

  public V1ScopeReleaseNoteSpec() {}

  public V1ScopeReleaseNoteSpec versionDatetime(@Nullable String versionDatetime) {
    this.versionDatetime = versionDatetime;
    return this;
  }

  /**
   * Timestamp of scope version creation
   *
   * @return versionDatetime
   */
  @Schema(
      examples = {"2025-02-18T15:31:47.430Z"},
      description = "Timestamp of scope version creation")
  @JsonProperty(value = JSON_PROPERTY_VERSION_DATETIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getVersionDatetime() {
    return versionDatetime;
  }

  @JsonProperty(value = JSON_PROPERTY_VERSION_DATETIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setVersionDatetime(@Nullable String versionDatetime) {
    this.versionDatetime = versionDatetime;
  }

  public V1ScopeReleaseNoteSpec scopeVersion(@Nullable String scopeVersion) {
    this.scopeVersion = scopeVersion;
    return this;
  }

  /**
   * Version of the scope
   *
   * @return scopeVersion
   */
  @Schema(
      examples = {"1.0.116"},
      description = "Version of the scope")
  @JsonProperty(value = JSON_PROPERTY_SCOPE_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable String getScopeVersion() {
    return scopeVersion;
  }

  @JsonProperty(value = JSON_PROPERTY_SCOPE_VERSION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setScopeVersion(@Nullable String scopeVersion) {
    this.scopeVersion = scopeVersion;
  }

  public V1ScopeReleaseNoteSpec commits(@Nullable List<@Valid V1Commit> commits) {
    this.commits = commits;
    return this;
  }

  public V1ScopeReleaseNoteSpec addCommitsItem(V1Commit commitsItem) {
    if (this.commits == null) {
      this.commits = new ArrayList<>();
    }
    this.commits.add(commitsItem);
    return this;
  }

  /**
   * List of commits
   *
   * @return commits
   */
  @Schema(description = "List of commits")
  @JsonProperty(value = JSON_PROPERTY_COMMITS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<@Valid V1Commit> getCommits() {
    return commits;
  }

  @JsonProperty(value = JSON_PROPERTY_COMMITS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCommits(@Nullable List<@Valid V1Commit> commits) {
    this.commits = commits;
  }

  public V1ScopeReleaseNoteSpec prs(@Nullable List<String> prs) {
    this.prs = prs;
    return this;
  }

  public V1ScopeReleaseNoteSpec addPrsItem(String prsItem) {
    if (this.prs == null) {
      this.prs = new ArrayList<>();
    }
    this.prs.add(prsItem);
    return this;
  }

  /**
   * List of PRs
   *
   * @return prs
   */
  @Schema(description = "List of PRs")
  @JsonProperty(value = JSON_PROPERTY_PRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<String> getPrs() {
    return prs;
  }

  @JsonProperty(value = JSON_PROPERTY_PRS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setPrs(@Nullable List<String> prs) {
    this.prs = prs;
  }

  public V1ScopeReleaseNoteSpec impactedDevices(@Nullable List<@Valid V1Device> impactedDevices) {
    this.impactedDevices = impactedDevices;
    return this;
  }

  public V1ScopeReleaseNoteSpec addImpactedDevicesItem(V1Device impactedDevicesItem) {
    if (this.impactedDevices == null) {
      this.impactedDevices = new ArrayList<>();
    }
    this.impactedDevices.add(impactedDevicesItem);
    return this;
  }

  /**
   * List of devices
   *
   * @return impactedDevices
   */
  @Schema(description = "List of devices")
  @JsonProperty(value = JSON_PROPERTY_IMPACTED_DEVICES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public @Nullable List<@Valid V1Device> getImpactedDevices() {
    return impactedDevices;
  }

  @JsonProperty(value = JSON_PROPERTY_IMPACTED_DEVICES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setImpactedDevices(@Nullable List<@Valid V1Device> impactedDevices) {
    this.impactedDevices = impactedDevices;
  }

  /** Return true if this v1.ScopeReleaseNoteSpec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    V1ScopeReleaseNoteSpec v1ScopeReleaseNoteSpec = (V1ScopeReleaseNoteSpec) o;
    return Objects.equals(this.versionDatetime, v1ScopeReleaseNoteSpec.versionDatetime)
        && Objects.equals(this.scopeVersion, v1ScopeReleaseNoteSpec.scopeVersion)
        && Objects.equals(this.commits, v1ScopeReleaseNoteSpec.commits)
        && Objects.equals(this.prs, v1ScopeReleaseNoteSpec.prs)
        && Objects.equals(this.impactedDevices, v1ScopeReleaseNoteSpec.impactedDevices);
  }

  @Override
  public int hashCode() {
    return Objects.hash(versionDatetime, scopeVersion, commits, prs, impactedDevices);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class V1ScopeReleaseNoteSpec {\n");
    sb.append("    versionDatetime: ").append(toIndentedString(versionDatetime)).append("\n");
    sb.append("    scopeVersion: ").append(toIndentedString(scopeVersion)).append("\n");
    sb.append("    commits: ").append(toIndentedString(commits)).append("\n");
    sb.append("    prs: ").append(toIndentedString(prs)).append("\n");
    sb.append("    impactedDevices: ").append(toIndentedString(impactedDevices)).append("\n");
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

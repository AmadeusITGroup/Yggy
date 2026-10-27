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

/** Response object of the Kubernetes policies service */
@Schema(description = "Response object of the Kubernetes policies service")
@JsonPropertyOrder({KubernetesPoliciesResponse.JSON_PROPERTY_KUBERNETES_POLICIES})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KubernetesPoliciesResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_KUBERNETES_POLICIES = "kubernetes_policies";
  private List<KubernetesPolicy> kubernetesPolicies;

  public KubernetesPoliciesResponse() {}

  public KubernetesPoliciesResponse kubernetesPolicies(List<KubernetesPolicy> kubernetesPolicies) {
    this.kubernetesPolicies = kubernetesPolicies;
    return this;
  }

  public KubernetesPoliciesResponse addKubernetesPoliciesItem(
      KubernetesPolicy kubernetesPoliciesItem) {
    if (this.kubernetesPolicies == null) {
      this.kubernetesPolicies = new ArrayList<>();
    }
    this.kubernetesPolicies.add(kubernetesPoliciesItem);
    return this;
  }

  /**
   * Get kubernetesPolicies
   *
   * @return kubernetesPolicies
   */
  @NotNull
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_KUBERNETES_POLICIES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<@Valid KubernetesPolicy> getKubernetesPolicies() {
    return kubernetesPolicies;
  }

  @JsonProperty(value = JSON_PROPERTY_KUBERNETES_POLICIES, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setKubernetesPolicies(List<KubernetesPolicy> kubernetesPolicies) {
    this.kubernetesPolicies = kubernetesPolicies;
  }

  @Override
  public KubernetesPoliciesResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public KubernetesPoliciesResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public KubernetesPoliciesResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public KubernetesPoliciesResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this KubernetesPoliciesResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KubernetesPoliciesResponse kubernetesPoliciesResponse = (KubernetesPoliciesResponse) o;
    return Objects.equals(this.kubernetesPolicies, kubernetesPoliciesResponse.kubernetesPolicies)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kubernetesPolicies, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KubernetesPoliciesResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    kubernetesPolicies: ").append(toIndentedString(kubernetesPolicies)).append("\n");
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

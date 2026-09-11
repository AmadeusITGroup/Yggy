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

/** Response object of the Kubernetes object service */
@Schema(description = "Response object of the Kubernetes object service")
@JsonPropertyOrder({
  KubernetesObjectsResponse.JSON_PROPERTY_PAAS_COMPONENTS,
  KubernetesObjectsResponse.JSON_PROPERTY_NETWORK_SETS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class KubernetesObjectsResponse extends ApiMetadata {
  public static final String JSON_PROPERTY_PAAS_COMPONENTS = "paas_components";
  private List<PAASComponent> paasComponents;

  public static final String JSON_PROPERTY_NETWORK_SETS = "network_sets";
  private List<NetworkSet> networkSets;

  public KubernetesObjectsResponse() {}

  public KubernetesObjectsResponse paasComponents(List<PAASComponent> paasComponents) {
    this.paasComponents = paasComponents;
    return this;
  }

  public KubernetesObjectsResponse addPaasComponentsItem(PAASComponent paasComponentsItem) {
    if (this.paasComponents == null) {
      this.paasComponents = new ArrayList<>();
    }
    this.paasComponents.add(paasComponentsItem);
    return this;
  }

  /**
   * Get paasComponents
   *
   * @return paasComponents
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_PAAS_COMPONENTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<PAASComponent> getPaasComponents() {
    return paasComponents;
  }

  @JsonProperty(value = JSON_PROPERTY_PAAS_COMPONENTS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setPaasComponents(List<PAASComponent> paasComponents) {
    this.paasComponents = paasComponents;
  }

  public KubernetesObjectsResponse networkSets(List<NetworkSet> networkSets) {
    this.networkSets = networkSets;
    return this;
  }

  public KubernetesObjectsResponse addNetworkSetsItem(NetworkSet networkSetsItem) {
    if (this.networkSets == null) {
      this.networkSets = new ArrayList<>();
    }
    this.networkSets.add(networkSetsItem);
    return this;
  }

  /**
   * Get networkSets
   *
   * @return networkSets
   */
  @NotNull
  @Valid
  @Schema(required = true, description = "")
  @JsonProperty(value = JSON_PROPERTY_NETWORK_SETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<NetworkSet> getNetworkSets() {
    return networkSets;
  }

  @JsonProperty(value = JSON_PROPERTY_NETWORK_SETS, required = true)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setNetworkSets(List<NetworkSet> networkSets) {
    this.networkSets = networkSets;
  }

  @Override
  public KubernetesObjectsResponse url(@Nullable String url) {
    this.setUrl(url);
    return this;
  }

  @Override
  public KubernetesObjectsResponse urlVersion(@Nullable String urlVersion) {
    this.setUrlVersion(urlVersion);
    return this;
  }

  @Override
  public KubernetesObjectsResponse version(@Nullable String version) {
    this.setVersion(version);
    return this;
  }

  @Override
  public KubernetesObjectsResponse apiVersion(@Nullable String apiVersion) {
    this.setApiVersion(apiVersion);
    return this;
  }

  /** Return true if this KubernetesObjectsResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KubernetesObjectsResponse kubernetesObjectsResponse = (KubernetesObjectsResponse) o;
    return Objects.equals(this.paasComponents, kubernetesObjectsResponse.paasComponents)
        && Objects.equals(this.networkSets, kubernetesObjectsResponse.networkSets)
        && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paasComponents, networkSets, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KubernetesObjectsResponse {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    paasComponents: ").append(toIndentedString(paasComponents)).append("\n");
    sb.append("    networkSets: ").append(toIndentedString(networkSets)).append("\n");
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

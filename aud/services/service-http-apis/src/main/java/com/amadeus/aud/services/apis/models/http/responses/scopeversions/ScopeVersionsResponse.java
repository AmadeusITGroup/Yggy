package com.amadeus.aud.services.apis.models.http.responses.scopeversions;

import com.amadeus.aud.libraries.common.BaseResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;
import java.util.Objects;

@JsonPropertyOrder({
  ScopeVersionsResponse.JSON_PROPERTY_SCOPES_VERSION,
})
public class ScopeVersionsResponse extends BaseResponse {
  public static final String JSON_PROPERTY_SCOPES_VERSION = "scopes_versions";

  @JsonProperty(JSON_PROPERTY_SCOPES_VERSION)
  private Map<String, String> scopesVersion;

  @JsonCreator
  public ScopeVersionsResponse() {}

  public ScopeVersionsResponse(
      String url,
      String urlVersion,
      String version,
      String apiVersion,
      Map<String, String> scopesVersion) {
    super(url, urlVersion, version, apiVersion);
    this.scopesVersion = scopesVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    if (this == o) {
      return true;
    }
    return (o instanceof ScopeVersionsResponse that) && scopesVersion.equals(that.scopesVersion);
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode = Objects.hash(super.hashCode(), scopesVersion);
    }
    return hashcode;
  }

  public Map<String, String> getScopesVersion() {
    return scopesVersion;
  }

  public void setScopesVersion(Map<String, String> scopesVersion) {
    this.scopesVersion = scopesVersion;
  }
}

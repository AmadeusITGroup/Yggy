package com.amadeus.aud.services.apis.models.http.responses.prs;

import com.amadeus.aud.libraries.common.BaseResponse;
import com.amadeus.aud.libraries.yaml.models.V1Device;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({
  BaseResponse.JSON_PROPERTY_URL,
  BaseResponse.JSON_PROPERTY_URL_VERSION,
  BaseResponse.JSON_PROPERTY_VERSION,
  BaseResponse.JSON_PROPERTY_API_VERSION,
  PrStatus.Status.JSON_PROPERTY_SCOPE_NAME,
  PrStatus.JSON_PROPERTY_STATUS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PrStatus extends BaseResponse {

  @JsonPropertyOrder({
    BaseResponse.JSON_PROPERTY_URL,
    BaseResponse.JSON_PROPERTY_URL_VERSION,
    BaseResponse.JSON_PROPERTY_VERSION,
    BaseResponse.JSON_PROPERTY_API_VERSION,
    PrStatus.JSON_PROPERTY_STATUS
  })
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public static class Status {
    public static final String JSON_PROPERTY_SCOPE_NAME = "scope_name";
    public static final String JSON_PROPERTY_SCOPE_CURRENT_VERSION = "scope_current_version";
    public static final String JSON_PROPERTY_AUD_VERSION = "aud_version";
    public static final String JSON_PROPERTY_STATUS = "status";
    public static final String JSON_PROPERTY_SUB_STATUS = "sub_status";
    public static final String JSON_PROPERTY_PR_NUMBER = "pr_number";
    public static final String JSON_PROPERTY_INTEGRATED_DATETIME = "integrated_datetime";
    public static final String JSON_PROPERTY_INTEGRATED_SCOPE_VERSION = "integrated_scope_version";
    public static final String JSON_PROPERTY_IMPACTED_DEVICES = "impacted_devices";

    private int hashcode = 0;

    @JsonProperty(JSON_PROPERTY_SCOPE_NAME)
    private String scopeName;

    @JsonProperty(JSON_PROPERTY_SCOPE_CURRENT_VERSION)
    private String scopeCurrentVersion;

    @JsonProperty(JSON_PROPERTY_AUD_VERSION)
    private String audVersion;

    @JsonProperty(JSON_PROPERTY_STATUS)
    private String status;

    @JsonProperty(JSON_PROPERTY_SUB_STATUS)
    private String subStatus;

    @JsonProperty(JSON_PROPERTY_PR_NUMBER)
    private String prNumber;

    @JsonProperty(JSON_PROPERTY_INTEGRATED_DATETIME)
    private String integratedDatetime;

    @JsonProperty(JSON_PROPERTY_INTEGRATED_SCOPE_VERSION)
    private String integratedScopeVersion;

    @JsonProperty(JSON_PROPERTY_IMPACTED_DEVICES)
    private List<V1Device> impactedDevices;

    @JsonCreator
    public Status() {}

    @Override
    public boolean equals(Object o) {
      if (!super.equals(o)) {
        return false;
      }
      if (this == o) {
        return true;
      }

      return (o instanceof Status that)
          && Objects.equals(scopeName, that.scopeName)
          && Objects.equals(scopeCurrentVersion, that.scopeCurrentVersion)
          && Objects.equals(status, that.status)
          && Objects.equals(subStatus, that.subStatus)
          && Objects.equals(prNumber, that.prNumber)
          && Objects.equals(integratedDatetime, that.integratedDatetime)
          && Objects.equals(integratedScopeVersion, that.integratedScopeVersion)
          && Objects.equals(impactedDevices, that.impactedDevices);
    }

    @Override
    public int hashCode() {
      if (hashcode == 0) {
        hashcode =
            Objects.hash(
                super.hashCode(),
                scopeName,
                scopeCurrentVersion,
                status,
                subStatus,
                prNumber,
                integratedDatetime,
                integratedScopeVersion,
                impactedDevices);
      }
      return hashcode;
    }

    public String getScopeName() {
      return scopeName;
    }

    public void setScopeName(String scopeName) {
      this.scopeName = scopeName;
    }

    public String getScopeCurrentVersion() {
      return scopeCurrentVersion;
    }

    public void setScopeCurrentVersion(String scopeCurrentVersion) {
      this.scopeCurrentVersion = scopeCurrentVersion;
    }

    public String getAudVersion() {
      return audVersion;
    }

    public void setAudVersion(String audVersion) {
      this.audVersion = audVersion;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public String getSubStatus() {
      return subStatus;
    }

    public void setSubStatus(String subStatus) {
      this.subStatus = subStatus;
    }

    public String getPrNumber() {
      return prNumber;
    }

    public void setPrNumber(String prNumber) {
      this.prNumber = prNumber;
    }

    public String getIntegratedDatetime() {
      return integratedDatetime;
    }

    public void setIntegratedDatetime(String integratedDatetime) {
      this.integratedDatetime = integratedDatetime;
    }

    public String getIntegratedScopeVersion() {
      return integratedScopeVersion;
    }

    public void setIntegratedScopeVersion(String integratedScopeVersion) {
      this.integratedScopeVersion = integratedScopeVersion;
    }

    public List<V1Device> getImpactedDevices() {
      return impactedDevices;
    }

    public void setImpactedDevices(List<V1Device> impactedDevices) {
      this.impactedDevices = impactedDevices;
    }
  }

  public static final String JSON_PROPERTY_STATUS = "status";

  @JsonProperty(JSON_PROPERTY_STATUS)
  private Status status;

  public PrStatus(String url, String urlVersion, String version, String apiVersion, Status status) {
    super(url, urlVersion, version, apiVersion);
    this.status = status;
  }

  @JsonCreator
  public PrStatus() {}

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    if (this == o) {
      return true;
    }

    return (o instanceof PrStatus that) && Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode = Objects.hash(super.hashCode(), status);
    }
    return hashcode;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}

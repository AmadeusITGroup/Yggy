package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record PrStatusV2(
    @JsonProperty("scope_name") String scopeName,
    @JsonProperty("scope_current_version") String scopeCurrentVersion,
    @JsonProperty("aud_version") String audVersion,
    @JsonProperty("status") String status,
    @JsonProperty("sub_status") String subStatus,
    @JsonProperty("pr_number") String prNumber,
    @JsonProperty("integrated_datetime") String integratedDatetime,
    @JsonProperty("integrated_scope_version") String integratedScopeVersion,
    @JsonProperty("impacted_devices") List<DeviceTypeGroup> impactedDevices) {}

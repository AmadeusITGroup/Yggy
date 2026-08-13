package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import com.amadeus.aud.libraries.yaml.models.V1Device;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.List;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ScopeReleaseNoteMixin.JSON_PROPERTY_COMMITS,
  ScopeReleaseNoteMixin.JSON_PROPERTY_IMPACTED_DEVICES
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(ScopeReleaseNote.class)
public interface ScopeReleaseNoteMixin extends AbstractBaseMixin<V1ScopeReleaseNote> {

  String JSON_PROPERTY_COMMITS = "commits";
  String JSON_PROPERTY_IMPACTED_DEVICES = "impacted_devices";

  @JsonProperty(JSON_PROPERTY_COMMITS)
  List<V1Commit> getCommits();

  @JsonProperty(JSON_PROPERTY_IMPACTED_DEVICES)
  List<V1Device> getImpactedDevices();
}

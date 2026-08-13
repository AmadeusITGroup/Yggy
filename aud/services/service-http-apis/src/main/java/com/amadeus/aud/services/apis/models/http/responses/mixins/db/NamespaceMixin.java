package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Namespace;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.quarkus.jackson.JacksonMixin;
import java.util.List;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  NamespaceMixin.JSON_PROPERTY_AUTHORIZED_SCOPES,
  NamespaceMixin.JSON_PROPERTY_IS_SHARED
})
@JacksonMixin(Namespace.class)
public interface NamespaceMixin extends AbstractBaseMixin<V1Namespace> {

  String JSON_PROPERTY_AUTHORIZED_SCOPES = "authorized_scopes";
  String JSON_PROPERTY_IS_SHARED = "is_shared";

  @JsonIgnore
  Set<Scope> getAuthorizedScopes();

  @JsonProperty(JSON_PROPERTY_AUTHORIZED_SCOPES)
  List<String> getAuthorizedScopesNames();

  @JsonProperty(JSON_PROPERTY_IS_SHARED)
  boolean isShared();

  @JsonIgnore
  Set<ComponentInstance> getComponentInstances();
}

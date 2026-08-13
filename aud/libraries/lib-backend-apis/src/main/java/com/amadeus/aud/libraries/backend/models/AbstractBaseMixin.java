package com.amadeus.aud.libraries.backend.models;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.jackson.JacksonMixin;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
// @JsonIdentityInfo(
//    generator = ObjectIdGenerators.PropertyGenerator.class,
//    property = AbstractBaseMixin.JSON_PROPERTY_ID)
@JacksonMixin(AbstractBase.class)
public interface AbstractBaseMixin<T> {

  String JSON_PROPERTY_ID = "id";
  String JSON_PROPERTY_TYPE = "type";
  String JSON_PROPERTY_SCOPE = "scope";
  String JSON_PROPERTY_COMMENT = "comment";
  String JSON_PROPERTY_NAME = "name";
  String JSON_PROPERTY_TAGS = "tags";
  String JSON_PROPERTY_PROVIDER_SPECIFIC = "provider_specific";
  String JSON_PROPERTY_SECURITY_JUSTIFICATION = "security_justification";

  String id();

  @JsonProperty(JSON_PROPERTY_TAGS)
  Set<Map<String, String>> getTagsAsMap();

  @JsonProperty(JSON_PROPERTY_NAME)
  String getName();

  @JsonProperty(JSON_PROPERTY_COMMENT)
  String getComment();

  @JsonProperty(JSON_PROPERTY_PROVIDER_SPECIFIC)
  Map<String, String> getProviderSpecific();

  @JsonProperty(JSON_PROPERTY_SECURITY_JUSTIFICATION)
  String getSecurityJustification();

  @JsonProperty(JSON_PROPERTY_SCOPE)
  String getScopeName();

  @JsonProperty(JSON_PROPERTY_TYPE)
  String getType();

  @JsonIgnore
  T getData();

  @JsonIgnore
  Map<String, String> getTags();

  @JsonIgnore
  long getVersion();

  @JsonIgnore
  String getReadableVersion();

  @JsonIgnore
  Map<String, String> getSimpleJsonRepresentation();

  @JsonIgnore
  String getURLHash();

  @JsonIgnore
  Object getMetadata();
}

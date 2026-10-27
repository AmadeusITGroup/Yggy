package com.amadeus.aud.services.apis.models.http.responses.mixins.db;

import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.ProtocolEnum;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.RangeSet;
import io.quarkus.jackson.JacksonMixin;
import java.util.Set;

@JsonPropertyOrder({
  AbstractBaseMixin.JSON_PROPERTY_ID,
  AbstractBaseMixin.JSON_PROPERTY_NAME,
  AbstractBaseMixin.JSON_PROPERTY_TYPE,
  AbstractBaseMixin.JSON_PROPERTY_SCOPE,
  AbstractBaseMixin.JSON_PROPERTY_COMMENT,
  AbstractBaseMixin.JSON_PROPERTY_TAGS,
  AbstractBaseMixin.JSON_PROPERTY_PROVIDER_SPECIFIC,
  ServiceMixin.JSON_PROPERTY_COMPONENT,
  ServiceMixin.JSON_PROPERTY_DEPRECATED_STATUS,
  ServiceMixin.JSON_PROPERTY_DEPRECATED_MESSAGE,
  ServiceMixin.JSON_PROPERTY_IS_PRIVATE,
  ServiceMixin.JSON_PROPERTY_IS_PCI,
  ServiceMixin.JSON_PROPERTY_IS_PII,
  ServiceMixin.JSON_PROPERTY_IS_ENCRYPTED,
  ServiceMixin.JSON_PROPERTY_IS_DB_SYNCHRO,
  ServiceMixin.JSON_PROPERTY_APPLICATION_ID,
  ServiceMixin.JSON_PROPERTY_SERVICE_NOW_ID,
  ServiceMixin.JSON_PROPERTY_PROTOCOL,
  ServiceMixin.JSON_PROPERTY_PORTS
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonMixin(Service.class)
public interface ServiceMixin extends AbstractBaseMixin<V1Service> {

  String JSON_PROPERTY_COMPONENT = "component";
  String JSON_PROPERTY_DEPRECATED_STATUS = "deprecated_status";
  String JSON_PROPERTY_DEPRECATED_MESSAGE = "deprecated_message";
  String JSON_PROPERTY_IS_PRIVATE = "is_private";
  String JSON_PROPERTY_IS_PCI = "is_pci";
  String JSON_PROPERTY_IS_PII = "is_pii";
  String JSON_PROPERTY_IS_ENCRYPTED = "is_encrypted";
  String JSON_PROPERTY_IS_DB_SYNCHRO = "is_db_synchro";
  String JSON_PROPERTY_APPLICATION_ID = "application_id";
  String JSON_PROPERTY_PROTOCOL = "protocol";
  String JSON_PROPERTY_SERVICE_NOW_ID = "service_now_id";
  String JSON_PROPERTY_PORTS = "ports";

  @JsonProperty(JSON_PROPERTY_SERVICE_NOW_ID)
  String getServiceNowId();

  @JsonProperty(JSON_PROPERTY_PORTS)
  String getPorts();

  @JsonProperty(JSON_PROPERTY_PROTOCOL)
  ProtocolEnum getProtocol();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_STATUS)
  DeprecatedStatus getDeprecatedStatus();

  @JsonProperty(JSON_PROPERTY_DEPRECATED_MESSAGE)
  String getDeprecatedMessage();

  @JsonProperty(JSON_PROPERTY_IS_PRIVATE)
  boolean isPrivate();

  @JsonProperty(JSON_PROPERTY_IS_PCI)
  boolean isPci();

  @JsonProperty(JSON_PROPERTY_IS_PII)
  boolean isPii();

  @JsonProperty(JSON_PROPERTY_IS_ENCRYPTED)
  boolean isEncrypted();

  @JsonProperty(JSON_PROPERTY_IS_DB_SYNCHRO)
  boolean isDbSynchro();

  @JsonProperty(JSON_PROPERTY_APPLICATION_ID)
  String getApplicationIdName();

  @JsonProperty(JSON_PROPERTY_COMPONENT)
  String getComponentName();

  @JsonIgnore
  Component getComponent();

  @JsonIgnore
  ApplicationId getApplicationId();

  @JsonIgnore
  RangeSet<Integer> getApprovedPorts();

  @JsonIgnore
  ProtocolEnum getApprovedProtocol();

  @JsonIgnore
  Set<DependencyInstance> getDependencyInstances();

  @JsonIgnore
  RangeSet<Integer> getPortsRange();
}

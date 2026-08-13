package com.amadeus.aud.services.apis.models.http.responses;

import com.amadeus.aud.libraries.common.BaseResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Collection;
import java.util.Objects;

@JsonPropertyOrder({GenericResponse.JSON_PROPERTY_VALUES})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> extends BaseResponse {
  public static final String JSON_PROPERTY_VALUES = "values";

  @JsonProperty(JSON_PROPERTY_VALUES)
  private Collection<T> values;

  @JsonCreator
  public GenericResponse() {}

  public GenericResponse(
      String apiVersion, String version, String url, String urlVersion, Collection<T> values) {
    super(url, urlVersion, version, apiVersion);
    this.values = values;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) {
      return false;
    }
    if (this == o) {
      return true;
    }
    return (o instanceof GenericResponse<?> that) && values.equals(that.values);
  }

  @Override
  public int hashCode() {
    if (hashcode == 0) {
      hashcode = Objects.hash(super.hashCode(), values);
    }
    return hashcode;
  }

  public Collection<T> getValues() {
    return values;
  }

  public void setValues(Collection<T> values) {
    this.values = values;
  }
}

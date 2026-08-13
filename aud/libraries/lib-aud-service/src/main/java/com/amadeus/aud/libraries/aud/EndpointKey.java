package com.amadeus.aud.libraries.aud;

import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import java.util.Locale;

public record EndpointKey(Endpoint.TypeEnum type, String value) {
  public EndpointKey(Endpoint.TypeEnum type, String value) {
    this.type = type;
    this.value = value != null ? value.toLowerCase(Locale.ROOT) : null;
  }

  public EndpointKey(Endpoint endpoint) {
    this(endpoint.getType(), endpoint.getValue());
  }
}

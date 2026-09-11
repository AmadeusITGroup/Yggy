package com.amadeus.aud.services.apis.services.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;

final class EndpointTestObjectMapper {
  private static final ObjectMapper INSTANCE = new ObjectMapper().findAndRegisterModules();

  private EndpointTestObjectMapper() {}

  static ObjectMapper get() {
    return INSTANCE;
  }
}

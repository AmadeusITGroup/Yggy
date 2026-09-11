package com.amadeus.aud.services.engine.resources;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.engine.data.exceptions.InvalidVersionException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class InvalidVersionExceptionMapperTest {

  @Test
  void invalidVersionIsMappedToBadRequest() {
    InvalidVersionException exception = new InvalidVersionException("invalid");

    Response response = new InvalidVersionExceptionMapper().toResponse(exception);

    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(exception.getMessage());
  }
}

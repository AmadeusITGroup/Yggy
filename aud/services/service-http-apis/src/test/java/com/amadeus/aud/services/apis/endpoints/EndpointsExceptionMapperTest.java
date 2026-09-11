package com.amadeus.aud.services.apis.endpoints;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.engine.data.exceptions.InvalidVersionException;
import com.amadeus.aud.libraries.yaml.models.Error;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class EndpointsExceptionMapperTest {
  private final EndpointsExceptionMapper sut = new EndpointsExceptionMapper();

  @Test
  void mapsInvalidVersionToBadRequest() {
    InvalidVersionException exception = new InvalidVersionException("NOT_SET");

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getCode()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    assertThat(error.getMessage()).isEqualTo("Invalid version format: NOT_SET");
  }

  @Test
  void preservesMethodNotAllowedResponse() {
    NotAllowedException exception = new NotAllowedException("GET");

    Response actual = sut.toResponse(exception);

    assertThat(actual).isSameAs(exception.getResponse());
    assertThat(actual.getStatus()).isEqualTo(Response.Status.METHOD_NOT_ALLOWED.getStatusCode());
    assertThat(actual.getAllowedMethods()).containsExactly("GET");
  }

  @Test
  void preservesNotAcceptableResponse() {
    NotAcceptableException exception = new NotAcceptableException();

    Response actual = sut.toResponse(exception);

    assertThat(actual).isSameAs(exception.getResponse());
    assertThat(actual.getStatus()).isEqualTo(Response.Status.NOT_ACCEPTABLE.getStatusCode());
  }
}

package com.amadeus.aud.libraries.nsg.http;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.nsg.models.LandingZoneNotFound;
import com.amadeus.aud.libraries.yaml.models.Error;
import io.quarkus.cache.CacheException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class NsgExceptionMapperTest {
  private final NsgExceptionMapper sut = new NsgExceptionMapper();

  @Test
  void mapsLandingZoneNotFoundTo404() {
    LandingZoneNotFound exception = new LandingZoneNotFound("my-lz");

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getCode()).isEqualTo(404);
    assertThat(error.getMessage()).isEqualTo("Landing zone not found: my-lz");
  }

  @Test
  void mapsIllegalStateExceptionTo400() {
    IllegalStateException exception =
        new IllegalStateException("Landing zone is not in Azure, NSGs can not be processed.");

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getCode()).isEqualTo(400);
    assertThat(error.getMessage())
        .isEqualTo("Landing zone is not in Azure, NSGs can not be processed.");
  }

  @Test
  void mapsIllegalCallerExceptionTo503() {
    IllegalCallerException exception =
        new IllegalCallerException("Repository not ready yet for version 1.0.0");

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getCode()).isEqualTo(503);
    assertThat(error.getMessage()).isEqualTo("Service Unavailable. Try again later.");
  }

  @Test
  void mapsUnknownExceptionTo500() {
    RuntimeException exception = new RuntimeException("unexpected");

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getCode()).isEqualTo(500);
    assertThat(error.getMessage()).isEqualTo("Internal Server Error");
  }

  @Test
  void unwrapsCacheExceptionToLandingZoneNotFound() {
    LandingZoneNotFound cause = new LandingZoneNotFound("cached-lz");
    CacheException exception = new CacheException(cause);

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getMessage()).isEqualTo("Landing zone not found: cached-lz");
  }

  @Test
  void unwrapsCacheExceptionToIllegalStateException() {
    IllegalStateException cause = new IllegalStateException("not Azure");
    CacheException exception = new CacheException(cause);

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getMessage()).isEqualTo("not Azure");
  }

  @Test
  void unwrapsCacheExceptionToIllegalCallerException() {
    IllegalCallerException cause =
        new IllegalCallerException("Repository not ready yet for version 1.0.0");
    CacheException exception = new CacheException(cause);

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
  }

  @Test
  void cacheExceptionWithNullCauseMapsTo500() {
    CacheException exception = new CacheException(null);

    Response actual = sut.toResponse(exception);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getCode()).isEqualTo(500);
    assertThat(error.getMessage()).isEqualTo("Internal Server Error");
  }

  @Test
  void mapsNullExceptionTo500() {
    Response actual = sut.toResponse(null);

    assertThat(actual.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    Error error = (Error) actual.getEntity();
    assertThat(error.getCode()).isEqualTo(500);
    assertThat(error.getMessage()).isEqualTo("Internal Server Error");
  }
}

package com.amadeus.aud.libraries.nsg.http;

import com.amadeus.aud.libraries.nsg.models.LandingZoneNotFound;
import com.amadeus.aud.libraries.yaml.models.Error;
import io.quarkus.cache.CacheException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/// Catches all exceptions thrown by NSG endpoints and returns proper error responses.
@Provider
public class NsgExceptionMapper implements ExceptionMapper<Throwable> {
  @Override
  public Response toResponse(Throwable exception) {
    return switch (unwrapException(exception)) {
      case LandingZoneNotFound e -> notFound(e.getMessage());
      case IllegalStateException e -> badRequest(e.getMessage());
      case IllegalCallerException _ ->
          error(Response.Status.SERVICE_UNAVAILABLE, "Service Unavailable. Try again later.");
      case null, default -> internalError("Internal Server Error", exception);
    };
  }

  private Throwable unwrapException(Throwable exception) {
    if (exception instanceof CacheException && exception.getCause() != null) {
      return exception.getCause();
    }
    return exception;
  }

  private static Response internalError(String message, Throwable exception) {
    Log.error("Internal Server Error", message, exception);
    return error(Response.Status.INTERNAL_SERVER_ERROR, message);
  }

  private static Response notFound(String message) {
    return error(Response.Status.NOT_FOUND, message);
  }

  private static Response badRequest(String message) {
    Log.infof("Bad request: %s", message);
    return error(Response.Status.BAD_REQUEST, message);
  }

  private static Response error(Response.Status status, String message) {
    final Error error = new Error();
    error.setCode(status.getStatusCode());
    error.setMessage(message);
    return Response.status(status).entity(error).build();
  }
}

package com.amadeus.aud.services.apis.endpoints;

import com.amadeus.aud.libraries.yaml.models.Error;
import com.azure.storage.blob.models.BlobStorageException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.cache.CacheException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/// Catches all exceptions thrown by all endpoints and returns proper error responses.
@Provider
public class EndpointsExceptionMapper implements ExceptionMapper<Throwable> {
  @Override
  public Response toResponse(Throwable exception) {
    return switch (unwrapException(exception)) {
      case JsonProcessingException e ->
          internalError("Error processing JSON: " + e.getMessage(), e);
      case BadRequestException e -> error(Response.Status.BAD_REQUEST, e.getMessage());
      case InternalServerErrorException e ->
          internalError("Internal Server Error: " + e.getMessage(), e);
      case NotFoundException e -> notFound(e.getMessage());
      case ServiceUnavailableException _ ->
          error(Response.Status.SERVICE_UNAVAILABLE, "Service Unavailable. Try again later.");
      case BlobStorageException e -> handleBlobStorageException(e);
      case null, default -> internalError("Internal Server Error", exception);
    };
  }

  private static Response handleBlobStorageException(BlobStorageException e) {
    if (e.getStatusCode() == 404) {
      return notFound(e.getMessage());
    }
    return internalError(e.getMessage(), e);
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

  private static Response error(Response.Status status, String message) {
    final Error error = new Error();
    error.setCode(status.getStatusCode());
    error.setMessage(message);
    return Response.status(status).entity(error).build();
  }
}

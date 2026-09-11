package com.amadeus.aud.services.engine.resources;

import com.amadeus.aud.libraries.engine.data.exceptions.InvalidVersionException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public final class InvalidVersionExceptionMapper
    implements ExceptionMapper<InvalidVersionException> {

  @Override
  public Response toResponse(InvalidVersionException exception) {
    return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
  }
}

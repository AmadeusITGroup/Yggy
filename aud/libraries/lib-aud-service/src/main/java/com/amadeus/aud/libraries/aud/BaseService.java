package com.amadeus.aud.libraries.aud;

import com.amadeus.aud.libraries.engine.data.repositories.base.BaseRepository;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;

@Singleton
public class BaseService {

  private ObjectMapper objectMapper;

  @Inject
  protected void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.objectMapper.findAndRegisterModules();
  }

  public Response getOkResponse(Object object) throws JsonProcessingException {
    byte[] responseBytes = objectMapper.writeValueAsBytes(object);
    return Response.status(Response.Status.OK).entity(responseBytes).build();
  }

  public void checkRepository(
      @Nullable ConcurrentRepository<?> repository, String versionNumber, String repoName) {
    if (repository == null || !repository.isReady()) {
      throw new ServiceUnavailableException(
          repoName + " repository is not ready for version: " + versionNumber);
    }
  }

  public void checkRepository(
      @Nullable BaseRepository<?> repository, String versionNumber, String repoName) {
    if (repository == null || !repository.isReady()) {
      throw new ServiceUnavailableException(
          repoName + " repository is not ready for version: " + versionNumber);
    }
  }
}

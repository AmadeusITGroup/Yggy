package com.amadeus.aud.libraries.backend.services.v2;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UriFromFile implements UriProvider {

  private final ObjectMapper objectMapper;
  private Response<List<UriItem>> cachedResponse;

  @Inject
  public UriFromFile(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  private static void addValidUri(UriItem uriItem, List<UriItem> validUris) {
    try {
      new URI(uriItem.uri());
      validUris.add(uriItem);
    } catch (URISyntaxException e) {
      Log.warnf("Skipping invalid URI item: %s - Reason: %s", uriItem.uri(), e.getMessage());
    }
  }

  public void updateResponse(InputStream inputStream) throws IOException {
    Response<List<UriItem>> rawResponse =
        objectMapper.readValue(inputStream, new TypeReference<>() {});
    List<UriItem> validUris = new ArrayList<>();

    for (UriItem uriItem : rawResponse.data()) {
      if (uriItem.title() == null
          || uriItem.title().isEmpty()
          || uriItem.uri() == null
          || uriItem.uri().isEmpty()
          || uriItem.category() == null
          || uriItem.category().isEmpty()) {
        Log.warnf("Skipping invalid URI item: %s - Reason: Empty fields", uriItem.uri());
        continue;
      }
      addValidUri(uriItem, validUris);
    }

    cachedResponse = new Response<>(validUris, null);
  }

  void onStart(@Observes StartupEvent event) {
    try (InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("uris.json")) {
      if (inputStream == null) {
        throw new IllegalStateException("uris.json not found in classpath");
      }
      updateResponse(inputStream);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load uris.json at startup", e);
    }
  }

  @Override
  public Uni<Response<List<UriItem>>> getUris() {
    return Uni.createFrom()
        .item(() -> cachedResponse)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }
}

package com.amadeus.aud.libraries.engine.data.services.external.parsing;

import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public final class JsonService extends AbstractDocumentParsingService {

  @Inject
  public JsonService() {
    super(ObjectMapperFactory.createJsonMapper());
  }

  public ObjectMapper getMapper() {
    return this.objectMapper;
  }

  @Override
  protected ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> parseContent(
      String json) throws IOException {
    final ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> data =
        new ConcurrentHashMap<>();

    parseContent(createParser(json), data);

    return data;
  }

  @Override
  protected void parseFromRegularFile(
      File file, Map<Class<? extends ModelElement>, List<ModelElement>> data) throws IOException {
    parseContent(createParser(file), data);
  }

  private void parseContent(
      JsonParser jsonParser, Map<Class<? extends ModelElement>, List<ModelElement>> data)
      throws IOException {
    try (jsonParser) {
      if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
        throw new IOException("Expected JSON array at root level");
      }

      while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        addElementToData(data, objectMapper.readValue(jsonParser, ModelElement.class));
      }
    }
  }

  public ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> importFromReader(
      Reader reader) throws IOException {
    final ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> data =
        new ConcurrentHashMap<>();
    try (JsonParser jsonParser = objectMapper.getFactory().createParser(reader)) {
      if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
        throw new IOException("Expected JSON array at root level");
      }
      while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
        addElementToData(data, objectMapper.readValue(jsonParser, ModelElement.class));
      }
    }
    return data;
  }
}

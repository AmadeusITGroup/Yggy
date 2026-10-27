package com.amadeus.aud.libraries.checker.reports;

import com.amadeus.aud.libraries.checker.models.Message;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class JsonReporter implements Reporter {
  private final OutputStream outputStream;
  private final ObjectMapper mapper;
  private final JsonGenerator generator;

  public JsonReporter(OutputStream outputStream) {
    this.outputStream = new BufferedOutputStream(outputStream);
    this.mapper = new ObjectMapper();
    mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    mapper.registerModule(new JavaTimeModule());
    try {
      this.generator =
          mapper
              .getFactory()
              .createGenerator(outputStream, JsonEncoding.UTF8)
              .setPrettyPrinter(new DefaultPrettyPrinter());
      generator.writeStartObject();
      generator.writeStringField("title", "AUD Report");
      generator.writeFieldName("messages");
      generator.writeStartArray();
      outputStream.flush();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void addMessages(Collection<Message> messages) throws IOException {
    for (Message message : messages) {
      mapper.writeValue(generator, message);
    }
    outputStream.flush();
  }

  @Override
  public void close() throws IOException {
    generator.writeEndArray();
    generator.writeEndObject();
    outputStream.flush();
    generator.close();
  }
}

package com.amadeus.aud.libraries.http.logging;

import io.quarkus.logging.json.runtime.JsonFormatter.JsonLogGenerator;
import io.quarkus.logging.json.runtime.JsonProvider;
import jakarta.annotation.Nullable;
import java.util.Map;
import org.jboss.logmanager.ExtLogRecord;

public final class HttpAccessLogJsonProvider implements JsonProvider {
  @Override
  public void writeTo(JsonLogGenerator generator, ExtLogRecord logRecord) throws Exception {
    @Nullable Object[] parameters = logRecord.getParameters();
    if (HttpAccessLogFilter.LOG_CATEGORY.equals(logRecord.getLoggerName())
        && parameters != null
        && parameters.length == 1
        && parameters[0]
            instanceof
            HttpAccessLog(
                String method,
                int status,
                String uri,
                String proto,
                Map<String, String> requestHeaders,
                Map<String, String> responseHeaders)) {
      generator
          .startObject("request")
          .add("method", method)
          .add("uri", uri)
          .add("proto", proto)
          .add("headers", requestHeaders)
          .endObject()
          .startObject("response")
          .add("status", status)
          .add("headers", responseHeaders)
          .endObject();
    }
  }
}

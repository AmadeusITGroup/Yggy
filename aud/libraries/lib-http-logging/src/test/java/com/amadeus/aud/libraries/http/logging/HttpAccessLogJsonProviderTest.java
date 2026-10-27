package com.amadeus.aud.libraries.http.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.json.runtime.JsonFormatter;
import io.quarkus.logging.json.runtime.JsonProvider;
import io.vertx.core.http.HttpHeaders;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import org.jboss.logmanager.ExtLogRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpAccessLogJsonProviderTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void writesNestedRequestAndResponseObjectsOnAccessEvents() throws Exception {
    ExtLogRecord logRecord = accessRecord(sampleEvent());
    logRecord.putMdc("request_id", "correlation-id");

    JsonNode json = mapper.readTree(formatter().format(logRecord));

    JsonNode request = json.path("request");
    JsonNode response = json.path("response");
    JsonNode requestHeaders = request.path("headers");
    JsonNode responseHeaders = response.path("headers");
    assertThat(request.isObject()).isTrue();
    assertThat(request.size()).isEqualTo(4);
    assertThat(request.path("method").asText()).isEqualTo("GET");
    assertThat(request.path("uri").asText()).isEqualTo("/v1/yaml-schema");
    assertThat(request.path("proto").asText()).isEqualTo("HTTP/1.1");
    assertThat(requestHeaders.isObject()).isTrue();
    assertThat(requestHeaders.size()).isEqualTo(4);
    assertThat(requestHeaders.path("host").asText()).isEqualTo("example:8080");
    assertThat(requestHeaders.path("user-agent").asText()).isEqualTo("schema-client/1.0");
    assertThat(requestHeaders.path("accept").asText()).isEqualTo("application/json");
    assertThat(requestHeaders.path("accept-encoding").asText()).isEqualTo("gzip, deflate");
    for (String field : List.of("host", "user-agent", "accept", "accept-encoding")) {
      assertThat(request.has(field)).as("request header %s is nested", field).isFalse();
    }
    assertThat(response.isObject()).isTrue();
    assertThat(response.size()).isEqualTo(2);
    assertThat(response.path("status").isInt()).isTrue();
    assertThat(response.path("status").intValue()).isEqualTo(200);
    assertThat(responseHeaders.isObject()).isTrue();
    assertThat(responseHeaders.size()).isEqualTo(2);
    assertThat(responseHeaders.path("content-encoding").asText()).isEqualTo("gzip");
    assertThat(responseHeaders.path("content-type").asText())
        .isEqualTo("application/json; charset=utf-8");
    for (String field : List.of("content-encoding", "content-type")) {
      assertThat(response.has(field)).as("response header %s is nested", field).isFalse();
    }
    for (String field :
        List.of(
            "method",
            "status",
            "uri",
            "proto",
            "path",
            "headers",
            "host",
            "user-agent",
            "accept",
            "accept-encoding",
            "content-encoding",
            "content-type")) {
      assertThat(json.has(field)).as("no top-level %s", field).isFalse();
    }
    assertThat(json.path("mdc").path("request_id").asText()).isEqualTo("correlation-id");
    assertThat(logRecord.getMdcCopy()).containsEntry("request_id", "correlation-id");
    assertThat(logRecord.getMdcCopy())
        .doesNotContainKeys(
            "request",
            "response",
            "headers",
            "method",
            "status",
            "uri",
            "proto",
            "path",
            "host",
            "user-agent",
            "accept",
            "accept-encoding",
            "content-encoding",
            "content-type");
  }

  @Test
  void preservesHeaderMapKeysWithoutRenaming() throws Exception {
    Map<String, String> requestHeaders =
        Map.of("User-Agent", "schema-client/1.0", "Accept-Encoding", "gzip");
    Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
    ExtLogRecord logRecord =
        accessRecord(
            new HttpAccessLog("GET", 200, "/", "HTTP/1.1", requestHeaders, responseHeaders));

    JsonNode json = mapper.readTree(formatter().format(logRecord));

    assertThat(json.path("request").path("headers")).isEqualTo(mapper.valueToTree(requestHeaders));
    assertThat(json.path("response").path("headers"))
        .isEqualTo(mapper.valueToTree(responseHeaders));
  }

  @Test
  void escapesValuesWithoutEmbeddingJsonInTheMessage() throws Exception {
    ExtLogRecord logRecord =
        accessRecord(
            new HttpAccessLog(
                "GET",
                200,
                "/quoted/\"path",
                "HTTP/1.1",
                Map.of(
                    HttpHeaders.HOST.toString(), "quoted\"host",
                    HttpHeaders.USER_AGENT.toString(), "schema-client/1.0 (\"quoted\")",
                    HttpHeaders.ACCEPT.toString(), "application/json; profile=\"quoted\"",
                    HttpHeaders.ACCEPT_ENCODING.toString(), "gzip"),
                Map.of(
                    HttpHeaders.CONTENT_ENCODING.toString(), "gzip",
                    HttpHeaders.CONTENT_TYPE.toString(), "application/json; profile=\"quoted\"")));

    JsonNode json = mapper.readTree(formatter().format(logRecord));

    assertThat(json.path("request").path("uri").asText()).isEqualTo("/quoted/\"path");
    assertThat(json.path("request").path("headers").path("host").asText())
        .isEqualTo("quoted\"host");
    assertThat(json.path("request").path("headers").path("user-agent").asText())
        .isEqualTo("schema-client/1.0 (\"quoted\")");
    assertThat(json.path("request").path("headers").path("accept").asText())
        .isEqualTo("application/json; profile=\"quoted\"");
    assertThat(json.path("response").path("headers").path("content-type").asText())
        .isEqualTo("application/json; profile=\"quoted\"");
    assertThat(json.path("message").isTextual()).isTrue();
  }

  @Test
  void omitsAbsentHeaders() throws Exception {
    ExtLogRecord logRecord =
        accessRecord(new HttpAccessLog("GET", 200, "/", "HTTP/1.1", Map.of(), Map.of()));

    JsonNode json = mapper.readTree(formatter().format(logRecord));

    assertThat(json.path("request").path("headers").isObject()).isTrue();
    assertThat(json.path("request").path("headers").size()).isZero();
    assertThat(json.path("response").path("headers").isObject()).isTrue();
    assertThat(json.path("response").path("headers").size()).isZero();
  }

  @ParameterizedTest
  @ValueSource(strings = {"request", "response"})
  void rejectsNullHeaderValues(String headers) {
    HttpAccessLog sample = sampleEvent();
    Map<String, String> requestHeaders = new LinkedHashMap<>(sample.requestHeaders());
    Map<String, String> responseHeaders = new LinkedHashMap<>(sample.responseHeaders());
    if ("request".equals(headers)) {
      requestHeaders.put("host", null);
    } else {
      responseHeaders.put("content-type", null);
    }

    assertThatThrownBy(
            () ->
                new HttpAccessLog(
                    sample.method(),
                    sample.status(),
                    sample.uri(),
                    sample.proto(),
                    requestHeaders,
                    responseHeaders))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void doesNotAddAccessFieldsToApplicationLogs() throws Exception {
    ExtLogRecord logRecord = accessRecord(sampleEvent());
    logRecord.setLoggerName("com.amadeus.aud.application");

    JsonNode json = mapper.readTree(formatter().format(logRecord));

    assertThat(json.has("request")).isFalse();
    assertThat(json.has("response")).isFalse();
  }

  @Test
  void ignoresUnstructuredMessagesInTheAccessCategory() throws Exception {
    ExtLogRecord logRecord = new ExtLogRecord(Level.INFO, "ordinary message", getClass().getName());
    logRecord.setLoggerName(HttpAccessLogFilter.LOG_CATEGORY);

    JsonNode json = mapper.readTree(formatter().format(logRecord));

    assertThat(json.has("request")).isFalse();
    assertThat(json.has("response")).isFalse();
    assertThat(json.path("message").asText()).isEqualTo("ordinary message");
  }

  @Test
  void ignoresUnexpectedAccessArguments() throws Exception {
    ExtLogRecord logRecord = accessRecord("ordinary argument");
    ExtLogRecord empty = accessRecord();
    empty.setMessage("ordinary message");
    ExtLogRecord multiple = accessRecord(sampleEvent(), "extra");
    JsonFormatter formatter = formatter();

    for (ExtLogRecord candidate : List.of(logRecord, empty, multiple)) {
      JsonNode json = mapper.readTree(formatter.format(candidate));
      assertThat(json.has("request")).isFalse();
      assertThat(json.has("response")).isFalse();
    }
  }

  @Test
  void honorsExcludedUriWithoutFilteringHeaderMapEntries() throws Exception {
    HttpAccessLog event = sampleEvent();
    ExtLogRecord logRecord = accessRecord(event);
    JsonFormatter formatter = formatter();
    formatter.setExcludedKeys(
        Set.of(
            "uri",
            "host",
            "user-agent",
            "accept",
            "accept-encoding",
            "content-encoding",
            "content-type"));

    JsonNode json = mapper.readTree(formatter.format(logRecord));

    assertThat(json.path("request").has("uri")).isFalse();
    assertThat(json.path("request").path("headers"))
        .isEqualTo(mapper.valueToTree(event.requestHeaders()));
    assertThat(json.path("response").path("headers"))
        .isEqualTo(mapper.valueToTree(event.responseHeaders()));
    assertThat(json.path("request").path("method").asText()).isEqualTo("GET");
    assertThat(json.path("response").path("status").intValue()).isEqualTo(200);
  }

  @ParameterizedTest
  @ValueSource(strings = {"request", "response", "headers"})
  void honorsExcludedObjectsWithoutDroppingSiblingFields(String excluded) throws Exception {
    ExtLogRecord logRecord = accessRecord(sampleEvent());
    JsonFormatter formatter = formatter();
    formatter.setExcludedKeys(Set.of(excluded));

    JsonNode json = mapper.readTree(formatter.format(logRecord));

    assertThat(json.has(excluded)).isFalse();
    if ("request".equals(excluded)) {
      assertThat(json.path("response").path("status").intValue()).isEqualTo(200);
      assertThat(json.path("response").path("headers").size()).isEqualTo(2);
    } else if ("headers".equals(excluded)) {
      assertThat(json.path("request").has("headers")).isFalse();
      assertThat(json.path("response").has("headers")).isFalse();
      assertThat(json.path("request").path("method").asText()).isEqualTo("GET");
      assertThat(json.path("response").path("status").intValue()).isEqualTo(200);
    } else {
      assertThat(json.path("request").path("method").asText()).isEqualTo("GET");
      assertThat(json.path("request").path("uri").asText()).isEqualTo("/v1/yaml-schema");
      assertThat(json.path("request").path("headers").size()).isEqualTo(4);
    }
  }

  @Test
  void snapshotsHeaderMapsForDeferredFormatting() throws Exception {
    HttpAccessLog sample = sampleEvent();
    Map<String, String> requestHeaders = new LinkedHashMap<>(sample.requestHeaders());
    Map<String, String> responseHeaders = new LinkedHashMap<>(sample.responseHeaders());
    HttpAccessLog event =
        new HttpAccessLog(
            sample.method(),
            sample.status(),
            sample.uri(),
            sample.proto(),
            requestHeaders,
            responseHeaders);

    requestHeaders.clear();
    responseHeaders.clear();
    JsonNode json = mapper.readTree(formatter().format(accessRecord(event)));

    assertThat(json.path("request").path("headers").size()).isEqualTo(4);
    assertThat(json.path("response").path("headers").size()).isEqualTo(2);
    assertThatThrownBy(() -> event.requestHeaders().put(HttpHeaders.HOST.toString(), "changed"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(
            () -> event.responseHeaders().put(HttpHeaders.CONTENT_TYPE.toString(), "changed"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  private HttpAccessLog sampleEvent() {
    return new HttpAccessLog(
        "GET",
        200,
        "/v1/yaml-schema",
        "HTTP/1.1",
        Map.of(
            HttpHeaders.HOST.toString(), "example:8080",
            HttpHeaders.USER_AGENT.toString(), "schema-client/1.0",
            HttpHeaders.ACCEPT.toString(), "application/json",
            HttpHeaders.ACCEPT_ENCODING.toString(), "gzip, deflate"),
        Map.of(
            HttpHeaders.CONTENT_ENCODING.toString(), "gzip",
            HttpHeaders.CONTENT_TYPE.toString(), "application/json; charset=utf-8"));
  }

  @Test
  void honorsExcludedProtocolWithoutDroppingOtherRequestFields() throws Exception {
    JsonFormatter formatter = formatter();
    formatter.setExcludedKeys(Set.of("proto"));

    JsonNode json = mapper.readTree(formatter.format(accessRecord(sampleEvent())));

    assertThat(json.path("request").has("proto")).isFalse();
    assertThat(json.path("request").path("method").asText()).isEqualTo("GET");
    assertThat(json.path("request").path("headers").size()).isEqualTo(4);
    assertThat(json.path("response").path("status").intValue()).isEqualTo(200);
  }

  @Test
  void registersProviderThroughServiceLoader() {
    List<Class<?>> providerTypes = new ArrayList<>();

    for (JsonProvider provider : ServiceLoader.load(JsonProvider.class)) {
      providerTypes.add(provider.getClass());
    }

    assertThat(providerTypes).contains(HttpAccessLogJsonProvider.class);
  }

  private JsonFormatter formatter() {
    JsonFormatter formatter = new JsonFormatter();
    formatter.setDiscoveredProviders(List.of(new HttpAccessLogJsonProvider()));
    return formatter;
  }

  private ExtLogRecord accessRecord(Object... parameters) {
    ExtLogRecord logRecord =
        new ExtLogRecord(
            Level.INFO, "HTTP request: %s", ExtLogRecord.FormatStyle.PRINTF, getClass().getName());
    logRecord.setLoggerName(HttpAccessLogFilter.LOG_CATEGORY);
    logRecord.setParameters(parameters);
    return logRecord;
  }
}

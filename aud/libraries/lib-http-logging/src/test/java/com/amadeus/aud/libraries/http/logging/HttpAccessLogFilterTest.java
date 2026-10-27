package com.amadeus.aud.libraries.http.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.quarkus.vertx.http.runtime.filters.Filter;
import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.ext.web.Router;
import jakarta.annotation.Nullable;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;
import org.jboss.logmanager.formatters.PatternFormatter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Execution(ExecutionMode.SAME_THREAD)
class HttpAccessLogFilterTest {
  private static final BlockingQueue<ExtLogRecord> EVENTS = new LinkedBlockingQueue<>();
  private static final Logger LOGGER = Logger.getLogger(HttpAccessLogFilter.LOG_CATEGORY);
  private static final Handler HANDLER =
      new ExtHandler() {
        @Override
        protected void doPublish(ExtLogRecord logRecord) {
          EVENTS.add(logRecord);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
      };
  private static Vertx vertx;
  private static HttpServer server;
  private static HttpClient client;
  @Nullable private static Level previousLevel;
  private static boolean previousUseParentHandlers;

  @BeforeAll
  static void startServer() throws Exception {
    previousLevel = LOGGER.getLevel();
    previousUseParentHandlers = LOGGER.getUseParentHandlers();
    LOGGER.setLevel(Level.INFO);
    LOGGER.setUseParentHandlers(false);
    LOGGER.addHandler(HANDLER);
    vertx = Vertx.vertx();
    client = vertx.createHttpClient();
    Router router = Router.router(vertx);
    Filters filters = new Filters();
    new HttpAccessLogFilter(true).register(filters);
    for (Filter filter : filters.getFilters()) {
      router.route().order(-filter.getPriority()).handler(filter.getHandler());
    }
    router
        .route("/created")
        .handler(context -> context.response().setStatusCode(201).end("created"));
    router.route("/redirect").handler(context -> context.redirect("/created"));
    router.route("/denied").handler(context -> context.response().setStatusCode(401).end());
    router.route("/failed").handler(context -> context.fail(500));
    router
        .route("/encoded")
        .handler(
            context ->
                context
                    .response()
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .end("{\"value\":\"" + "x".repeat(1024) + "\"}"));
    router
        .route("/aborted")
        .handler(
            context ->
                context
                    .response()
                    .setChunked(true)
                    .write("partial")
                    .onSuccess(ignored -> context.request().connection().close()));
    router.route("/rerouted").handler(context -> context.reroute(HttpMethod.GET, "/created"));
    server =
        await(
            vertx
                .createHttpServer(new HttpServerOptions().setCompressionSupported(true))
                .requestHandler(router)
                .listen(0, "127.0.0.1"));
  }

  @AfterAll
  static void stopServer() throws Exception {
    try {
      await(vertx.close());
    } finally {
      LOGGER.removeHandler(HANDLER);
      LOGGER.setLevel(previousLevel);
      LOGGER.setUseParentHandlers(previousUseParentHandlers);
    }
  }

  @BeforeEach
  void clearEvents() {
    EVENTS.clear();
  }

  @ParameterizedTest
  @CsvSource({
    "GET, /created, 201",
    "POST, /created, 201",
    "HEAD, /created, 201",
    "GET, /redirect, 302",
    "GET, /denied, 401",
    "GET, /missing, 404",
    "GET, /failed, 500"
  })
  void logsFinalStatusForCompletedResponses(String method, String path, int status)
      throws Exception {
    HttpClientResponse response =
        await(exchange(HttpMethod.valueOf(method), path, "public.example:8443", null));
    HttpAccessLog event = nextEvent();

    assertThat(response.statusCode()).isEqualTo(status);
    assertThat(event.method()).isEqualTo(method);
    assertThat(event.status()).isEqualTo(status);
    assertThat(event.uri()).isEqualTo(path);
    assertThat(event.proto()).isEqualTo("HTTP/1.1");
    assertThat(event.requestHeaders())
        .isEqualTo(
            expectedEvent(method, status, path, "public.example:8443", null).requestHeaders());
    assertThat(event.responseHeaders()).isEqualTo(expectedResponseHeaders(response));
    assertThat(EVENTS).isEmpty();
  }

  @Test
  void excludesQueryStringsAndSensitiveHeaders() throws Exception {
    int responseStatus =
        await(
            client
                .request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/created?token=secret")
                .compose(
                    request ->
                        request
                            .putHeader("Host", "public.example")
                            .putHeader("User-Agent", "schema-client/1.0")
                            .putHeader("Authorization", "Bearer secret")
                            .putHeader("Cookie", "session=secret")
                            .send())
                .compose(response -> response.body().map(ignored -> response.statusCode())));
    HttpAccessLog event = nextEvent();

    assertThat(responseStatus).isEqualTo(201);
    assertThat(event)
        .isEqualTo(expectedEvent("GET", 201, "/created", "public.example", "schema-client/1.0"));
    assertThat(event.toString()).doesNotContain("secret", "token", "Authorization", "Cookie");
  }

  @Test
  void logsOneEventWithOriginalMethodAndPathAfterRerouting() throws Exception {
    int responseStatus =
        await(request(HttpMethod.POST, "/rerouted", "public.example", "original-client/1.0"));
    HttpAccessLog event = nextEvent();

    assertThat(responseStatus).isEqualTo(201);
    assertThat(event)
        .isEqualTo(
            expectedEvent("POST", 201, "/rerouted", "public.example", "original-client/1.0"));
    assertThat(EVENTS).isEmpty();
  }

  @Test
  void keepsConcurrentRequestsSeparate() throws Exception {
    Future<Integer> created =
        request(HttpMethod.POST, "/created", "first.example", "first-client/1.0");
    Future<Integer> denied =
        request(HttpMethod.GET, "/denied", "second.example", "second-client/1.0");

    assertThat(await(created)).isEqualTo(201);
    assertThat(await(denied)).isEqualTo(401);
    List<HttpAccessLog> events = List.of(nextEvent(), nextEvent());

    assertThat(events)
        .containsExactlyInAnyOrder(
            expectedEvent("POST", 201, "/created", "first.example", "first-client/1.0"),
            expectedEvent("GET", 401, "/denied", "second.example", "second-client/1.0"));
  }

  @Test
  void omitsAbsentHostHeader() throws Exception {
    String response;
    try (Socket socket = new Socket("127.0.0.1", server.actualPort())) {
      socket.setSoTimeout(5000);
      socket
          .getOutputStream()
          .write(
              "GET /created HTTP/1.0\r\nConnection: close\r\n\r\n"
                  .getBytes(StandardCharsets.US_ASCII));
      response = new String(socket.getInputStream().readAllBytes(), StandardCharsets.US_ASCII);
    }
    HttpAccessLog event = nextEvent();

    assertThat(response).contains("201 Created");
    assertThat(event).isEqualTo(expectedEvent("GET", 201, "/created", "HTTP/1.0", null, null));
    assertThat(event.requestHeaders()).isEmpty();
    assertThat(event.responseHeaders()).isEmpty();
  }

  @Test
  void usesIndependentStructuredAccessLoggingProperty() throws NoSuchMethodException {
    ConfigProperty property =
        HttpAccessLogFilter.class
            .getConstructor(boolean.class)
            .getParameters()[0]
            .getAnnotation(ConfigProperty.class);

    assertThat(property).isNotNull();
    assertThat(property.name()).isEqualTo("aud.http.access-log.enabled");
    assertThat(property.defaultValue()).isEqualTo("true");
  }

  @Test
  void doesNotRegisterWhenDisabled() {
    Filters filters = new Filters();
    Router management = Router.router(vertx);
    HttpAccessLogFilter sut = new HttpAccessLogFilter(false);

    sut.register(filters);
    sut.registerManagement(() -> management);

    assertThat(filters.getFilters()).isEmpty();
    assertThat(management.getRoutes()).isEmpty();
  }

  @Test
  void preservesPresentEmptyHeaderValues() throws Exception {
    int status =
        await(
            client
                .request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/created")
                .compose(
                    request ->
                        request
                            .putHeader("User-Agent", "")
                            .putHeader("Accept", "")
                            .putHeader("Accept-Encoding", "")
                            .send())
                .compose(response -> response.body().map(ignored -> response.statusCode())));
    HttpAccessLog event = nextEvent();

    assertThat(status).isEqualTo(201);
    assertThat(event.requestHeaders())
        .containsEntry("user-agent", "")
        .containsEntry("accept", "")
        .containsEntry("accept-encoding", "");
  }

  @ParameterizedTest
  @CsvSource({"/q/health, 200", "/rejected, 400"})
  void logsCompletedManagementResponses(String path, int expectedStatus) throws Exception {
    Router management = Router.router(vertx);
    management
        .route("/rejected")
        .order(Integer.MIN_VALUE + 1)
        .handler(context -> context.response().setStatusCode(400).end());
    new HttpAccessLogFilter(true).registerManagement(() -> management);
    management.route("/q/health").handler(context -> context.response().end("healthy"));
    HttpServer managementServer =
        await(vertx.createHttpServer().requestHandler(management).listen(0, "127.0.0.1"));

    try {
      int status =
          await(
              client
                  .request(HttpMethod.GET, managementServer.actualPort(), "127.0.0.1", path)
                  .compose(
                      request ->
                          request
                              .putHeader("Host", "management.example")
                              .putHeader("User-Agent", "management-client/1.0")
                              .send())
                  .compose(response -> response.body().map(ignored -> response.statusCode())));
      HttpAccessLog event = nextEvent();

      assertThat(status).isEqualTo(expectedStatus);
      assertThat(event)
          .isEqualTo(
              expectedEvent(
                  "GET", expectedStatus, path, "management.example", "management-client/1.0"));
    } finally {
      await(managementServer.close());
    }
  }

  @Test
  void warnsWhenResponseDoesNotComplete() throws Exception {
    Future<Integer> response = request(HttpMethod.GET, "/aborted", "public.example");

    assertThatThrownBy(() -> await(response)).hasCauseInstanceOf(Exception.class);
    ExtLogRecord logRecord = EVENTS.poll(5, TimeUnit.SECONDS);

    assertThat(logRecord).isNotNull();
    assertThat(logRecord.getLevel().intValue()).isEqualTo(Level.WARNING.intValue());
    assertThat(logRecord.getThrown()).hasMessageContaining("closed");
    assertThat(logRecord.getParameters())
        .containsExactly(expectedEvent("GET", 200, "/aborted", "public.example", null));
  }

  @Test
  void capturesRequestHeadersAndFinalCompressedResponseHeaders() throws Exception {
    HttpClientResponse response =
        await(
            client
                .request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/encoded")
                .compose(
                    request ->
                        request
                            .putHeader("Host", "public.example")
                            .putHeader("User-Agent", "schema-client/1.0")
                            .putHeader("Accept", "application/json")
                            .putHeader("Accept-Encoding", "gzip")
                            .send())
                .compose(reply -> reply.body().map(ignored -> reply)));
    HttpAccessLog event = nextEvent();

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.getHeader("Content-Encoding")).isEqualTo("gzip");
    assertThat(response.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");
    assertThat(event)
        .isEqualTo(
            new HttpAccessLog(
                "GET",
                200,
                "/encoded",
                "HTTP/1.1",
                Map.of(
                    HttpHeaders.HOST.toString(), "public.example",
                    HttpHeaders.USER_AGENT.toString(), "schema-client/1.0",
                    HttpHeaders.ACCEPT.toString(), "application/json",
                    HttpHeaders.ACCEPT_ENCODING.toString(), "gzip"),
                Map.of(
                    HttpHeaders.CONTENT_ENCODING.toString(), "gzip",
                    HttpHeaders.CONTENT_TYPE.toString(), "application/json; charset=utf-8")));
  }

  private static HttpAccessLog expectedEvent(
      String method, int status, String uri, @Nullable String host, @Nullable String userAgent) {
    return expectedEvent(method, status, uri, "HTTP/1.1", host, userAgent);
  }

  private static HttpAccessLog expectedEvent(
      String method,
      int status,
      String uri,
      String proto,
      @Nullable String host,
      @Nullable String userAgent) {
    Map<String, String> requestHeaders = new LinkedHashMap<>();
    if (host != null) {
      requestHeaders.put(HttpHeaders.HOST.toString(), host);
    }
    if (userAgent != null) {
      requestHeaders.put(HttpHeaders.USER_AGENT.toString(), userAgent);
    }
    return new HttpAccessLog(method, status, uri, proto, requestHeaders, Map.of());
  }

  private static Map<String, String> expectedResponseHeaders(HttpClientResponse response) {
    Map<String, String> headers = new LinkedHashMap<>();
    @Nullable String encoding = response.getHeader(HttpHeaders.CONTENT_ENCODING);
    @Nullable String type = response.getHeader(HttpHeaders.CONTENT_TYPE);
    if (encoding != null) {
      headers.put(HttpHeaders.CONTENT_ENCODING.toString(), encoding);
    }
    if (type != null) {
      headers.put(HttpHeaders.CONTENT_TYPE.toString(), type);
    }
    return headers;
  }

  @ParameterizedTest
  @CsvSource({"HTTP_1_0, HTTP/1.0", "HTTP_1_1, HTTP/1.1", "HTTP_2, HTTP/2"})
  void capturesNegotiatedHttpProtocol(HttpVersion version, String expectedProto) throws Exception {
    HttpClient protocolClient =
        vertx.createHttpClient(
            new HttpClientOptions().setProtocolVersion(version).setHttp2ClearTextUpgrade(false));

    try {
      HttpClientResponse response =
          await(
              protocolClient
                  .request(HttpMethod.GET, server.actualPort(), "127.0.0.1", "/created")
                  .compose(HttpClientRequest::send)
                  .compose(reply -> reply.body().map(ignored -> reply)));
      HttpAccessLog event = nextEvent();

      assertThat(response.version()).isEqualTo(version);
      assertThat(response.statusCode()).isEqualTo(201);
      assertThat(event.proto()).isEqualTo(expectedProto);
      assertThat(EVENTS).isEmpty();
    } finally {
      await(protocolClient.close());
    }
  }

  private static Future<Integer> request(HttpMethod method, String path, String host) {
    return request(method, path, host, null);
  }

  private static Future<Integer> request(
      HttpMethod method, String path, String host, @Nullable String userAgent) {
    return exchange(method, path, host, userAgent).map(HttpClientResponse::statusCode);
  }

  private static Future<HttpClientResponse> exchange(
      HttpMethod method, String path, String host, @Nullable String userAgent) {
    return client
        .request(method, server.actualPort(), "127.0.0.1", path)
        .compose(
            (HttpClientRequest request) -> {
              request.putHeader("Host", host);
              if (userAgent != null) {
                request.putHeader("User-Agent", userAgent);
              }
              return request.send();
            })
        .compose(response -> response.body().map(ignored -> response));
  }

  private static HttpAccessLog nextEvent() throws InterruptedException {
    ExtLogRecord logRecord = EVENTS.poll(5, TimeUnit.SECONDS);
    assertThat(logRecord).as("completed response access event").isNotNull();
    assertThat(logRecord.getLoggerName()).isEqualTo(HttpAccessLogFilter.LOG_CATEGORY);
    assertThat(logRecord.getLevel()).isEqualTo(Level.INFO);
    assertThat(logRecord.getParameters()).hasSize(1);
    HttpAccessLog event = (HttpAccessLog) logRecord.getParameters()[0];
    assertThat(event.requestHeaders()).doesNotContainValue(null);
    assertThat(event.responseHeaders()).doesNotContainValue(null);
    assertThat(new PatternFormatter("%m").format(logRecord))
        .isEqualTo("%s %s: %d".formatted(event.method(), event.uri(), event.status()));
    return event;
  }

  private static <T> T await(Future<T> future) throws Exception {
    return future.toCompletionStage().toCompletableFuture().get(5, TimeUnit.SECONDS);
  }
}

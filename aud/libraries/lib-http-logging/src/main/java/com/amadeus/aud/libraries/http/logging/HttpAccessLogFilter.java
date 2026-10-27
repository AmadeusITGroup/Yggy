package com.amadeus.aud.libraries.http.logging;

import io.quarkus.vertx.http.ManagementInterface;
import io.quarkus.vertx.http.runtime.RouteConstants;
import io.quarkus.vertx.http.runtime.filters.Filters;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HttpAccessLogFilter {
  static final String LOG_CATEGORY = "com.amadeus.aud.http.access";
  private static final String REGISTERED = HttpAccessLogFilter.class.getName();
  private static final Logger LOG = Logger.getLogger(LOG_CATEGORY);
  private final boolean enabled;

  @Inject
  public HttpAccessLogFilter(
      @ConfigProperty(name = "aud.http.access-log.enabled", defaultValue = "true")
          boolean enabled) {
    this.enabled = enabled;
  }

  void register(@Observes Filters filters) {
    if (enabled) {
      filters.register(this::filter, Integer.MAX_VALUE);
    }
  }

  void registerManagement(@Observes ManagementInterface management) {
    if (enabled) {
      management
          .router()
          .route()
          .order(RouteConstants.ROUTE_ORDER_ACCESS_LOG_HANDLER)
          .handler(this::filter);
    }
  }

  private void filter(RoutingContext context) {
    // Rerouting runs filters again; retain one event with the original request values.
    if (context.get(REGISTERED) == null) {
      context.put(REGISTERED, Boolean.TRUE);
      HttpServerRequest request = context.request();
      String method = request.method().name();
      String uri = request.path();
      String proto =
          switch (request.version()) {
            case HTTP_1_0 -> "HTTP/1.0";
            case HTTP_1_1 -> "HTTP/1.1";
            case HTTP_2 -> "HTTP/2";
          };
      Map<String, String> requestHeaders = new LinkedHashMap<>();
      addHeader(requestHeaders, HttpHeaders.HOST, request.getHeader(HttpHeaders.HOST));
      addHeader(requestHeaders, HttpHeaders.USER_AGENT, request.getHeader(HttpHeaders.USER_AGENT));
      addHeader(requestHeaders, HttpHeaders.ACCEPT, request.getHeader(HttpHeaders.ACCEPT));
      addHeader(
          requestHeaders,
          HttpHeaders.ACCEPT_ENCODING,
          request.getHeader(HttpHeaders.ACCEPT_ENCODING));
      context.addEndHandler(
          (AsyncResult<Void> result) -> {
            HttpServerResponse response = context.response();
            Map<String, String> responseHeaders = new LinkedHashMap<>();
            addHeader(
                responseHeaders,
                HttpHeaders.CONTENT_ENCODING,
                response.headers().get(HttpHeaders.CONTENT_ENCODING));
            addHeader(
                responseHeaders,
                HttpHeaders.CONTENT_TYPE,
                response.headers().get(HttpHeaders.CONTENT_TYPE));
            HttpAccessLog event =
                new HttpAccessLog(
                    method, response.getStatusCode(), uri, proto, requestHeaders, responseHeaders);
            if (result.succeeded()) {
              LOG.infof("%s", event);
            } else {
              LOG.warnf(result.cause(), "HTTP response did not complete: %s", event);
            }
          });
    }
    context.next();
  }

  private static void addHeader(
      Map<String, String> headers, CharSequence name, @Nullable String value) {
    if (value != null) {
      headers.put(name.toString(), value);
    }
  }
}

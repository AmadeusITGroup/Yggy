package com.amadeus.aud.services.apis.security;

import com.amadeus.aud.libraries.common.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nullable;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/// Traces inbound bearer tokens on every HTTP request without blocking.
/// Extracts JWT claims, logs token details, and emits Prometheus metrics.
/// This filter never aborts the request, it is purely observational: it exists to
/// measure which callers use which tokens before authentication is enforced.
@Provider
@PreMatching
@Priority(Priorities.AUTHENTICATION + 1)
public class TokenTracingFilter implements ContainerRequestFilter {

  private static final Logger LOG = Logger.getLogger(TokenTracingFilter.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String METRIC_REQUESTS_TOTAL = "aud_api_requests_total";
  private static final String METRIC_NO_AUTH_TOKEN_TOTAL = "aud_api_requests_no_auth_token_total";
  private static final String METRIC_TOKEN_VALIDATION = "aud_api_token_validation";
  private static final String METRIC_TOKEN_CLIENT = "aud_api_token_client";
  private static final String TAG_TOKEN_STATUS = "token_status";
  private static final String STATUS_ABSENT = "absent";
  private static final String STATUS_PRESENT = "present";
  private static final String STATUS_MALFORMED = "malformed";
  private static final String UNKNOWN = "unknown";
  private static final int MIN_JWT_PARTS = 2;
  private static final int JWT_PAYLOAD_INDEX = 1;
  private static final Set<String> PUBLIC_EXACT_PATHS = Set.of("/version");
  private static final Set<String> PUBLIC_PREFIX_PATHS =
      Set.of("/graphdump", "/health", "/q/health", "/metrics");

  private final MeterRegistry meterRegistry;
  private final boolean tracingEnabled;

  @Inject
  public TokenTracingFilter(
      MeterRegistry meterRegistry,
      @ConfigProperty(name = "aud.security.tracing.enabled", defaultValue = "true")
          boolean tracingEnabled) {
    this.meterRegistry = meterRegistry;
    this.tracingEnabled = tracingEnabled;
  }

  @Override
  public void filter(ContainerRequestContext ctx) {
    if (!tracingEnabled) {
      return;
    }

    String path = ctx.getUriInfo().getPath();
    if (isPublicPath(path)) {
      return;
    }

    String method = ctx.getMethod();
    String authHeader = ctx.getHeaderString("Authorization");

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      handleMissingToken(path, method);
      return;
    }

    String token = authHeader.substring(BEARER_PREFIX.length());
    handleToken(token, path, method);
  }

  private void handleMissingToken(String path, String method) {
    LOG.warnf("token_trace: no bearer token | path=%s method=%s", path, method);

    counter(METRIC_REQUESTS_TOTAL, TAG_TOKEN_STATUS, STATUS_ABSENT).increment();
    meterRegistry.counter(METRIC_NO_AUTH_TOKEN_TOTAL, "path", path, "method", method).increment();
  }

  private void handleToken(String token, String path, String method) {
    TokenClaims claims;
    try {
      claims = decodePayload(token);
    } catch (IllegalArgumentException | JsonProcessingException e) {
      LOG.warnf(
          "token_trace: malformed token | path=%s method=%s error=%s",
          path, method, e.getMessage());
      counter(METRIC_REQUESTS_TOTAL, TAG_TOKEN_STATUS, STATUS_MALFORMED).increment();
      return;
    }

    counter(METRIC_REQUESTS_TOTAL, TAG_TOKEN_STATUS, STATUS_PRESENT).increment();

    boolean hasReadAll = claims.hasRole(Constants.AUTH_READ_ALL_ROLE);

    meterRegistry
        .counter(
            METRIC_TOKEN_VALIDATION, "check", "roles", "result", hasReadAll ? "valid" : "invalid")
        .increment();

    String appId = claims.appId() != null ? claims.appId() : UNKNOWN;
    meterRegistry.counter(METRIC_TOKEN_CLIENT, "appid", appId).increment();

    String spId = appId;
    String caller = claims.name() != null ? claims.name() : UNKNOWN;

    if (hasReadAll) {
      LOG.debugf(
          "token_trace: valid token | path=%s method=%s oid=%s service_principal=%s caller=%s roles=%s",
          path, method, claims.oid(), spId, caller, claims.roles());
    } else {
      LOG.warnf(
          "token_trace: token missing %s role | path=%s method=%s oid=%s service_principal=%s caller=%s roles=%s scp=%s",
          Constants.AUTH_READ_ALL_ROLE,
          path,
          method,
          claims.oid(),
          spId,
          caller,
          claims.roles(),
          claims.scp());
    }
  }

  private Counter counter(String name, String tagKey, String tagValue) {
    return meterRegistry.counter(name, tagKey, tagValue);
  }

  static TokenClaims decodePayload(String token) throws JsonProcessingException {
    String[] parts = token.split("\\.");
    if (parts.length < MIN_JWT_PARTS) {
      throw new IllegalArgumentException(
          "JWT must have at least " + MIN_JWT_PARTS + " parts, got " + parts.length);
    }
    String payload = parts[JWT_PAYLOAD_INDEX];
    // JWT uses Base64url encoding
    byte[] decoded = Base64.getUrlDecoder().decode(payload);
    return TokenClaims.fromJson(new String(decoded, StandardCharsets.UTF_8));
  }

  private static boolean isPublicPath(String path) {
    if (PUBLIC_EXACT_PATHS.contains(path)) {
      return true;
    }
    for (String prefix : PUBLIC_PREFIX_PATHS) {
      if (path.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  record TokenClaims(
      @Nullable String appId,
      @Nullable String oid,
      @Nullable String name,
      List<String> roles,
      @Nullable String scp) {

    boolean hasRole(String role) {
      return roles != null && roles.contains(role);
    }

    static TokenClaims fromJson(String json) throws JsonProcessingException {
      JsonNode node = MAPPER.readTree(json);
      String appid = textOrNull(node, "appid");
      String appId = appid != null ? appid : textOrNull(node, "azp");
      return new TokenClaims(
          appId,
          textOrNull(node, "oid"),
          textOrNull(node, "name"),
          extractStringArray(node, "roles"),
          textOrNull(node, "scp"));
    }

    private static @Nullable String textOrNull(JsonNode node, String field) {
      JsonNode value = node.get(field);
      return value != null && value.isTextual() ? value.asText() : null;
    }

    private static List<String> extractStringArray(JsonNode node, String field) {
      JsonNode value = node.get(field);
      if (value == null || !value.isArray()) {
        return List.of();
      }
      ArrayList<String> result = new ArrayList<>();
      for (JsonNode element : value) {
        if (element.isTextual()) {
          result.add(element.asText());
        }
      }
      return List.copyOf(result);
    }
  }
}

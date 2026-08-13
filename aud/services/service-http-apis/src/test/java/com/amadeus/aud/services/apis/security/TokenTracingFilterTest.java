package com.amadeus.aud.services.apis.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.amadeus.aud.services.apis.security.TokenTracingFilter.TokenClaims;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TokenTracingFilterTest {

  private SimpleMeterRegistry meterRegistry;
  private TokenTracingFilter sut;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    sut = new TokenTracingFilter(meterRegistry, true);
  }

  private ContainerRequestContext mockRequest(String path, String method, String authHeader) {
    ContainerRequestContext ctx = mock(ContainerRequestContext.class);
    UriInfo uriInfo = mock(UriInfo.class);
    doReturn(path).when(uriInfo).getPath();
    doReturn(uriInfo).when(ctx).getUriInfo();
    doReturn(method).when(ctx).getMethod();
    doReturn(authHeader).when(ctx).getHeaderString("Authorization");
    return ctx;
  }

  private static String buildJwt(String payloadJson) {
    String header =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{}".getBytes(StandardCharsets.UTF_8));
    String payload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
    String signature =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("sig".getBytes(StandardCharsets.UTF_8));
    return header + "." + payload + "." + signature;
  }

  @Nested
  class WhenDisabled {
    @Test
    void doesNothing() {
      MeterRegistry reg = mock(MeterRegistry.class);
      TokenTracingFilter disabledSut = new TokenTracingFilter(reg, false);

      ContainerRequestContext ctx = mockRequest("/target/foo", "GET", null);
      disabledSut.filter(ctx);

      verifyNoInteractions(reg);
    }
  }

  @Nested
  class PublicPaths {
    static Stream<String> publicPaths() {
      return Stream.of("/version", "/q/health/ready", "/metrics");
    }

    @ParameterizedTest
    @MethodSource("publicPaths")
    void skipsPublicEndpoint(String path) {
      ContainerRequestContext ctx = mockRequest(path, "GET", null);
      sut.filter(ctx);
      assertThat(counterValue("aud_api_requests_total")).isZero();
    }

    @Test
    void versionedApiPathIsNotPublic() {
      ContainerRequestContext ctx =
          mockRequest("/version/1.0.35499/platform/IVN/kubernetes-objects", "GET", null);
      sut.filter(ctx);
      assertThat(counterValue("aud_api_requests_total", "token_status", "absent")).isOne();
    }
  }

  @Nested
  class MissingToken {
    @Test
    void incrementsNoTokenCounter() {
      ContainerRequestContext ctx = mockRequest("/target/foo", "GET", null);
      sut.filter(ctx);

      assertThat(counterValue("aud_api_requests_total", "token_status", "absent")).isOne();
      assertThat(
              counterValue(
                  "aud_api_requests_no_auth_token_total", "path", "/target/foo", "method", "GET"))
          .isOne();
    }

    @Test
    void countsNonBearerAuthAsAbsent() {
      ContainerRequestContext ctx = mockRequest("/target/foo", "GET", "Basic dXNlcjpwYXNz");
      sut.filter(ctx);

      assertThat(counterValue("aud_api_requests_total", "token_status", "absent")).isOne();
    }
  }

  @Nested
  class MalformedToken {
    @Test
    void countsMalformedToken() {
      ContainerRequestContext ctx = mockRequest("/target/foo", "GET", "Bearer not-a-jwt");
      sut.filter(ctx);

      assertThat(counterValue("aud_api_requests_total", "token_status", "malformed")).isOne();
    }
  }

  @Nested
  class ValidToken {
    @Test
    void countsTokenWithReadAllRole() {
      String payload =
          """
          {
            "iss": "https://login.microsoftonline.com/tid/v2.0",
            "aud": "6a4db7a0-f3c8-4b9c-b966-2d07ccbecdb9",
            "appid": "client-app-123",
            "oid": "object-id-456",
            "sub": "subject-789",
            "tid": "7d7761c0-8b7a-49bb-8975-a6aa1be7c38b",
            "roles": ["Read.All"],
            "scp": "user_impersonation"
          }
          """;
      ContainerRequestContext ctx =
          mockRequest("/target/foo", "GET", "Bearer " + buildJwt(payload));
      sut.filter(ctx);

      assertThat(counterValue("aud_api_requests_total", "token_status", "present")).isOne();
      assertThat(counterValue("aud_api_token_validation", "check", "roles", "result", "valid"))
          .isOne();
      assertThat(counterValue("aud_api_token_client", "appid", "client-app-123")).isOne();
    }

    @Test
    void countsTokenMissingReadAllRole() {
      String payload =
          """
          {
            "iss": "https://sts.windows.net/tid/",
            "aud": "6a4db7a0-f3c8-4b9c-b966-2d07ccbecdb9",
            "azp": "legacy-client-456",
            "oid": "oid-1",
            "sub": "sub-1",
            "tid": "7d7761c0-8b7a-49bb-8975-a6aa1be7c38b",
            "roles": ["SomeOtherRole"]
          }
          """;
      ContainerRequestContext ctx =
          mockRequest("/target/bar", "POST", "Bearer " + buildJwt(payload));
      sut.filter(ctx);

      assertThat(counterValue("aud_api_requests_total", "token_status", "present")).isOne();
      assertThat(counterValue("aud_api_token_validation", "check", "roles", "result", "invalid"))
          .isOne();
      // azp should be used as fallback when appid is absent
      assertThat(counterValue("aud_api_token_client", "appid", "legacy-client-456")).isOne();
    }

    @Test
    void countsTokenWithNoRoles() {
      String payload =
          """
          {
            "iss": "https://login.microsoftonline.com/tid/v2.0",
            "aud": "test-aud",
            "appid": "no-roles-client"
          }
          """;
      ContainerRequestContext ctx =
          mockRequest("/target/baz", "GET", "Bearer " + buildJwt(payload));
      sut.filter(ctx);

      assertThat(counterValue("aud_api_token_validation", "check", "roles", "result", "invalid"))
          .isOne();
    }
  }

  @Nested
  class TokenClaimsParsing {
    @Test
    void parsesAllClaims() throws Exception {
      String json =
          """
          {
            "appid": "app123",
            "oid": "oid456",
            "name": "John Doe",
            "roles": ["Read.All", "Write.All"],
            "scp": "user_impersonation openid"
          }
          """;
      TokenClaims claims = TokenClaims.fromJson(json);

      assertThat(claims.appId()).isEqualTo("app123");
      assertThat(claims.oid()).isEqualTo("oid456");
      assertThat(claims.name()).isEqualTo("John Doe");
      assertThat(claims.roles()).containsExactly("Read.All", "Write.All");
      assertThat(claims.scp()).isEqualTo("user_impersonation openid");
      assertThat(claims.hasRole("Read.All")).isTrue();
      assertThat(claims.hasRole("Admin")).isFalse();
    }

    @Test
    void fallsBackToAzpWhenAppidMissing() throws Exception {
      String json =
          """
          {"azp": "azp-client"}
          """;
      TokenClaims claims = TokenClaims.fromJson(json);
      assertThat(claims.appId()).isEqualTo("azp-client");
    }

    @Test
    void handlesEmptyRolesArray() throws Exception {
      String json =
          """
          {"roles": []}
          """;
      TokenClaims claims = TokenClaims.fromJson(json);
      assertThat(claims.roles()).isEmpty();
      assertThat(claims.hasRole("Read.All")).isFalse();
    }

    @Test
    void handlesMissingFields() throws Exception {
      TokenClaims claims = TokenClaims.fromJson("{}");
      assertThat(claims.appId()).isNull();
      assertThat(claims.oid()).isNull();
      assertThat(claims.name()).isNull();
      assertThat(claims.roles()).isEmpty();
      assertThat(claims.scp()).isNull();
    }
  }

  private double counterValue(String name, String... tags) {
    Counter counter = meterRegistry.find(name).tags(tags).counter();
    return counter != null ? counter.count() : 0.0;
  }
}

package com.amadeus.aud.libraries.engine.data.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UrlUtilsTest {

  @Nested
  class ExtractScheme {
    @Test
    void withHttpsScheme() {
      String url = "https://example.com/path";

      Optional<String> scheme = UrlUtils.extractScheme(url);

      assertThat(scheme).hasValue("https");
    }

    @Test
    void withHttpScheme() {
      String url = "http://example.com";

      Optional<String> scheme = UrlUtils.extractScheme(url);

      assertThat(scheme).hasValue("http");
    }

    @Test
    void withWssScheme() {
      String url = "wss://example.com:8080/socket";

      Optional<String> scheme = UrlUtils.extractScheme(url);

      assertThat(scheme).hasValue("wss");
    }

    @Test
    void withWildcardUrl() {
      String url = "https://*.example.com";

      Optional<String> scheme = UrlUtils.extractScheme(url);

      assertThat(scheme).hasValue("https");
    }

    @Test
    void withNoScheme() {
      String url = "example.com";

      Optional<String> scheme = UrlUtils.extractScheme(url);

      assertThat(scheme).isEmpty();
    }

    @Test
    void withComplexUrl() {
      String url = "https://user@example.com:8080/path?query=value#fragment";

      Optional<String> scheme = UrlUtils.extractScheme(url);

      assertThat(scheme).hasValue("https");
    }
  }

  @Nested
  class ExtractHostname {
    @Test
    void withSimpleUrl() {
      String url = "https://example.com";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withUrlAndPath() {
      String url = "https://example.com/path/to/resource";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withUrlAndPort() {
      String url = "https://example.com:8080";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withUrlPortAndPath() {
      String url = "https://example.com:8080/path";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withUrlAndQuery() {
      String url = "https://example.com?param=value";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withUrlAndFragment() {
      String url = "https://example.com#section";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withUserInfo() {
      String url = "https://user:password@example.com";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withUserInfoAndPort() {
      String url = "https://user@example.com:8080/path";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("example.com");
    }

    @Test
    void withWildcard() {
      String url = "https://*.example.com";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("*.example.com");
    }

    @Test
    void withWildcardAndPath() {
      String url = "https://*.example.com/path";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("*.example.com");
    }

    @Test
    void withSubdomain() {
      String url = "https://api.example.com";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("api.example.com");
    }

    @Test
    void withIpAddress() {
      String url = "https://192.168.1.1";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("192.168.1.1");
    }

    @Test
    void withIpAddressAndPort() {
      String url = "https://192.168.1.1:8080/path";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("192.168.1.1");
    }

    @Test
    void withNoScheme() {
      String url = "example.com";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).isEmpty();
    }

    @Test
    void withNoSchemeAndPath() {
      String url = "example.com/hello";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).isEmpty();
    }

    @Test
    void withComplexUrl() {
      String url = "https://user@api.example.com:8080/v1/resource?key=value#section";

      Optional<String> hostname = UrlUtils.extractHostname(url);

      assertThat(hostname).hasValue("api.example.com");
    }
  }
}

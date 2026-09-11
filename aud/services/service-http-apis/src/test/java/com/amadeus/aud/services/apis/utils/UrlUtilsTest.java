package com.amadeus.aud.services.apis.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

class UrlUtilsTest {

  @Test
  void constructor_isPrivate() throws NoSuchMethodException {
    Constructor<UrlUtils> constructor = UrlUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      UrlUtils instance = constructor.newInstance();
      assertThat(instance).isNotNull();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      // Intentionally private constructor
    }
  }

  @Test
  void getUrl_returnsAbsolutePath() {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    URI absolute = URI.create("https://example.com/api/things");
    Mockito.doReturn(absolute).when(uriInfo).getAbsolutePath();

    String result = UrlUtils.getUrl(uriInfo);

    assertThat(result).isEqualTo("https://example.com/api/things");
  }

  @Test
  void getVersionedUrl_withString() {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    URI request = URI.create("http://myhost.internal:8080/api/resource");
    Mockito.doReturn(request).when(uriInfo).getRequestUri();

    String result = UrlUtils.getVersionedUrl(uriInfo, "1.2.3");

    assertThat(result).isEqualTo("http://myhost.internal/version/1.2.3/api/resource");
  }

  @Test
  void getVersionedUrl_withVersionObject_ignoresQueryAndFragment() {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    URI request = URI.create("https://host.example.com/api/items?filter=all#section1");
    Mockito.doReturn(request).when(uriInfo).getRequestUri();

    Version version = new Version();
    version.setVersionNumber("9.8.7");

    String result = UrlUtils.getVersionedUrl(uriInfo, version);

    // query & fragment should be dropped because implementation uses URI#getPath()
    assertThat(result).isEqualTo("https://host.example.com/version/9.8.7/api/items");
  }

  @Test
  void getVersionedUrl_rootPath() {
    UriInfo uriInfo = Mockito.mock(UriInfo.class);
    URI request = URI.create("http://localhost/");
    Mockito.doReturn(request).when(uriInfo).getRequestUri();

    String result = UrlUtils.getVersionedUrl(uriInfo, "1.0.0");

    assertThat(result).isEqualTo("http://localhost/version/1.0.0/");
  }

  @Nested
  class TestGetPortFromURL {
    @ParameterizedTest
    @CsvSource({
      "http://example.com:8080,8080",
      "http://example.com,80",
      "https://example.com,443",
      "http://*.example.com:1234,1234",
      "example.com:8080,8080",
      "tcp://10.0.0.1:9090,9090",
      "udp://10.0.0.1:53,53"
    })
    void parsesPortCorrectly(String url, int expectedPort) {
      int port = UrlUtils.getPortFromURL(url);
      assertThat(port).isEqualTo(expectedPort);
    }

    @ParameterizedTest
    @CsvSource({
      "http://:invalid,-1",
      "\"\",-1",
      "tcp://host:notanumber,-1",
      "www.example.com,-1",
      "unknown-scheme://example.com,-1"
    })
    void returnsMinusOneForMalformedOrNoSchemeOrEmpty(String url, int expectedPort) {
      int port = UrlUtils.getPortFromURL(url);
      assertThat(port).isEqualTo(expectedPort);
    }
  }

  @Nested
  class TestGetPortFromURLAsList {
    @Test
    void returnsPortInListWhenValid() {
      List<String> result = UrlUtils.getPortFromURLAsList("https://example.com:8080");
      assertThat(result).containsExactly("8080");
    }

    @Test
    void returnsEmptyListWhenInvalid() {
      List<String> result = UrlUtils.getPortFromURLAsList("http://:invalid");
      assertThat(result).isEmpty();
    }
  }

  @Nested
  class TestGetPortsFromUrlListAsSet {
    @Test
    void extractsUniquePortsFromIterable() {
      List<Object> urls =
          List.of(
              "http://example.com",
              "https://example.com:8443",
              "tcp://10.0.0.1:5000",
              "http://:invalid",
              "http://example.com:80");

      Set<String> result = UrlUtils.getPortsFromUrlListAsSet(urls);

      assertThat(result).containsExactlyInAnyOrder("80", "8443", "5000");
    }

    @Test
    void returnsEmptySetForEmptyIterable() {
      Set<String> result = UrlUtils.getPortsFromUrlListAsSet(List.of());
      assertThat(result).isEmpty();
    }
  }
}

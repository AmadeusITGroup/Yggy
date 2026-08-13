package com.amadeus.aud.libraries.common;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UrlUtilsTest {

  @Test
  void getUrlShouldReturnAbsolutePath() {
    UriInfo uriInfo = uriInfoWithAbsolutePath("https://example.com/api/resources");

    String result = UrlUtils.getUrl(uriInfo);

    assertThat(result).isEqualTo("https://example.com/api/resources");
  }

  @Test
  void getVersionedUrlShouldBuildUrlWithVersionBeforeRequestPath() {
    UriInfo uriInfo = uriInfoWithRequestUri("https://example.com/api/resources?name=test");

    String result = UrlUtils.getVersionedUrl(uriInfo, "42");

    assertThat(result).isEqualTo("https://example.com/version/42/api/resources");
  }

  @ParameterizedTest
  @CsvSource({
    "http://example.com:8080/api/resources, 8080",
    "https://example.com/api/resources, 443",
    "example.com/api/resources, 443",
    "tcp://example.com:8443, 8443",
    "udp://example.com:53, 53",
    "https://example.com:invalid, -1"
  })
  void getPortFromURLShouldReturnExpectedPort(String url, int expectedPort) {
    int result = UrlUtils.getPortFromURL(url);

    assertThat(result).isEqualTo(expectedPort);
  }

  @Test
  void getPortFromURLAsListShouldReturnSinglePortAsString() {
    String url = "http://example.com:8080/api/resources";

    List<String> result = UrlUtils.getPortFromURLAsList(url);

    assertThat(result).containsExactly("8080");
  }

  @Test
  void getPortFromURLAsListShouldReturnEmptyListWhenPortCannotBeFound() {
    String url = "unknown://example.com/api/resources";

    List<String> result = UrlUtils.getPortFromURLAsList(url);

    assertThat(result).isEmpty();
  }

  @Test
  void getPortsFromUrlListAsSetShouldReturnUniqueResolvedPorts() {
    List<Object> urls =
        List.of(
            "http://example.com/api/resources",
            "https://example.com/api/resources",
            "tcp://example.com:8443",
            "https://example.com:8443/api/resources");

    Set<String> result = UrlUtils.getPortsFromUrlListAsSet(urls);

    assertThat(result).containsExactlyInAnyOrder("80", "443", "8443");
  }

  private static UriInfo uriInfoWithAbsolutePath(String absolutePath) {
    return new TestUriInfo(URI.create(absolutePath), URI.create("https://unused.example"));
  }

  private static UriInfo uriInfoWithRequestUri(String requestUri) {
    return new TestUriInfo(URI.create("https://unused.example"), URI.create(requestUri));
  }

  private record TestUriInfo(URI absolutePath, URI requestUri) implements UriInfo {

    @Override
    public String getPath() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getPath(boolean decode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<PathSegment> getPathSegments() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public URI getRequestUri() {
      return requestUri;
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
      throw new UnsupportedOperationException();
    }

    @Override
    public URI getAbsolutePath() {
      return absolutePath;
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
      throw new UnsupportedOperationException();
    }

    @Override
    public URI getBaseUri() {
      throw new UnsupportedOperationException();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
      throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters() {
      throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
      throw new UnsupportedOperationException();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMatchedURIs() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> getMatchedResources() {
      throw new UnsupportedOperationException();
    }

    @Override
    public URI resolve(URI uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public URI relativize(URI uri) {
      throw new UnsupportedOperationException();
    }
  }
}

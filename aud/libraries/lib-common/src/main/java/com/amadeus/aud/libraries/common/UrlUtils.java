package com.amadeus.aud.libraries.common;

import io.quarkus.logging.Log;
import jakarta.ws.rs.core.UriInfo;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class UrlUtils {

  private UrlUtils() {}

  public static String getUrl(UriInfo uriInfo) {
    return uriInfo.getAbsolutePath().toString();
  }

  public static String getVersionedUrl(UriInfo uriInfo, String versionNumber) {
    URI uri = uriInfo.getRequestUri();
    return new StringBuffer()
        .append(uri.getScheme())
        .append("://")
        .append(uri.getHost())
        .append("/version/")
        .append(versionNumber)
        .append(uri.getPath())
        .toString();
  }

  public static int getPortFromURL(String url) {
    try {
      if (url.startsWith("tcp://") || url.startsWith("udp://")) {
        url = url.substring(6);
        String[] parts = url.split(":", 2);
        if (parts[1] != null) {
          return Integer.parseInt(parts[1]);
        }
      }
      // Wildcard is not a valid URI character
      if (!url.startsWith("www.") && !url.contains("://")) {
        url = "https://" + url;
      }
      URI parsed = new URI(url.replace("*", "star"));
      int parsedPort;
      if (parsed.getPort() > 0) {
        parsedPort = parsed.getPort();
      } else {
        parsedPort = parsed.toURL().getDefaultPort();
      }

      if (parsedPort <= 0) {
        Log.debugf("Could not find the port for URL or scheme from %s", url);
        return -1;
      } else {
        return parsedPort;
      }
    } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
      Log.debugf("The URL is malformed: %s %s", url, e.getMessage());
      return -1;
    }
  }

  public static List<String> getPortFromURLAsList(String url) {
    List<String> list = new ArrayList<>();
    int port = UrlUtils.getPortFromURL(url);
    if (port != -1) {
      list.add(String.valueOf(port));
    }
    return list;
  }

  public static Set<String> getPortsFromUrlListAsSet(Iterable<Object> urlList) {
    Set<String> ports = new HashSet<>();
    for (Object url : urlList) {
      int port = UrlUtils.getPortFromURL(url.toString());
      if (port != -1) {
        ports.add(String.valueOf(port));
      }
    }
    return ports;
  }
}

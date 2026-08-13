package com.amadeus.aud.libraries.models.core.utils;

import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class EndpointUtils {
  private EndpointUtils() {}

  public static Set<com.amadeus.aud.libraries.yaml.models.Endpoint> deepCopyEndpoints(
      List<com.amadeus.aud.libraries.yaml.models.Endpoint> endpointsToCopy) {
    Set<com.amadeus.aud.libraries.yaml.models.Endpoint> endpoints = new HashSet<>();
    for (com.amadeus.aud.libraries.yaml.models.Endpoint endpoint : endpointsToCopy) {
      com.amadeus.aud.libraries.yaml.models.Endpoint yamlEndpoint =
          new com.amadeus.aud.libraries.yaml.models.Endpoint();
      yamlEndpoint.setValue(endpoint.getValue());
      yamlEndpoint.setType(endpoint.getType());
      yamlEndpoint.setDescription(endpoint.getDescription());
      endpoints.add(yamlEndpoint);
    }
    return endpoints;
  }

  public static com.amadeus.aud.libraries.yaml.models.Endpoint translateDataEndpointToYamlEndpoint(
      Endpoint endpointToCopy) {
    com.amadeus.aud.libraries.yaml.models.Endpoint yamlEndpoint =
        new com.amadeus.aud.libraries.yaml.models.Endpoint();
    yamlEndpoint.setValue(endpointToCopy.getValue().toLowerCase(Locale.ROOT));
    yamlEndpoint.setType(endpointToCopy.getType().toString());
    yamlEndpoint.setDescription(endpointToCopy.getDescription());
    return yamlEndpoint;
  }

  private static void addRangeToRangeSet(RangeSet<BigInteger> ipNetworks, String ip) {
    try {
      if (ip != null) {
        ipNetworks.add(IPv4Utils.toRange(ip));
      }
    } catch (UnsupportedOperationException e) {
      Log.debugf("Unknown Host: " + e.getMessage());
    }
  }

  private static void addMergedEndpoints(
      Set<com.amadeus.aud.libraries.yaml.models.Endpoint> mergedCidrEndpoints,
      RangeSet<BigInteger> ipNetworks) {
    for (Range<BigInteger> ipNetwork : ipNetworks.asRanges()) {
      mergedCidrEndpoints.addAll(EndpointUtils.createEndpoints(ipNetwork));
    }
  }

  public static List<com.amadeus.aud.libraries.yaml.models.Endpoint>
      translateDataEndpointsToYamlEndpoints(Set<Endpoint> endpointsToCopy) {
    List<com.amadeus.aud.libraries.yaml.models.Endpoint> endpoints = new ArrayList<>();
    for (Endpoint endpoint : endpointsToCopy) {
      endpoints.add(translateDataEndpointToYamlEndpoint(endpoint));
    }
    return endpoints;
  }

  public static List<com.amadeus.aud.libraries.yaml.models.Endpoint> createEndpoints(
      Range<BigInteger> range) {
    List<com.amadeus.aud.libraries.yaml.models.Endpoint> endpointList = new ArrayList<>();
    for (String endpointIp : IPv4Utils.toStrings(range)) {
      com.amadeus.aud.libraries.yaml.models.Endpoint yamlEndpoint =
          new com.amadeus.aud.libraries.yaml.models.Endpoint();
      yamlEndpoint.setType("cidr");
      yamlEndpoint.setValue(endpointIp);
      yamlEndpoint.setDescription(endpointIp);
      endpointList.add(yamlEndpoint);
    }
    return endpointList;
  }

  public static Collection<com.amadeus.aud.libraries.yaml.models.Endpoint> mergeCidrs(
      @Nullable List<com.amadeus.aud.libraries.yaml.models.Endpoint> endpoints) {
    // merge consecutive CIDRs if possible
    if (endpoints == null) {
      return Collections.emptyList();
    }

    RangeSet<BigInteger> ipNetworks = TreeRangeSet.create();
    Set<com.amadeus.aud.libraries.yaml.models.Endpoint> mergedCidrEndpoints = new HashSet<>();
    for (com.amadeus.aud.libraries.yaml.models.Endpoint endpoint : endpoints) {
      if ("cidr".equals(endpoint.getType())) {
        addRangeToRangeSet(ipNetworks, endpoint.getValue());
      } else {
        mergedCidrEndpoints.add(endpoint);
      }
    }
    addMergedEndpoints(mergedCidrEndpoints, ipNetworks);

    return mergedCidrEndpoints;
  }

  public static Collection<com.amadeus.aud.libraries.yaml.models.Endpoint> mergeCidrsFromData(
      @Nullable Collection<Endpoint> endpoints) {
    // merge consecutive CIDRs if possible
    if (endpoints == null) {
      return Collections.emptyList();
    }

    RangeSet<BigInteger> ipNetworks = TreeRangeSet.create();
    Set<com.amadeus.aud.libraries.yaml.models.Endpoint> mergedCidrEndpoints = new HashSet<>();
    for (Endpoint endpoint : endpoints) {
      if (Objects.equals(endpoint.getType(), Endpoint.TypeEnum.CIDR)) {
        addRangeToRangeSet(ipNetworks, endpoint.getValue());
      } else {
        mergedCidrEndpoints.add(translateDataEndpointToYamlEndpoint(endpoint));
      }
    }
    addMergedEndpoints(mergedCidrEndpoints, ipNetworks);

    return mergedCidrEndpoints;
  }
}

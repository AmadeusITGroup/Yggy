package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.amadeus.aud.libraries.yaml.models.Endpoint;
import com.google.common.collect.Range;
import jakarta.annotation.Nullable;
import java.math.BigInteger;
import org.eclipse.microprofile.graphql.Input;

@Input("EndpointFilter")
public class EndpointFilter {
  private @Nullable String type;
  private @Nullable String value;

  public boolean matches(@Nullable Endpoint endpoint) {
    if (endpoint == null) {
      return false;
    }
    if (type != null
        && (endpoint.getType() == null || !endpoint.getType().equalsIgnoreCase(type))) {
      return false;
    }
    if (value != null) {
      if (endpoint.getValue() == null) {
        return false;
      }
      // For CIDR type, check if ranges overlap
      if ("cidr".equalsIgnoreCase(type) || "cidr".equalsIgnoreCase(endpoint.getType())) {
        return cidrMatches(value, endpoint.getValue());
      }
      // For other types, use simple contains check
      if (!endpoint.getValue().contains(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if CIDR ranges match. Returns true if the filter CIDR contains (encloses) the endpoint
   * CIDR. This ensures we only match endpoints that are within the specified filter range.
   */
  private boolean cidrMatches(String filterCidr, String endpointCidr) {
    try {
      Range<BigInteger> filterRange = IPv4Utils.toRange(filterCidr);
      Range<BigInteger> endpointRange = IPv4Utils.toRange(endpointCidr);

      if (filterRange == null || endpointRange == null) {
        return false;
      }

      // Only match if the filter range contains/encloses the endpoint range
      // This means the endpoint CIDR is within the filter CIDR
      return filterRange.encloses(endpointRange);
    } catch (Exception e) {
      // If CIDR parsing fails, fall back to string contains
      return endpointCidr.contains(filterCidr);
    }
  }

  public @Nullable String getType() {
    return type;
  }

  public void setType(@Nullable String type) {
    this.type = type;
  }

  public @Nullable String getValue() {
    return value;
  }

  public void setValue(@Nullable String value) {
    this.value = value;
  }
}

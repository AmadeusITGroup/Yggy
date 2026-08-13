package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(IPAddressInstanceFilter.NAME)
public class IPAddressInstanceFilter {
  public static final String NAME = "IPAddressInstanceFilter";
  public static final String PROVIDER = "ipAddressInstances";
  private StringFilter name;
  private StringFilter scope;
  private StringFilter host;
  private StringFilter advertisedTo;
  private StringFilter componentInstance;

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(StringFilter scope) {
    this.scope = scope;
  }

  public void setHost(StringFilter host) {
    this.host = host;
  }

  public void setAdvertisedTo(StringFilter advertisedTo) {
    this.advertisedTo = advertisedTo;
  }

  public void setComponentInstance(StringFilter componentInstance) {
    this.componentInstance = componentInstance;
  }

  public StringFilter getName() {
    return name;
  }

  public StringFilter getScope() {
    return scope;
  }

  public StringFilter getHost() {
    return host;
  }

  public StringFilter getAdvertisedTo() {
    return advertisedTo;
  }

  public StringFilter getComponentInstance() {
    return componentInstance;
  }

  public boolean matches(@Nullable IPAddressInstance ipAddressInstance) {
    if (ipAddressInstance == null) {
      return false;
    }

    V1IPAddressInstanceSpec.AdvertisedToEnum advertisedToEnum = ipAddressInstance.getAdvertisedTo();
    String advertisedToValue = advertisedToEnum != null ? advertisedToEnum.getValue() : null;

    return (name == null || name.matches(ipAddressInstance.getName()))
        && (scope == null || scope.matches(ipAddressInstance.getScopeName()))
        && (host == null || host.matches(ipAddressInstance.getHost()))
        && (advertisedTo == null || advertisedTo.matches(advertisedToValue))
        && (componentInstance == null
            || componentInstance.matches(ipAddressInstance.getComponentInstanceName()));
  }
}

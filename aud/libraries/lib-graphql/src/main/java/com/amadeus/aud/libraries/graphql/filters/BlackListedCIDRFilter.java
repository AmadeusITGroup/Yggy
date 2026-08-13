package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(BlackListedCIDRFilter.NAME)
public class BlackListedCIDRFilter {
  public static final String NAME = "BlackListedCIDRFilter";
  public static final String PROVIDER = "blackListedCIDRs";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter cidr;
  private StringFilter transportLayer;
  private StringFilter ports;

  public StringFilter getId() {
    return id;
  }

  public void setId(StringFilter id) {
    this.id = id;
  }

  public StringFilter getName() {
    return name;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public ScopeFilter getScope() {
    return scope;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public StringFilter getCidr() {
    return cidr;
  }

  public void setCidr(StringFilter cidr) {
    this.cidr = cidr;
  }

  public StringFilter getTransportLayer() {
    return transportLayer;
  }

  public void setTransportLayer(StringFilter transportLayer) {
    this.transportLayer = transportLayer;
  }

  public StringFilter getPorts() {
    return ports;
  }

  public void setPorts(StringFilter ports) {
    this.ports = ports;
  }

  public boolean matches(@Nullable BlackListedCIDR blackListedCIDR) {
    return blackListedCIDR != null
        && FilterUtils.stringFilterMatches(id, blackListedCIDR.id(), s -> s)
        && FilterUtils.stringFilterMatches(name, blackListedCIDR.getName(), s -> s)
        && (scope == null || scope.matches(blackListedCIDR.getScope()))
        && FilterUtils.stringFilterMatches(cidr, blackListedCIDR.getCidr(), s -> s)
        && FilterUtils.anyStringFilterMatches(
            transportLayer,
            blackListedCIDR.getTransportLayer(),
            V1BlackListedCIDRSpec.TransportLayerEnum::getValue)
        && FilterUtils.anyStringFilterMatches(ports, blackListedCIDR.getPorts(), p -> p);
  }
}

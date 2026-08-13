package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURLSpec;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(BlackListedURLFilter.NAME)
public class BlackListedURLFilter {
  public static final String NAME = "BlackListedURLFilter";
  public static final String PROVIDER = "blackListedURLs";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter fqdn;
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

  public StringFilter getFqdn() {
    return fqdn;
  }

  public void setFqdn(StringFilter fqdn) {
    this.fqdn = fqdn;
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

  public boolean matches(@Nullable BlackListedURL url) {
    return url != null
        && FilterUtils.stringFilterMatches(id, url.id(), s -> s)
        && FilterUtils.stringFilterMatches(name, url.getName(), s -> s)
        && (scope == null || scope.matches(url.getScope()))
        && FilterUtils.stringFilterMatches(fqdn, url.getFqdn(), s -> s)
        && FilterUtils.anyStringFilterMatches(
            transportLayer,
            url.getTransportLayer(),
            V1BlackListedURLSpec.TransportLayerEnum::getValue)
        && FilterUtils.anyStringFilterMatches(ports, url.getPorts(), p -> p);
  }
}

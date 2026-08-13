package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(BlockedPortFilter.NAME)
public class BlockedPortFilter {
  public static final String NAME = "BlockedPortFilter";
  public static final String PROVIDER = "blockedPorts";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private Integer port;
  private StringFilter protocol;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public void setProtocol(StringFilter protocol) {
    this.protocol = protocol;
  }

  public StringFilter getId() {
    return id;
  }

  public StringFilter getName() {
    return name;
  }

  public ScopeFilter getScope() {
    return scope;
  }

  public Integer getPort() {
    return port;
  }

  public StringFilter getProtocol() {
    return protocol;
  }

  public boolean matches(@Nullable BlockedPort blockedPort) {
    return blockedPort != null
        && (id == null || id.matches(blockedPort.id()))
        && (name == null || name.matches(blockedPort.getName()))
        && (scope == null || scope.matches(blockedPort.getScope()))
        && (port == null || port.equals(blockedPort.getPort()))
        && (protocol == null
            || protocol.matches(
                blockedPort.getProtocol() != null ? blockedPort.getProtocol().getValue() : null));
  }
}

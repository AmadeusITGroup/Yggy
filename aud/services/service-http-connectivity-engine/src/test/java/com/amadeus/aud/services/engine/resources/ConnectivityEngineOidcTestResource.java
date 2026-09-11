package com.amadeus.aud.services.engine.resources;

import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import java.util.Map;

public final class ConnectivityEngineOidcTestResource extends OidcWiremockTestResource {
  @Override
  public Map<String, String> start() {
    Map<String, String> configuration = super.start();
    configuration.put("quarkus.oidc.tenant-enabled", "true");
    configuration.put(
        "quarkus.oidc.auth-server-url", configuration.get("keycloak.url") + "/realms/quarkus");
    configuration.put("quarkus.oidc.client-id", "connectivity-engine-test");
    configuration.put("quarkus.oidc.resolve-tenants-with-issuer", "false");
    return configuration;
  }
}

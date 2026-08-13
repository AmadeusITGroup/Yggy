package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallVendorsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1FirewallVendor;
import jakarta.inject.Singleton;

@Singleton
public class FirewallVendorService extends BaseService<FirewallVendor, V1FirewallVendor> {

  @Override
  protected FirewallVendorsRepository getRepository(Version version) {
    FirewallVendorsRepository firewallVendorsRepository = new FirewallVendorsRepository();
    version.setFirewallVendorsRepository(firewallVendorsRepository);
    return firewallVendorsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(
        version,
        FirewallVendor.class,
        V1FirewallVendor.class,
        (FirewallVendor firewallVendor, Scope scope) -> {
          if (scope == null) {
            return;
          }
          scope.addFirewallVendor(firewallVendor);
        });
  }
}

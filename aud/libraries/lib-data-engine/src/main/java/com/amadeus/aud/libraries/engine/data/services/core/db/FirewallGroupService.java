package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallGroupsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallVendorsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroup;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroupSpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class FirewallGroupService extends BaseService<FirewallGroup, V1FirewallGroup> {

  @Override
  protected FirewallGroupsRepository getRepository(Version version) {
    FirewallGroupsRepository firewallGroupsRepository = new FirewallGroupsRepository();
    version.setFirewallGroupsRepository(firewallGroupsRepository);
    return firewallGroupsRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    FirewallVendorsRepository firewallVendorsRepository = version.getFirewallVendorsRepository();
    if (firewallVendorsRepository == null) {
      Log.errorf("FirewallGroup Repository not found for version %s", version);
      return;
    }

    TargetsRepository targetsRepository = version.getTargetsRepository();
    if (targetsRepository == null) {
      Log.errorf("Target Repository not found for version %s", version);
      return;
    }

    init(
        version,
        FirewallGroup.class,
        V1FirewallGroup.class,
        (FirewallGroup firewallGroup, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addFirewallGroup(firewallGroup);
          linkVendorAndTarget(firewallGroup, firewallVendorsRepository, targetsRepository);
        });
  }

  private void linkVendorAndTarget(
      FirewallGroup firewallGroup,
      FirewallVendorsRepository firewallVendorsRepository,
      TargetsRepository targetsRepository) {

    V1FirewallGroupSpec spec = firewallGroup.getData().getSpec();

    String vendorName = spec.getVendor();
    if (vendorName != null && !vendorName.isEmpty()) {
      FirewallVendor firewallVendor = firewallVendorsRepository.get(vendorName);
      if (firewallVendor == null) {
        logModelWarning(
            "FirewallGroup %s references a non-existing FirewallVendor %s",
            firewallGroup.id(), vendorName);
      } else {
        firewallGroup.setFirewallVendor(firewallVendor);
        firewallVendor.addFirewallGroup(firewallGroup);
      }
    }

    String targetName = spec.getTarget();
    if (targetName != null && !targetName.isEmpty()) {
      Target target = targetsRepository.get(targetName);
      if (target == null) {
        logModelWarning(
            "FirewallGroup %s references a non-existing Target %s", firewallGroup.id(), targetName);
      } else {
        firewallGroup.setTarget(target);
        target.addFirewallGroup(firewallGroup);
      }
    }
  }
}

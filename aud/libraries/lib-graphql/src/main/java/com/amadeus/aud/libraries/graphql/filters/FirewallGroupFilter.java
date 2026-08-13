package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(FirewallGroupFilter.NAME)
public class FirewallGroupFilter {
  public static final String NAME = "FirewallGroupFilter";
  public static final String PROVIDER = "firewallGroups";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter firewallVendor;
  private StringFilter target;
  private StringFilter zoneInstanceName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public ScopeFilter getScope() {
    return scope;
  }

  public void setFirewallVendor(StringFilter firewallVendor) {
    this.firewallVendor = firewallVendor;
  }

  public void setTarget(StringFilter target) {
    this.target = target;
  }

  public void setZoneInstanceName(StringFilter zoneInstanceName) {
    this.zoneInstanceName = zoneInstanceName;
  }

  public StringFilter getId() {
    return id;
  }

  public boolean matches(@Nullable FirewallGroup firewallGroup) {
    return firewallGroup != null
        && (id == null || id.matches(firewallGroup.id()))
        && (name == null || name.matches(firewallGroup.getName()))
        && (scope == null || scope.matches(firewallGroup.getScope()))
        && (firewallVendor == null || firewallVendor.matches(firewallGroup.getFirewallVendorName()))
        && (target == null || target.matches(firewallGroup.getTargetName()))
        && FilterUtils.anyStringFilterMatches(
            zoneInstanceName, firewallGroup.getZoneInstances(), AbstractBase::getName);
  }
}

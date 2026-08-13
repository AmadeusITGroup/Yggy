package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(FirewallVendorFilter.NAME)
public class FirewallVendorFilter {
  public static final String NAME = "FirewallVendorFilter";
  public static final String PROVIDER = "firewallVendors";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private List<FirewallGroupFilter> firewallGroups;

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

  public void setFirewallGroups(List<FirewallGroupFilter> firewallGroups) {
    this.firewallGroups = firewallGroups;
  }

  public StringFilter getId() {
    return id;
  }

  public boolean matches(@Nullable FirewallVendor firewallVendor) {
    return firewallVendor != null
        && (id == null || id.matches(firewallVendor.id()))
        && (name == null || name.matches(firewallVendor.getName()))
        && (scope == null || scope.matches(firewallVendor.getScope()))
        && FilterUtils.allFilterMatch(
            firewallGroups, firewallVendor.getFirewallGroups(), FirewallGroupFilter::matches);
  }
}

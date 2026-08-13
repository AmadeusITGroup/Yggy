package com.amadeus.aud.libraries.graphql.endpoints;

import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.toPage;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.AbstractBaseMixin;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.graphql.dtos.FirewallVendorGraphQL;
import com.amadeus.aud.libraries.graphql.filters.FirewallGroupFilter;
import com.amadeus.aud.libraries.graphql.filters.FirewallVendorFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import jakarta.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class FirewallVendorGraphQLProvider {

  private final AudService audService;

  @Inject
  public FirewallVendorGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(FirewallVendorFilter.PROVIDER)
  public PageGraphQL<FirewallVendorGraphQL> getFirewallVendors(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("firewallGroups") List<FirewallGroupFilter> firewallGroupFilters,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    FirewallVendorFilter firewallVendorFilter = new FirewallVendorFilter();
    firewallVendorFilter.setId(id);
    firewallVendorFilter.setName(name);
    firewallVendorFilter.setScope(scopeFilter);
    firewallVendorFilter.setFirewallGroups(firewallGroupFilters);

    List<FirewallVendor> filteredFirewallVendors = new ArrayList<>();
    for (FirewallVendor firewallVendor :
        version.getFirewallVendorsRepository().getData().values()) {
      if (firewallVendorFilter.matches(firewallVendor)) {
        filteredFirewallVendors.add(firewallVendor);
      }
    }
    return toPage(filteredFirewallVendors, pageNumber, pageSize, FirewallVendorGraphQL::from);
  }
}

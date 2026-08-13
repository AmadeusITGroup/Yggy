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
import com.amadeus.aud.libraries.graphql.dtos.ZoneInstanceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.ZoneInstanceFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
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
public class ZoneInstanceGraphQLProvider {

  private final AudService audService;

  @Inject
  public ZoneInstanceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ZoneInstanceFilter.PROVIDER)
  public PageGraphQL<ZoneInstanceGraphQL> getZoneInstances(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("landingZoneName") StringFilter landingZoneName,
      @Name("zoneTypeName") StringFilter zoneTypeName,
      @Name("firewallGroupName") StringFilter firewallGroupName,
      @Name("isNsgNeeded") Boolean isNsgNeeded,
      @Name("subnetInstanceName") StringFilter subnetInstanceName,
      @Name("componentInstanceName") StringFilter componentInstanceName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ZoneInstanceFilter zoneInstanceFilter = new ZoneInstanceFilter();
    zoneInstanceFilter.setId(id);
    zoneInstanceFilter.setName(name);
    zoneInstanceFilter.setScope(scopeFilter);
    zoneInstanceFilter.setLandingZoneName(landingZoneName);
    zoneInstanceFilter.setZoneTypeName(zoneTypeName);
    zoneInstanceFilter.setFirewallGroupName(firewallGroupName);
    zoneInstanceFilter.setIsNsgNeeded(isNsgNeeded);
    zoneInstanceFilter.setSubnetInstanceName(subnetInstanceName);
    zoneInstanceFilter.setComponentInstanceName(componentInstanceName);
    List<ZoneInstance> filteredZoneInstances = new ArrayList<>();
    for (ZoneInstance zoneInstance : version.getZoneInstancesRepository().getData().values()) {
      if (zoneInstanceFilter.matches(zoneInstance)) {
        filteredZoneInstances.add(zoneInstance);
      }
    }
    return toPage(filteredZoneInstances, pageNumber, pageSize, ZoneInstanceGraphQL::from);
  }
}

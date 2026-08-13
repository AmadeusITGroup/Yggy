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
import com.amadeus.aud.libraries.graphql.dtos.ZoneTypeGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.ZoneTypeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
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
public class ZoneTypeGraphQLProvider {

  private final AudService audService;

  @Inject
  public ZoneTypeGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ZoneTypeFilter.PROVIDER)
  public PageGraphQL<ZoneTypeGraphQL> getZoneTypes(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("isTechnical") Boolean isTechnical,
      @Name("isReal") Boolean isReal,
      @Name("isPaas") Boolean isPaas,
      @Name("componentName") StringFilter componentName,
      @Name("connectionName") StringFilter connectionName,
      @Name("zoneInstanceName") StringFilter zoneInstanceName,
      @Name("componentInstanceName") StringFilter componentInstanceName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ZoneTypeFilter zoneTypeFilter = new ZoneTypeFilter();
    zoneTypeFilter.setId(id);
    zoneTypeFilter.setName(name);
    zoneTypeFilter.setScope(scopeFilter);
    zoneTypeFilter.setIsTechnical(isTechnical);
    zoneTypeFilter.setIsReal(isReal);
    zoneTypeFilter.setIsPaas(isPaas);
    zoneTypeFilter.setComponentName(componentName);
    zoneTypeFilter.setConnectionName(connectionName);
    zoneTypeFilter.setZoneInstanceName(zoneInstanceName);
    zoneTypeFilter.setComponentInstanceName(componentInstanceName);
    List<ZoneType> filteredZoneTypes = new ArrayList<>();
    for (ZoneType zoneType : version.getZoneTypesRepository().getData().values()) {
      if (zoneTypeFilter.matches(zoneType)) {
        filteredZoneTypes.add(zoneType);
      }
    }
    return toPage(filteredZoneTypes, pageNumber, pageSize, ZoneTypeGraphQL::from);
  }
}

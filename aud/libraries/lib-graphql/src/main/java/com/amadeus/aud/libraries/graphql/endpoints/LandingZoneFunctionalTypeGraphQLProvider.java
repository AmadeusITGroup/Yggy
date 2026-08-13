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
import com.amadeus.aud.libraries.graphql.dtos.LandingZoneFunctionalTypeGraphQL;
import com.amadeus.aud.libraries.graphql.filters.LandingZoneFilter;
import com.amadeus.aud.libraries.graphql.filters.LandingZoneFunctionalTypeFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
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
public class LandingZoneFunctionalTypeGraphQLProvider {

  private final AudService audService;

  @Inject
  public LandingZoneFunctionalTypeGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(LandingZoneFunctionalTypeFilter.PROVIDER)
  public PageGraphQL<LandingZoneFunctionalTypeGraphQL> getLandingZoneFunctionalTypes(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("isGenerated") Boolean isGenerated,
      @Name("landingZones") List<LandingZoneFilter> landingZoneFilters,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    LandingZoneFunctionalTypeFilter landingZoneFunctionalTypeFilter =
        new LandingZoneFunctionalTypeFilter();
    landingZoneFunctionalTypeFilter.setId(id);
    landingZoneFunctionalTypeFilter.setName(name);
    landingZoneFunctionalTypeFilter.setScope(scopeFilter);
    landingZoneFunctionalTypeFilter.setIsGenerated(isGenerated);
    landingZoneFunctionalTypeFilter.setLandingZones(landingZoneFilters);

    List<LandingZoneFunctionalType> filteredLandingZoneFunctionalTypes = new ArrayList<>();
    for (LandingZoneFunctionalType landingZoneFunctionalType :
        version.getLandingZoneFunctionalTypesRepository().getData().values()) {
      if (landingZoneFunctionalTypeFilter.matches(landingZoneFunctionalType)) {
        filteredLandingZoneFunctionalTypes.add(landingZoneFunctionalType);
      }
    }
    return toPage(
        filteredLandingZoneFunctionalTypes,
        pageNumber,
        pageSize,
        LandingZoneFunctionalTypeGraphQL::from);
  }
}

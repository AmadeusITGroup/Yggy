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
import com.amadeus.aud.libraries.graphql.dtos.LandingZoneGraphQL;
import com.amadeus.aud.libraries.graphql.filters.LandingZoneFilter;
import com.amadeus.aud.libraries.graphql.filters.MacroPhaseFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.TargetFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
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
public class LandingZoneGraphQLProvider {

  private final AudService audService;

  @Inject
  public LandingZoneGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(LandingZoneFilter.PROVIDER)
  public PageGraphQL<LandingZoneGraphQL> getLandingZones(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("regionName") StringFilter regionName,
      @Name("landingZoneTypeName") StringFilter landingZoneTypeName,
      @Name("macroPhase") MacroPhaseFilter macroPhaseFilter,
      @Name("functionalType") StringFilter functionalType,
      @Name("target") TargetFilter targetFilter,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    LandingZoneFilter landingZoneFilter = new LandingZoneFilter();
    landingZoneFilter.setId(id);
    landingZoneFilter.setName(name);
    landingZoneFilter.setScope(scopeFilter);
    landingZoneFilter.setTarget(targetFilter);
    landingZoneFilter.setRegionName(regionName);
    landingZoneFilter.setLandingZoneTypeName(landingZoneTypeName);
    landingZoneFilter.setMacroPhase(macroPhaseFilter);
    landingZoneFilter.setFunctionalType(functionalType);

    List<LandingZone> filteredLandingZones = new ArrayList<>();
    for (LandingZone landingZone : version.getLandingZonesRepository().getData().values()) {
      if (landingZoneFilter.matches(landingZone)) {
        filteredLandingZones.add(landingZone);
      }
    }
    return toPage(filteredLandingZones, pageNumber, pageSize, LandingZoneGraphQL::from);
  }
}

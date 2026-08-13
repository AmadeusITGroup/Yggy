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
import com.amadeus.aud.libraries.graphql.dtos.TargetGraphQL;
import com.amadeus.aud.libraries.graphql.filters.MacroPhaseFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.TargetFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Target;
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
public class TargetGraphQLProvider {

  private final AudService audService;

  @Inject
  public TargetGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(TargetFilter.PROVIDER)
  public PageGraphQL<TargetGraphQL> getTargets(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("providerName") StringFilter providerName,
      @Name("targetTypeName") StringFilter targetTypeName,
      @Name("macroPhase") MacroPhaseFilter macroPhaseFilter,
      @Name("continentName") StringFilter continentName,
      @Name("popPrimaryName") StringFilter popPrimaryName,
      @Name("popSecondaryName") StringFilter popSecondaryName,
      @Name("isCloud") Boolean isCloud,
      @Name("isAmadeus") Boolean isAmadeus,
      @Name("isAudManaged") Boolean isAudManaged,
      @Name("isEnableOfficeConnectivity") Boolean isEnableOfficeConnectivity,
      @Name("isEnableJumpServerAccess") Boolean isEnableJumpServerAccess,
      @Name("isEnableVnetToVnet") Boolean isEnableVnetToVnet,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    TargetFilter targetFilter = new TargetFilter();
    targetFilter.setId(id);
    targetFilter.setName(name);
    targetFilter.setScope(scopeFilter);
    targetFilter.setProviderName(providerName);
    targetFilter.setTargetTypeName(targetTypeName);
    targetFilter.setMacroPhase(macroPhaseFilter);
    targetFilter.setContinentName(continentName);
    targetFilter.setPopPrimaryName(popPrimaryName);
    targetFilter.setPopSecondaryName(popSecondaryName);
    targetFilter.setIsCloud(isCloud);
    targetFilter.setIsAmadeus(isAmadeus);
    targetFilter.setIsAudManaged(isAudManaged);
    targetFilter.setIsEnableOfficeConnectivity(isEnableOfficeConnectivity);
    targetFilter.setIsEnableJumpServerAccess(isEnableJumpServerAccess);
    targetFilter.setIsEnableVnetToVnet(isEnableVnetToVnet);

    List<Target> filteredTargets = new ArrayList<>();
    for (Target target : version.getTargetsRepository().getData().values()) {
      if (targetFilter.matches(target)) {
        filteredTargets.add(target);
      }
    }
    return toPage(filteredTargets, pageNumber, pageSize, TargetGraphQL::from);
  }
}

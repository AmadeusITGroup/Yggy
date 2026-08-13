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
import com.amadeus.aud.libraries.graphql.dtos.DependencyInstanceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.DependencyInstanceFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
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
public class DependencyInstanceGraphQLProvider {

  private final AudService audService;

  @Inject
  public DependencyInstanceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(DependencyInstanceFilter.PROVIDER)
  public PageGraphQL<DependencyInstanceGraphQL> getDependencyInstances(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("dependencyName") StringFilter dependencyName,
      @Name("sourceComponentInstanceName") StringFilter sourceComponentInstanceName,
      @Name("targetComponentInstanceName") StringFilter targetComponentInstanceName,
      @Name("viaComponentInstanceName") StringFilter viaComponentInstanceName,
      @Name("viaServiceName") StringFilter viaServiceName,
      @Name("isSourceAsComponent") Boolean isSourceAsComponent,
      @Name("isSourceLimitedToLegacyOnly") Boolean isSourceLimitedToLegacyOnly,
      @Name("isSourceLimitedToCloudOnly") Boolean isSourceLimitedToCloudOnly,
      @Name("isSourceLimitedToLandingZone") Boolean isSourceLimitedToLandingZone,
      @Name("isSourceLimitedToTarget") Boolean isSourceLimitedToTarget,
      @Name("isSourceLimitedToContinent") Boolean isSourceLimitedToContinent,
      @Name("isSourceLimitedToMacroPhase") Boolean isSourceLimitedToMacroPhase,
      @Name("isSourceLimitedToSelector") Boolean isSourceLimitedToSelector,
      @Name("isTargetAsComponent") Boolean isTargetAsComponent,
      @Name("isTargetLimitedToLegacyOnly") Boolean isTargetLimitedToLegacyOnly,
      @Name("isTargetLimitedToCloudOnly") Boolean isTargetLimitedToCloudOnly,
      @Name("isTargetLimitedToSourceNamespace") Boolean isTargetLimitedToSourceNamespace,
      @Name("isTargetLimitedToSourcePaas") Boolean isTargetLimitedToSourcePaas,
      @Name("isTargetLimitedToSourceSecurityZone") Boolean isTargetLimitedToSourceSecurityZone,
      @Name("isTargetLimitedToSourceApplicationPhase")
          Boolean isTargetLimitedToSourceApplicationPhase,
      @Name("isTargetLimitedToSourceLandingZone") Boolean isTargetLimitedToSourceLandingZone,
      @Name("isTargetLimitedToSourceTarget") Boolean isTargetLimitedToSourceTarget,
      @Name("isTargetLimitedToSourceContinent") Boolean isTargetLimitedToSourceContinent,
      @Name("isTargetLimitedToSourceMacroPhase") Boolean isTargetLimitedToSourceMacroPhase,
      @Name("serviceName") StringFilter serviceName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    DependencyInstanceFilter dependencyInstanceFilter = new DependencyInstanceFilter();
    dependencyInstanceFilter.setId(id);
    dependencyInstanceFilter.setName(name);
    dependencyInstanceFilter.setScope(scopeFilter);
    dependencyInstanceFilter.setDependencyName(dependencyName);
    dependencyInstanceFilter.setSourceComponentInstanceName(sourceComponentInstanceName);
    dependencyInstanceFilter.setTargetComponentInstanceName(targetComponentInstanceName);
    dependencyInstanceFilter.setViaComponentInstanceName(viaComponentInstanceName);
    dependencyInstanceFilter.setViaServiceName(viaServiceName);
    dependencyInstanceFilter.setIsSourceAsComponent(isSourceAsComponent);
    dependencyInstanceFilter.setIsSourceLimitedToLegacyOnly(isSourceLimitedToLegacyOnly);
    dependencyInstanceFilter.setIsSourceLimitedToCloudOnly(isSourceLimitedToCloudOnly);
    dependencyInstanceFilter.setIsSourceLimitedToLandingZone(isSourceLimitedToLandingZone);
    dependencyInstanceFilter.setIsSourceLimitedToTarget(isSourceLimitedToTarget);
    dependencyInstanceFilter.setIsSourceLimitedToContinent(isSourceLimitedToContinent);
    dependencyInstanceFilter.setIsSourceLimitedToMacroPhase(isSourceLimitedToMacroPhase);
    dependencyInstanceFilter.setIsSourceLimitedToSelector(isSourceLimitedToSelector);
    dependencyInstanceFilter.setIsTargetAsComponent(isTargetAsComponent);
    dependencyInstanceFilter.setIsTargetLimitedToLegacyOnly(isTargetLimitedToLegacyOnly);
    dependencyInstanceFilter.setIsTargetLimitedToCloudOnly(isTargetLimitedToCloudOnly);
    dependencyInstanceFilter.setIsTargetLimitedToSourceNamespace(isTargetLimitedToSourceNamespace);
    dependencyInstanceFilter.setIsTargetLimitedToSourcePaas(isTargetLimitedToSourcePaas);
    dependencyInstanceFilter.setIsTargetLimitedToSourceSecurityZone(
        isTargetLimitedToSourceSecurityZone);
    dependencyInstanceFilter.setIsTargetLimitedToSourceApplicationPhase(
        isTargetLimitedToSourceApplicationPhase);
    dependencyInstanceFilter.setIsTargetLimitedToSourceLandingZone(
        isTargetLimitedToSourceLandingZone);
    dependencyInstanceFilter.setIsTargetLimitedToSourceTarget(isTargetLimitedToSourceTarget);
    dependencyInstanceFilter.setIsTargetLimitedToSourceContinent(isTargetLimitedToSourceContinent);
    dependencyInstanceFilter.setIsTargetLimitedToSourceMacroPhase(
        isTargetLimitedToSourceMacroPhase);
    dependencyInstanceFilter.setServiceName(serviceName);

    List<DependencyInstance> filteredDependencyInstances = new ArrayList<>();
    for (DependencyInstance dependencyInstance :
        version.getDependencyInstancesRepository().getData().values()) {
      if (dependencyInstanceFilter.matches(dependencyInstance)) {
        filteredDependencyInstances.add(dependencyInstance);
      }
    }
    return toPage(
        filteredDependencyInstances, pageNumber, pageSize, DependencyInstanceGraphQL::from);
  }
}

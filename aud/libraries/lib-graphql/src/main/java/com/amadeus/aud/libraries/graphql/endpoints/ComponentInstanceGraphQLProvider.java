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
import com.amadeus.aud.libraries.graphql.dtos.ComponentInstanceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ComponentFilter;
import com.amadeus.aud.libraries.graphql.filters.ComponentInstanceFilter;
import com.amadeus.aud.libraries.graphql.filters.LandingZoneFilter;
import com.amadeus.aud.libraries.graphql.filters.NamespaceFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.ZoneInstanceFilter;
import com.amadeus.aud.libraries.graphql.filters.ZoneTypeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
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
public class ComponentInstanceGraphQLProvider {

  private final AudService audService;

  @Inject
  public ComponentInstanceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ComponentInstanceFilter.PROVIDER)
  public PageGraphQL<ComponentInstanceGraphQL> getComponentInstances(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("zoneType") ZoneTypeFilter zoneTypeFilter,
      @Name("applicationPhase") StringFilter applicationPhase,
      @Name("namespace") NamespaceFilter namespaceFilter,
      @Name("serviceTag") StringFilter serviceTag,
      @Name("deployComponentInstance") StringFilter deployComponentInstance,
      @Name("inSubnetInstance") StringFilter inSubnetInstance,
      @Name("outSubnetInstance") StringFilter outSubnetInstance,
      @Name("inZoneInstance") ZoneInstanceFilter inZoneInstanceFilter,
      @Name("outZoneInstance") ZoneInstanceFilter outZoneInstanceFilter,
      @Name("endpointType") StringFilter endpointType,
      @Name("deprecatedStatus") StringFilter deprecatedStatus,
      @Name("hostingPlatformType") StringFilter hostingPlatformType,
      @Name("k8sIdentifier") StringFilter k8sIdentifier,
      @Name("landingZone") LandingZoneFilter landingZoneFilter,
      @Name("iaasComponentInstanceName") StringFilter iaasComponentInstanceName,
      @Name("isAutomaticallyInstantiated") Boolean isAutomaticallyInstantiated,
      @Name("component") ComponentFilter componentFilter,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ComponentInstanceFilter componentInstanceFilter = new ComponentInstanceFilter();
    componentInstanceFilter.setId(id);
    componentInstanceFilter.setName(name);
    componentInstanceFilter.setScope(scopeFilter);
    componentInstanceFilter.setZoneType(zoneTypeFilter);
    componentInstanceFilter.setApplicationPhaseName(applicationPhase);
    componentInstanceFilter.setNamespace(namespaceFilter);
    componentInstanceFilter.setServiceTagName(serviceTag);
    componentInstanceFilter.setDeployComponentInstanceName(deployComponentInstance);
    componentInstanceFilter.setInSubnetInstanceName(inSubnetInstance);
    componentInstanceFilter.setOutSubnetInstanceName(outSubnetInstance);
    componentInstanceFilter.setInZoneInstance(inZoneInstanceFilter);
    componentInstanceFilter.setOutZoneInstance(outZoneInstanceFilter);
    componentInstanceFilter.setEndpointType(endpointType);
    componentInstanceFilter.setDeprecatedStatus(deprecatedStatus);
    componentInstanceFilter.setHostingPlatformType(hostingPlatformType);
    componentInstanceFilter.setK8sIdentifier(k8sIdentifier);
    componentInstanceFilter.setLandingZone(landingZoneFilter);
    componentInstanceFilter.setIaasComponentInstanceName(iaasComponentInstanceName);
    componentInstanceFilter.setIsAutomaticallyInstantiated(isAutomaticallyInstantiated);
    componentInstanceFilter.setComponent(componentFilter);

    List<ComponentInstance> filteredComponentInstances = new ArrayList<>();
    for (ComponentInstance componentInstance :
        version.getComponentInstancesRepository().getData().values()) {
      if (componentInstanceFilter.matches(componentInstance)) {
        filteredComponentInstances.add(componentInstance);
      }
    }
    return toPage(filteredComponentInstances, pageNumber, pageSize, ComponentInstanceGraphQL::from);
  }
}

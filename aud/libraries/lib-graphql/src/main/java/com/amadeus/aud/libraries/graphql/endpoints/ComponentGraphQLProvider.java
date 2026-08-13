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
import com.amadeus.aud.libraries.graphql.dtos.ComponentGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ComponentFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.ZoneTypeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Component;
import jakarta.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ComponentGraphQLProvider {

  private final AudService audService;

  @Inject
  public ComponentGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ComponentFilter.PROVIDER)
  public PageGraphQL<ComponentGraphQL> getComponents(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("deprecatedStatus") StringFilter deprecatedStatus,
      @Name("isPublic") Boolean isPublic,
      @Name("isPrivate") Boolean isPrivate,
      @Name("isHosting") Boolean isHosting,
      @Name("isHosted") Boolean isHosted,
      @Name("isTechnical") Boolean isTechnical,
      @Name("isContainer") Boolean isContainer,
      @Name("isInterface") Boolean isInterface,
      @Name("securityZone") StringFilter securityZone,
      @Name("serviceNowId") StringFilter serviceNowId,
      @Name("zoneTypes") Set<ZoneTypeFilter> zoneTypeFilters,
      @Name("parentName") StringFilter parentName,
      @Name("childName") StringFilter childName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ComponentFilter componentFilter = new ComponentFilter();
    componentFilter.setId(id);
    componentFilter.setName(name);
    componentFilter.setScope(scopeFilter);
    componentFilter.setDeprecatedStatus(deprecatedStatus);
    componentFilter.setIsPublic(isPublic);
    componentFilter.setIsPrivate(isPrivate);
    componentFilter.setIsHosting(isHosting);
    componentFilter.setIsHosted(isHosted);
    componentFilter.setIsTechnical(isTechnical);
    componentFilter.setIsContainer(isContainer);
    componentFilter.setIsInterface(isInterface);
    componentFilter.setSecurityZone(securityZone);
    componentFilter.setServiceNowId(serviceNowId);
    componentFilter.setZoneTypes(zoneTypeFilters);
    componentFilter.setParentName(parentName);
    componentFilter.setChildName(childName);

    List<Component> filteredComponents = new ArrayList<>();
    for (Component component : version.getComponentsRepository().getData().values()) {
      if (componentFilter.matches(component)) {
        filteredComponents.add(component);
      }
    }
    return toPage(filteredComponents, pageNumber, pageSize, ComponentGraphQL::from);
  }
}

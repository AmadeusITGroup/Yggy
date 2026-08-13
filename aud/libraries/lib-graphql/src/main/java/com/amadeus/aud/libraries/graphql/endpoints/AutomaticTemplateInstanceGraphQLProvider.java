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
import com.amadeus.aud.libraries.graphql.dtos.AutomaticTemplateInstanceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.AutomaticTemplateInstanceFilter;
import com.amadeus.aud.libraries.graphql.filters.ComponentInstanceFilter;
import com.amadeus.aud.libraries.graphql.filters.LandingZoneFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
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
public class AutomaticTemplateInstanceGraphQLProvider {

  private final AudService audService;

  @Inject
  public AutomaticTemplateInstanceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(AutomaticTemplateInstanceFilter.PROVIDER)
  public PageGraphQL<AutomaticTemplateInstanceGraphQL> getAutomaticTemplateInstances(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("componentName") StringFilter componentName,
      @Name("landingZone") LandingZoneFilter landingZoneFilter,
      @Name("componentInstances") List<ComponentInstanceFilter> componentInstanceFilters,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    AutomaticTemplateInstanceFilter automaticTemplateInstanceFilter =
        new AutomaticTemplateInstanceFilter();
    automaticTemplateInstanceFilter.setId(id);
    automaticTemplateInstanceFilter.setName(name);
    automaticTemplateInstanceFilter.setScope(scopeFilter);
    automaticTemplateInstanceFilter.setComponentName(componentName);
    automaticTemplateInstanceFilter.setLandingZone(landingZoneFilter);
    automaticTemplateInstanceFilter.setComponentInstances(componentInstanceFilters);

    List<AutomaticTemplateInstance> filteredAutomaticTemplateInstances = new ArrayList<>();
    for (AutomaticTemplateInstance automaticTemplateInstance :
        version.getAutomaticTemplateInstancesRepository().getData().values()) {
      if (automaticTemplateInstanceFilter.matches(automaticTemplateInstance)) {
        filteredAutomaticTemplateInstances.add(automaticTemplateInstance);
      }
    }
    return toPage(
        filteredAutomaticTemplateInstances,
        pageNumber,
        pageSize,
        AutomaticTemplateInstanceGraphQL::from);
  }
}

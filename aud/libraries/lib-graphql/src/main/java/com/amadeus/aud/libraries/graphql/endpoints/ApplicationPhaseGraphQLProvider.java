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
import com.amadeus.aud.libraries.graphql.dtos.ApplicationPhaseGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ApplicationPhaseFilter;
import com.amadeus.aud.libraries.graphql.filters.MacroPhaseFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
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
public class ApplicationPhaseGraphQLProvider {

  private final AudService audService;

  @Inject
  public ApplicationPhaseGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ApplicationPhaseFilter.PROVIDER)
  public PageGraphQL<ApplicationPhaseGraphQL> getApplicationPhases(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("isCustomerFacing") Boolean isCustomerFacing,
      @Name("macroPhase") MacroPhaseFilter macroPhaseFilter,
      @Name("componentInstances") List<StringFilter> componentInstances,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ApplicationPhaseFilter applicationPhaseFilter = new ApplicationPhaseFilter();
    applicationPhaseFilter.setId(id);
    applicationPhaseFilter.setName(name);
    applicationPhaseFilter.setScope(scopeFilter);
    applicationPhaseFilter.setIsCustomerFacing(isCustomerFacing);
    applicationPhaseFilter.setMacroPhase(macroPhaseFilter);
    applicationPhaseFilter.setComponentInstances(componentInstances);

    List<ApplicationPhase> filteredApplicationPhases = new ArrayList<>();
    for (ApplicationPhase applicationPhase :
        version.getApplicationPhasesRepository().getData().values()) {
      if (applicationPhaseFilter.matches(applicationPhase)) {
        filteredApplicationPhases.add(applicationPhase);
      }
    }
    return toPage(filteredApplicationPhases, pageNumber, pageSize, ApplicationPhaseGraphQL::from);
  }
}

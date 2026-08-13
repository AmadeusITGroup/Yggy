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
import com.amadeus.aud.libraries.graphql.dtos.MacroPhaseGraphQL;
import com.amadeus.aud.libraries.graphql.filters.MacroPhaseFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
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
public class MacroPhaseGraphQLProvider {

  private final AudService audService;

  @Inject
  public MacroPhaseGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(MacroPhaseFilter.PROVIDER)
  public PageGraphQL<MacroPhaseGraphQL> getMacroPhases(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("automationOrder") Integer automationOrder,
      @Name("applicationPhaseName") StringFilter applicationPhaseName,
      @Name("targetName") StringFilter targetName,
      @Name("connectionPhaseName") StringFilter connectionPhaseName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    MacroPhaseFilter macroPhaseFilter = new MacroPhaseFilter();
    macroPhaseFilter.setId(id);
    macroPhaseFilter.setName(name);
    macroPhaseFilter.setScope(scopeFilter);
    macroPhaseFilter.setAutomationOrder(automationOrder);
    macroPhaseFilter.setApplicationPhaseName(applicationPhaseName);
    macroPhaseFilter.setTargetName(targetName);
    macroPhaseFilter.setConnectionPhaseName(connectionPhaseName);

    List<MacroPhase> filteredMacroPhases = new ArrayList<>();
    for (MacroPhase macroPhase : version.getMacroPhasesRepository().getData().values()) {
      if (macroPhaseFilter.matches(macroPhase)) {
        filteredMacroPhases.add(macroPhase);
      }
    }
    return toPage(filteredMacroPhases, pageNumber, pageSize, MacroPhaseGraphQL::from);
  }
}

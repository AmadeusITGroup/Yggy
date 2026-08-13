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
import com.amadeus.aud.libraries.graphql.dtos.ScopeGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Scope;
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
public class ScopeGraphQLProvider {

  private final AudService audService;

  @Inject
  public ScopeGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ScopeFilter.PROVIDER)
  public PageGraphQL<ScopeGraphQL> getScopes(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name("serviceNowId") StringFilter serviceNowId,
      @Name("nanoId") StringFilter nanoId,
      @Name("assigneeGroup") StringFilter assigneeGroup,
      @Name("repositoryVersion") StringFilter repositoryVersion,
      @Name("isDeleted") Boolean isDeleted,
      @Name("owner") StringFilter owner,
      @Name("role") StringFilter role,
      @Name("targetName") StringFilter targetName,
      @Name("landingZoneName") StringFilter landingZoneName,
      @Name("componentName") StringFilter componentName,
      @Name("componentInstanceName") StringFilter componentInstanceName,
      @Name("zoneInstanceName") StringFilter zoneInstanceName,
      @Name("subnetInstanceName") StringFilter subnetInstanceName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {

    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ScopeFilter scopeFilter = new ScopeFilter();
    scopeFilter.setId(id);
    scopeFilter.setName(name);
    scopeFilter.setServiceNowId(serviceNowId);
    scopeFilter.setNanoId(nanoId);
    scopeFilter.setAssigneeGroup(assigneeGroup);
    scopeFilter.setRepositoryVersion(repositoryVersion);
    scopeFilter.setIsDeleted(isDeleted);
    scopeFilter.setOwner(owner);
    scopeFilter.setRole(role);
    scopeFilter.setTargetName(targetName);
    scopeFilter.setLandingZoneName(landingZoneName);
    scopeFilter.setComponentName(componentName);
    scopeFilter.setComponentInstanceName(componentInstanceName);
    scopeFilter.setZoneInstanceName(zoneInstanceName);
    scopeFilter.setSubnetInstanceName(subnetInstanceName);

    List<Scope> filteredScopes = new ArrayList<>();
    for (Scope scope : version.getScopesRepository().getData().values()) {
      if (scopeFilter.matches(scope)) {
        filteredScopes.add(scope);
      }
    }
    return toPage(filteredScopes, pageNumber, pageSize, ScopeGraphQL::from);
  }
}

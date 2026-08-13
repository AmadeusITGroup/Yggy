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
import com.amadeus.aud.libraries.graphql.dtos.NamespaceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.NamespaceFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Namespace;
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
public class NamespaceGraphQLProvider {

  private final AudService audService;

  @Inject
  public NamespaceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(NamespaceFilter.PROVIDER)
  public PageGraphQL<NamespaceGraphQL> getNamespaces(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) StringFilter scope,
      @Name("isShared") Boolean isShared,
      @Name("isAutomaticallyGenerated") Boolean isAutomaticallyGenerated,
      @Name("authorizedScopeName") StringFilter authorizedScopeName,
      @Name("componentInstanceName") StringFilter componentInstanceName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    NamespaceFilter namespaceFilter = new NamespaceFilter();
    namespaceFilter.setId(id);
    namespaceFilter.setName(name);
    namespaceFilter.setScope(scope);
    namespaceFilter.setIsShared(isShared);
    namespaceFilter.setIsAutomaticallyGenerated(isAutomaticallyGenerated);
    namespaceFilter.setAuthorizedScopeName(authorizedScopeName);
    namespaceFilter.setComponentInstanceName(componentInstanceName);
    List<Namespace> filteredNamespaces = new ArrayList<>();
    for (Namespace namespace : version.getNamespacesRepository().getData().values()) {
      if (namespaceFilter.matches(namespace)) {
        filteredNamespaces.add(namespace);
      }
    }
    return toPage(filteredNamespaces, pageNumber, pageSize, NamespaceGraphQL::from);
  }
}

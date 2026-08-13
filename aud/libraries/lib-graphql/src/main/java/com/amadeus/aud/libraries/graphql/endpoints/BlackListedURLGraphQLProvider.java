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
import com.amadeus.aud.libraries.graphql.dtos.BlackListedURLGraphQL;
import com.amadeus.aud.libraries.graphql.filters.BlackListedURLFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
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
public class BlackListedURLGraphQLProvider {

  private final AudService audService;

  @Inject
  public BlackListedURLGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(BlackListedURLFilter.PROVIDER)
  public PageGraphQL<BlackListedURLGraphQL> getBlackListedURLs(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("fqdn") StringFilter fqdn,
      @Name("transport") StringFilter transportLayer,
      @Name("ports") StringFilter ports,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {

    // Resolve the requested version – blocks until the version is ready
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    // Build the composite filter from the individual GraphQL inputs
    BlackListedURLFilter filter = new BlackListedURLFilter();
    filter.setId(id);
    filter.setName(name);
    filter.setScope(scopeFilter);
    filter.setFqdn(fqdn);
    filter.setTransportLayer(transportLayer);
    filter.setPorts(ports);

    // Iterate and apply the filter
    List<BlackListedURL> filtered = new ArrayList<>();
    for (BlackListedURL entry : version.getBlackListedURLsRepository().getData().values()) {
      if (filter.matches(entry)) {
        filtered.add(entry);
      }
    }

    return toPage(filtered, pageNumber, pageSize, BlackListedURLGraphQL::from);
  }
}

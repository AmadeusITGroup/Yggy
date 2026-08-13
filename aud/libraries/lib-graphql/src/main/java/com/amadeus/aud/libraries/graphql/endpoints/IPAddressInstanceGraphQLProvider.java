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
import com.amadeus.aud.libraries.graphql.dtos.IPAddressInstanceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.IPAddressInstanceFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
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
public class IPAddressInstanceGraphQLProvider {

  private final AudService audService;

  @Inject
  public IPAddressInstanceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(IPAddressInstanceFilter.PROVIDER)
  public PageGraphQL<IPAddressInstanceGraphQL> getIpAddressInstances(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) StringFilter scope,
      @Name("host") StringFilter host,
      @Name("advertisedTo") StringFilter advertisedTo,
      @Name("componentInstance") StringFilter componentInstance,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    IPAddressInstanceFilter ipAddressInstanceFilter = new IPAddressInstanceFilter();
    ipAddressInstanceFilter.setName(name);
    ipAddressInstanceFilter.setScope(scope);
    ipAddressInstanceFilter.setHost(host);
    ipAddressInstanceFilter.setAdvertisedTo(advertisedTo);
    ipAddressInstanceFilter.setComponentInstance(componentInstance);

    List<IPAddressInstance> filteredIpAddressInstances = new ArrayList<>();
    for (IPAddressInstance ipAddressInstance :
        version.getIpAddressInstancesRepository().getData().values()) {
      if (ipAddressInstanceFilter.matches(ipAddressInstance)) {
        filteredIpAddressInstances.add(ipAddressInstance);
      }
    }
    return toPage(filteredIpAddressInstances, pageNumber, pageSize, IPAddressInstanceGraphQL::from);
  }
}

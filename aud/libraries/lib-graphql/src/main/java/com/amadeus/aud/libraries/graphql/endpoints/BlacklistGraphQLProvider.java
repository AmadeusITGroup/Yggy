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
import com.amadeus.aud.libraries.graphql.dtos.BlacklistGraphQL;
import com.amadeus.aud.libraries.graphql.filters.BlacklistFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
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
public class BlacklistGraphQLProvider {

  private final AudService audService;

  @Inject
  public BlacklistGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(BlacklistFilter.PROVIDER)
  public PageGraphQL<BlacklistGraphQL> getBlacklists(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("sourceComponent") StringFilter sourceComponent,
      @Name("destinationHostFqdns") List<String> destinationHostFqdns,
      @Name("destinationHostCidrs") List<String> destinationHostCidrs,
      @Name("sourceZones") List<String> sourceZones,
      @Name("sourceMacrophases") List<String> sourceMacrophases,
      @Name("sourceTargets") List<String> sourceTargets,
      @Name("destinationApplicationProtocols") List<String> destinationApplicationProtocols,
      @Name("destinationTransportProtocols") List<String> destinationTransportProtocols,
      @Name("destinationPortRanges") List<String> destinationPortRanges,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    BlacklistFilter blacklistFilter = new BlacklistFilter();
    blacklistFilter.setId(id);
    blacklistFilter.setName(name);
    blacklistFilter.setScope(scopeFilter);
    blacklistFilter.setSourceComponent(sourceComponent);
    blacklistFilter.setDestinationHostFqdns(destinationHostFqdns);
    blacklistFilter.setDestinationHostCidrs(destinationHostCidrs);
    blacklistFilter.setSourceZones(sourceZones);
    blacklistFilter.setSourceMacrophases(sourceMacrophases);
    blacklistFilter.setSourceTargets(sourceTargets);
    blacklistFilter.setDestinationApplicationProtocols(destinationApplicationProtocols);
    blacklistFilter.setDestinationTransportProtocols(destinationTransportProtocols);
    blacklistFilter.setDestinationPortRanges(destinationPortRanges);

    List<Blacklist> filteredBlacklists = new ArrayList<>();
    for (Blacklist blacklist : version.getBlacklistsRepository().getData().values()) {
      if (blacklistFilter.matches(blacklist)) {
        filteredBlacklists.add(blacklist);
      }
    }
    return toPage(filteredBlacklists, pageNumber, pageSize, BlacklistGraphQL::from);
  }
}

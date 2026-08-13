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
import com.amadeus.aud.libraries.graphql.dtos.ConnectionGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ConnectionFilter;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.ZoneTypeFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Connection;
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
public class ConnectionGraphQLProvider {
  private final AudService audService;

  @Inject
  public ConnectionGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ConnectionFilter.PROVIDER)
  public PageGraphQL<ConnectionGraphQL> getConnections(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("firewallLevel") StringFilter firewallLevel,
      @Name("securityControl") StringFilter securityControl,
      @Name("connectionType") StringFilter connectionType,
      @Name("sourceZoneType") ZoneTypeFilter sourceZoneTypeFilter,
      @Name("destinationZoneType") ZoneTypeFilter destinationZoneTypeFilter,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ConnectionFilter connectionFilter = new ConnectionFilter();
    connectionFilter.setId(id);
    connectionFilter.setName(name);
    connectionFilter.setScope(scopeFilter);
    connectionFilter.setFirewallLevel(firewallLevel);
    connectionFilter.setSecurityControl(securityControl);
    connectionFilter.setConnectionType(connectionType);
    connectionFilter.setSourceZoneType(sourceZoneTypeFilter);
    connectionFilter.setDestinationZoneType(destinationZoneTypeFilter);
    List<Connection> filteredConnections = new ArrayList<>();
    for (Connection connection : version.getConnectionsRepository().getData().values()) {
      if (connectionFilter.matches(connection)) {
        filteredConnections.add(connection);
      }
    }
    return toPage(filteredConnections, pageNumber, pageSize, ConnectionGraphQL::from);
  }
}

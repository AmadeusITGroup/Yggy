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
import com.amadeus.aud.libraries.graphql.dtos.SubnetInstanceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.SubnetInstanceFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
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
public class SubnetInstanceGraphQLProvider {

  private final AudService audService;

  @Inject
  public SubnetInstanceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(SubnetInstanceFilter.PROVIDER)
  public PageGraphQL<SubnetInstanceGraphQL> getSubnetInstances(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) StringFilter scope,
      @Name("landingZoneName") StringFilter landingZoneName,
      @Name("vlan") StringFilter vlan,
      @Name("isShared") Boolean isShared,
      @Name("isGroup") Boolean isGroup,
      @Name("isNetworkPrivate") Boolean isNetworkPrivate,
      @Name("cidr") StringFilter cidr,
      @Name("subnetTypeName") StringFilter subnetTypeName,
      @Name("zoneInstanceName") StringFilter zoneInstanceName,
      @Name("parentName") StringFilter parentName,
      @Name("deprecatedStatus") StringFilter deprecatedStatus,
      @Name("deprecatedMessage") StringFilter deprecatedMessage,
      @Name("childName") StringFilter childName,
      @Name("componentInstanceName") StringFilter componentInstanceName,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    SubnetInstanceFilter subnetInstanceFilter = new SubnetInstanceFilter();
    subnetInstanceFilter.setId(id);
    subnetInstanceFilter.setName(name);
    subnetInstanceFilter.setScope(scope);
    subnetInstanceFilter.setLandingZoneName(landingZoneName);
    subnetInstanceFilter.setVlan(vlan);
    subnetInstanceFilter.setIsShared(isShared);
    subnetInstanceFilter.setIsGroup(isGroup);
    subnetInstanceFilter.setIsNetworkPrivate(isNetworkPrivate);
    subnetInstanceFilter.setCidr(cidr);
    subnetInstanceFilter.setSubnetTypeName(subnetTypeName);
    subnetInstanceFilter.setZoneInstanceName(zoneInstanceName);
    subnetInstanceFilter.setParentName(parentName);
    subnetInstanceFilter.setDeprecatedStatus(deprecatedStatus);
    subnetInstanceFilter.setDeprecatedMessage(deprecatedMessage);
    subnetInstanceFilter.setChildName(childName);
    subnetInstanceFilter.setComponentInstanceName(componentInstanceName);

    List<SubnetInstance> filteredSubnetInstances = new ArrayList<>();
    for (SubnetInstance subnetInstance :
        version.getSubnetInstancesRepository().getData().values()) {
      if (subnetInstanceFilter.matches(subnetInstance)) {
        filteredSubnetInstances.add(subnetInstance);
      }
    }

    return toPage(filteredSubnetInstances, pageNumber, pageSize, SubnetInstanceGraphQL::from);
  }
}

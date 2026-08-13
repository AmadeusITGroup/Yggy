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
import com.amadeus.aud.libraries.graphql.dtos.ServiceGraphQL;
import com.amadeus.aud.libraries.graphql.filters.ScopeFilter;
import com.amadeus.aud.libraries.graphql.filters.ServiceFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Service;
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
public class ServiceGraphQLProvider {

  private static boolean isSyntheticService(Service service) {
    String name = service.getName();
    return name != null && name.matches(".*<!>.*<!>.*");
  }

  private final AudService audService;

  @Inject
  public ServiceGraphQLProvider(AudService audService) {
    this.audService = audService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(ServiceFilter.PROVIDER)
  public PageGraphQL<ServiceGraphQL> getServices(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name(AbstractBaseMixin.JSON_PROPERTY_ID) StringFilter id,
      @Name(AbstractBaseMixin.JSON_PROPERTY_NAME) StringFilter name,
      @Name(AbstractBaseMixin.JSON_PROPERTY_SCOPE) ScopeFilter scopeFilter,
      @Name("componentName") StringFilter componentName,
      @Name("applicationIdName") StringFilter applicationIdName,
      @Name("serviceNowId") StringFilter serviceNowId,
      @Name("protocol") StringFilter protocol,
      @Name("approvedProtocol") StringFilter approvedProtocol,
      @Name("deprecatedStatus") StringFilter deprecatedStatus,
      @Name("isPrivate") Boolean isPrivate,
      @Name("isPci") Boolean isPci,
      @Name("isPii") Boolean isPii,
      @Name("isEncrypted") Boolean isEncrypted,
      @Name("isDbSynchro") Boolean isDbSynchro,
      @Name("port") StringFilter port,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {
    Version version = audService.getVersion(versionNumber).await().indefinitely();

    ServiceFilter serviceFilter = new ServiceFilter();
    serviceFilter.setId(id);
    serviceFilter.setName(name);
    serviceFilter.setScope(scopeFilter);
    serviceFilter.setComponentName(componentName);
    serviceFilter.setApplicationIdName(applicationIdName);
    serviceFilter.setServiceNowId(serviceNowId);
    serviceFilter.setProtocol(protocol);
    serviceFilter.setApprovedProtocol(approvedProtocol);
    serviceFilter.setDeprecatedStatus(deprecatedStatus);
    serviceFilter.setIsPrivate(isPrivate);
    serviceFilter.setIsPci(isPci);
    serviceFilter.setIsPii(isPii);
    serviceFilter.setIsEncrypted(isEncrypted);
    serviceFilter.setIsDbSynchro(isDbSynchro);
    serviceFilter.setPort(port);

    List<Service> filteredServices = new ArrayList<>();
    for (Service service : version.getServicesRepository().getData().values()) {
      if (isSyntheticService(service)) {
        continue;
      }

      if (serviceFilter.matches(service)) {
        filteredServices.add(service);
      }
    }
    return toPage(filteredServices, pageNumber, pageSize, ServiceGraphQL::from);
  }
}

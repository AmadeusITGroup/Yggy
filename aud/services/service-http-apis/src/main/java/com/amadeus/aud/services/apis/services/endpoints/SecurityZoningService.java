package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.common.UrlUtils;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.SecurityZoningResponse;
import com.amadeus.aud.services.apis.models.core.securityzoning.SecurityZoningSubnet;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
public class SecurityZoningService extends BaseService {
  private final AudApiConfig audApiConfig;
  private final AudService audService;

  @Inject
  public SecurityZoningService(AudApiConfig audApiConfig, AudService audService) {
    this.audApiConfig = audApiConfig;
    this.audService = audService;
  }

  public Uni<Response> getSecurityZoning(String version, UriInfo uriInfo) {
    return audService
        .getVersion(version)
        .onItem()
        .transformToUni((Version ver) -> getSecurityZoning(ver, uriInfo));
  }

  @CacheResult(cacheName = "getSecurityZoning", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getSecurityZoning(Version version, UriInfo uriInfo) {
    String versionNumber = version.getVersionNumber();

    SubnetInstancesRepository subnetInstancesRepositoryRepository =
        version.getSubnetInstancesRepository();
    checkRepository(subnetInstancesRepositoryRepository, versionNumber, "SubnetInstances");

    if (subnetInstancesRepositoryRepository.getData() == null) {
      throw new ServiceUnavailableException();
    }

    Map<String, SecurityZoningSubnet> securityZoningSubnetsMap = new TreeMap<>();
    for (SubnetInstance subnetInstance : subnetInstancesRepositoryRepository.getData().values()) {
      SecurityZoningSubnet securityZoningSubnet =
          SecurityZoningSubnet.fromSubnetInstance(subnetInstance);
      String key =
          new StringBuilder(securityZoningSubnet.landingzone())
              .append("***")
              .append(securityZoningSubnet.name())
              .toString();
      securityZoningSubnetsMap.put(key, SecurityZoningSubnet.fromSubnetInstance(subnetInstance));
    }

    return Uni.createFrom()
        .item(
            Unchecked.supplier(
                () ->
                    getOkResponse(
                        new SecurityZoningResponse()
                            .subnets(securityZoningSubnetsMap)
                            .url(UrlUtils.getUrl(uriInfo))
                            .urlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber))
                            .version(versionNumber)
                            .apiVersion(audApiConfig.version()))))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultWorkerPool());
  }
}

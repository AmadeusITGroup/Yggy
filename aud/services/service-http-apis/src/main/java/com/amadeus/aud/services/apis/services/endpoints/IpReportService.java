package com.amadeus.aud.services.apis.services.endpoints;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.aud.BaseService;
import com.amadeus.aud.libraries.engine.data.configs.AudApiConfig;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.IPAddressInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetInstancesRepository;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.amadeus.aud.libraries.yaml.models.IpReportCidr;
import com.amadeus.aud.libraries.yaml.models.IpReportIp;
import com.amadeus.aud.libraries.yaml.models.IpReportSubnet;
import com.amadeus.aud.libraries.yaml.models.IpResponse;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec.EndpointTypeEnum;
import com.amadeus.aud.services.apis.utils.IpReportUtils;
import com.amadeus.aud.services.apis.utils.UrlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.InetAddresses;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.net.util.SubnetUtils;

@Singleton
public class IpReportService extends BaseService {
  private final AudService audService;
  private final AudApiConfig audApiConfig;

  @Inject
  public IpReportService(AudService audService, AudApiConfig audApiConfig) {
    this.audService = audService;
    this.audApiConfig = audApiConfig;
  }

  public Uni<Response> getIpReport(String dataVersion, String ipStr, UriInfo uriInfo) {
    return audService
        .getVersion(dataVersion)
        .onItem()
        .transformToUni(version -> getIpReport(version, ipStr, uriInfo));
  }

  @CacheResult(cacheName = "getIpReport", keyGenerator = VersionCacheKeyGenerator.class)
  public Uni<Response> getIpReport(Version version, String ipStr, UriInfo uriInfo) {
    return Uni.createFrom()
        .item(Unchecked.supplier(() -> getIpReportResponse(version, ipStr, uriInfo)))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .emitOn(Infrastructure.getDefaultExecutor());
  }

  private Response getIpReportResponse(Version version, String ipStr, UriInfo uriInfo)
      throws JsonProcessingException {
    SubnetInstancesRepository subnetRepo = version.getSubnetInstancesRepository();
    checkRepository(subnetRepo, version.getVersionNumber(), "Subnet Instances");

    ComponentInstancesRepository compRepo = version.getComponentInstancesRepository();
    checkRepository(compRepo, version.getVersionNumber(), "Component Instances");

    IPAddressInstancesRepository ipRepo = version.getIpAddressInstancesRepository();
    checkRepository(ipRepo, version.getVersionNumber(), "IP Address Instances");

    Set<SubnetInstance> relatedSubnets;
    Set<ComponentInstance> relatedComponents;
    Set<IPAddressInstance> relatedIPAddresses;

    try {
      InetAddress ipAddr = InetAddresses.forString(ipStr);
      String ipAddress = ipAddr.getHostAddress();
      relatedSubnets = findRelatedSubnets(ipAddress, subnetRepo);
      relatedComponents = findRelatedComponents(ipAddress, compRepo);
      relatedIPAddresses = findRelatedIPAddresses(ipAddress, ipRepo);
    } catch (IllegalArgumentException e) {
      return getOkResponse(
          new IpResponse()
              .url(uriInfo.getRequestUri().toString())
              .urlVersion("")
              .version(version.toString())
              .apiVersion(audApiConfig.version())
              .subnets(new ArrayList<>())
              .cidrs(new ArrayList<>())
              .ips(new ArrayList<>())
              .errors(List.of("Invalid IP address: " + ipStr)));
    }

    List<IpReportSubnet> reportSubnets = new ArrayList<>();
    for (SubnetInstance sub : relatedSubnets) {
      reportSubnets.add(IpReportUtils.toIpReportSubnet(sub));
    }

    List<IpReportCidr> reportCidrs = new ArrayList<>();
    for (ComponentInstance comp : relatedComponents) {
      reportCidrs.add(IpReportUtils.toIpReportCidr(comp));
    }

    List<IpReportIp> reportIps = new ArrayList<>();
    for (IPAddressInstance ip : relatedIPAddresses) {
      reportIps.add(IpReportUtils.toIpReportIp(ip));
    }
    String versionNumber = version.getVersionNumber();
    return getOkResponse(
        new IpResponse()
            .url(UrlUtils.getUrl(uriInfo))
            .urlVersion(UrlUtils.getVersionedUrl(uriInfo, versionNumber))
            .version(version.toString())
            .apiVersion(audApiConfig.version())
            .subnets(reportSubnets)
            .cidrs(reportCidrs)
            .ips(reportIps));
  }

  private static Set<SubnetInstance> findRelatedSubnets(
      String ipAddress, SubnetInstancesRepository repository) {
    HashSet<SubnetInstance> relatedSubnets = new HashSet<>();
    for (SubnetInstance sub : repository.getData().values()) {
      if (shouldSkip(sub)) {
        continue;
      }
      if (isIpInEndpoint(ipAddress, sub.getCidr())) {
        addParents(relatedSubnets, sub);
      }
    }
    return relatedSubnets;
  }

  private static Set<ComponentInstance> findRelatedComponents(
      String ipAddress, ComponentInstancesRepository repository) {
    Set<ComponentInstance> result = new HashSet<>();
    for (ComponentInstance ci : repository.getData().values()) {
      if (ci.getEndpointType() != EndpointTypeEnum.CID_RLIST) {
        continue;
      }
      for (Object endpoint : ci.getEndpoints()) {
        if (endpoint instanceof String endpointStr && isIpInEndpoint(ipAddress, endpointStr)) {
          result.add(ci);
          break;
        }
      }
    }
    return result;
  }

  private static Set<IPAddressInstance> findRelatedIPAddresses(
      String ipAddress, IPAddressInstancesRepository repository) {
    Set<IPAddressInstance> result = new HashSet<>();
    for (IPAddressInstance ipInstance : repository.getData().values()) {
      if (ipAddress.equals(ipInstance.getName())) {
        result.add(ipInstance);
      }
    }
    return result;
  }

  private static boolean shouldSkip(SubnetInstance sub) {
    return sub.isGroup() || IPv4Utils.ALL.equals(sub.getCidr());
  }

  private static void addParents(HashSet<SubnetInstance> relatedSubnets, SubnetInstance sub) {
    while (sub != null && !relatedSubnets.contains(sub)) {
      relatedSubnets.add(sub);
      sub = sub.getParent();
    }
  }

  private static boolean isIpInEndpoint(String ipAddress, String endpointStr) {
    try {
      if (endpointStr.contains("/")) {
        SubnetUtils subnetUtils = new SubnetUtils(endpointStr);
        subnetUtils.setInclusiveHostCount(true);
        return subnetUtils.getInfo().isInRange(ipAddress);
      } else {
        return ipAddress.equals(endpointStr);
      }
    } catch (Exception e) {
      return false;
    }
  }
}

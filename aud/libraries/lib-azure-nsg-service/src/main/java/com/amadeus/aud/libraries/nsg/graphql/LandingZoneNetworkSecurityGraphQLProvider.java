package com.amadeus.aud.libraries.nsg.graphql;

import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.DEFAULT_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_NUMBER;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_PAGE_SIZE;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.PARAM_VERSION;
import static com.amadeus.aud.libraries.graphql.utils.GraphQLUtils.toPage;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.nsg.models.Network;
import com.amadeus.aud.libraries.nsg.models.NetworkSecurity;
import com.amadeus.aud.libraries.nsg.models.ServiceTagOutboundRule;
import com.amadeus.aud.libraries.nsg.models.Subnet;
import com.amadeus.aud.libraries.nsg.models.ZoneSecurityGroups;
import com.amadeus.aud.libraries.nsg.services.LandingZoneService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Size;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class LandingZoneNetworkSecurityGraphQLProvider {

  private final AudService audService;
  private final LandingZoneService landingZoneService;

  @Inject
  public LandingZoneNetworkSecurityGraphQLProvider(
      AudService audService, LandingZoneService landingZoneService) {
    this.audService = audService;
    this.landingZoneService = landingZoneService;
  }

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query(NetworkSecurityFilter.PROVIDER)
  public PageGraphQL<NetworkSecurityGraphQL> getLandingZoneNetworkSecurity(
      @Name(PARAM_VERSION) @DefaultValue(DEFAULT_VERSION) String versionNumber,
      @Name("landingZoneName")
          @Size(min = 1, message = "Landing Zone Name must be at least 1 character")
          String landingZoneName,
      @Name("cidr") StringFilter cidr,
      @Name("subnetName") StringFilter subnetName,
      @Name("subnetType") StringFilter subnetType,
      @Name("zoneName") StringFilter zoneName,
      @Name("zoneType") StringFilter zoneType,
      @Name("labels") List<LabelFilter> labelFilters,
      @Name(PARAM_PAGE_NUMBER) @DefaultValue(DEFAULT_PAGE_NUMBER) int pageNumber,
      @Name(PARAM_PAGE_SIZE) @DefaultValue(DEFAULT_PAGE_SIZE) int pageSize) {

    Version version = audService.getVersion(versionNumber).await().indefinitely();

    // Get the specific landing zone by name
    NetworkSecurityGraphQL networkSecurityGraphQL =
        getNetworkSecurityForLandingZone(version, landingZoneName);

    List<NetworkSecurityGraphQL> results = new ArrayList<>();

    if (networkSecurityGraphQL != null) {
      // Only apply additional filters if at least one is provided
      boolean hasAdditionalFilters =
          cidr != null
              || subnetName != null
              || subnetType != null
              || zoneName != null
              || zoneType != null
              || (labelFilters != null && !labelFilters.isEmpty());

      if (hasAdditionalFilters) {
        NetworkSecurityFilter filter = new NetworkSecurityFilter();
        filter.setCidr(cidr);
        filter.setSubnetName(subnetName);
        filter.setSubnetType(subnetType);
        filter.setZoneName(zoneName);
        filter.setZoneType(zoneType);
        filter.setLabels(labelFilters);

        if (filter.matches(landingZoneName, networkSecurityGraphQL)) {
          results.add(networkSecurityGraphQL);
        }
      } else {
        results.add(networkSecurityGraphQL);
      }
    }

    return toPage(results, pageNumber, pageSize, dto -> dto);
  }

  private NetworkSecurityGraphQL getNetworkSecurityForLandingZone(
      Version version, String landingZoneName) {
    try {
      LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
      if (landingZonesRepository == null) {
        return null;
      }

      LandingZone landingZone = landingZonesRepository.get(landingZoneName);
      if (landingZone == null) {
        return null;
      }

      Target landingZoneTarget = landingZone.getTarget();
      String providerName = landingZoneTarget.getProviderName();

      if (!Objects.equals(providerName, "Microsoft - Azure")) {
        return null;
      }

      NetworkSecurity networkSecurity =
          landingZoneService.buildNetworkSecurity(version, landingZone, landingZoneTarget);
      return convertToGraphQL(landingZoneName, networkSecurity);
    } catch (Exception e) {
      return null;
    }
  }

  // Converts NetworkSecurity to NetworkSecurityGraphQL DTO
  private NetworkSecurityGraphQL convertToGraphQL(
      String landingZoneName, NetworkSecurity networkSecurity) {
    NetworkSecurityGraphQL dto = new NetworkSecurityGraphQL();
    dto.setLandingZoneId(landingZoneName);

    // Convert networks
    if (networkSecurity.getNetworks() != null) {
      Map<String, NetworkSecurityGraphQL.NetworkGraphQL> networksMap = new HashMap<>();
      for (Map.Entry<String, Network> entry : networkSecurity.getNetworks().entrySet()) {
        NetworkSecurityGraphQL.NetworkGraphQL networkDto =
            new NetworkSecurityGraphQL.NetworkGraphQL();
        networkDto.setCidrs(entry.getValue().getCidrs());

        // Convert providerSpecific from Object to ProviderSpecificGraphQL
        Object providerSpecific = entry.getValue().getProviderSpecific();
        if (providerSpecific instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> providerSpecificMap = (Map<String, Object>) providerSpecific;
          NetworkSecurityGraphQL.ProviderSpecificGraphQL providerSpecificDto =
              new NetworkSecurityGraphQL.ProviderSpecificGraphQL();

          // Extract dnsservers if present
          if (providerSpecificMap.containsKey("dnsservers")) {
            Object dnsservers = providerSpecificMap.get("dnsservers");
            if (dnsservers instanceof List) {
              @SuppressWarnings("unchecked")
              List<String> dnsServersList = (List<String>) dnsservers;
              providerSpecificDto.setDnsservers(dnsServersList);
            }
          }

          networkDto.setProviderSpecific(providerSpecificDto);
        }

        networkDto.setTags(entry.getValue().getTags());
        networksMap.put(entry.getKey(), networkDto);
      }
      dto.setNetworks(networksMap);
    }

    // Convert subnets
    if (networkSecurity.getSubnets() != null) {
      Map<String, NetworkSecurityGraphQL.SubnetGraphQL> subnetsMap = new HashMap<>();
      for (Map.Entry<String, Subnet> entry : networkSecurity.getSubnets().entrySet()) {
        Subnet subnet = entry.getValue();
        NetworkSecurityGraphQL.SubnetGraphQL subnetDto = new NetworkSecurityGraphQL.SubnetGraphQL();
        subnetDto.setName(subnet.getName());
        subnetDto.setSubnetType(subnet.getSubnetType());
        subnetDto.setCidr(subnet.getCidr());
        subnetDto.setZoneName(subnet.getZoneName());
        subnetDto.setZoneType(subnet.getZoneType());
        subnetDto.setAssociatedNSG(subnet.getAssociatedNSG());
        subnetsMap.put(entry.getKey(), subnetDto);
      }
      dto.setSubnets(subnetsMap);
    }

    // Convert NSGs
    if (networkSecurity.getNsgs() != null) {
      ZoneSecurityGroups nsgs = networkSecurity.getNsgs();
      NetworkSecurityGraphQL.ZoneSecurityGroupsGraphQL nsgsDto =
          new NetworkSecurityGraphQL.ZoneSecurityGroupsGraphQL();

      // Convert rules
      if (nsgs.rules() != null) {
        Map<String, NetworkSecurityGraphQL.SecurityGroupMapGraphQL> rulesMap = new HashMap<>();
        for (Map.Entry<String, com.amadeus.aud.libraries.nsg.models.SecurityGroupMap> entry :
            nsgs.rules().entrySet()) {
          NetworkSecurityGraphQL.SecurityGroupMapGraphQL sgMapDto =
              new NetworkSecurityGraphQL.SecurityGroupMapGraphQL();
          com.amadeus.aud.libraries.nsg.models.SecurityGroupMap sgMap = entry.getValue();

          // Convert inbound rules
          if (sgMap.getInbound() != null) {
            List<NetworkSecurityGraphQL.SubnetPortGraphQL> inboundList = new ArrayList<>();
            for (com.amadeus.aud.libraries.nsg.models.NetworkRule rule : sgMap.getInbound()) {
              NetworkSecurityGraphQL.SubnetPortGraphQL inboundDto =
                  new NetworkSecurityGraphQL.SubnetPortGraphQL();
              inboundDto.setName(rule.getName());
              inboundDto.setCidrs(rule.getCidrs());
              inboundDto.setDstCidrs(rule.getDstCidrs());
              inboundDto.setPorts(rule.getPorts());
              inboundDto.setProtocol(rule.getProtocol() != null ? rule.getProtocol().name() : null);
              inboundList.add(inboundDto);
            }
            sgMapDto.setInbound(inboundList);
          }

          // Convert outbound rules
          if (sgMap.getOutbound() != null) {
            List<NetworkSecurityGraphQL.ServiceTagOutboundRuleGraphQL> outboundList =
                new ArrayList<>();
            for (ServiceTagOutboundRule rule : sgMap.getOutbound()) {
              NetworkSecurityGraphQL.ServiceTagOutboundRuleGraphQL outboundDto =
                  new NetworkSecurityGraphQL.ServiceTagOutboundRuleGraphQL();
              outboundDto.setName(rule.getName());
              outboundDto.setServiceTag(rule.getServiceTag());
              outboundDto.setPorts(rule.getPorts());
              outboundDto.setProtocol(
                  rule.getProtocol() != null ? rule.getProtocol().getValue() : null);
              outboundDto.setCidrs(rule.getCidrs());
              outboundList.add(outboundDto);
            }
            sgMapDto.setOutbound(outboundList);
          }

          rulesMap.put(entry.getKey(), sgMapDto);
        }
        nsgsDto.setRules(rulesMap);
      }

      nsgsDto.setTags(nsgs.tags());
      dto.setNsgs(nsgsDto);
    }

    return dto;
  }
}

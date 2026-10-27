package com.amadeus.aud.libraries.v2.objects.service;

import static com.amadeus.aud.libraries.models.core.db.base.BaseInstance.ID_SEPARATOR;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Display;
import com.amadeus.aud.libraries.backend.models.v2.JsonPathLink;
import com.amadeus.aud.libraries.backend.models.v2.JsonPathPatternLink;
import com.amadeus.aud.libraries.backend.models.v2.Link;
import com.amadeus.aud.libraries.backend.models.v2.UriLink;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.OutboundSNAT;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Role;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public final class ObjectUtils {
  static final String DATA_BASE_URL = "/v2/versions/latest/data/";

  private final LinksHandler defaultLinksHandler;

  @Inject
  public ObjectUtils(LinksHandler defaultLinksHandler) {
    this.defaultLinksHandler = defaultLinksHandler;
  }

  public List<Link> links(BomElement obj, ObjectTypeListNamesEnum objectType) {
    List<Link> links = new ArrayList<>();
    // scope is common to all objects
    if (obj.getScope() != null) {
      Link scopeLink =
          new JsonPathLink(
              "$.scope",
              dataUri(ObjectTypeListNamesEnum.SCOPES, obj.getScope().id()),
              Display.HREF);
      links.add(scopeLink);
      links.add(
          new UriLink(
              "References",
              dataUri(ObjectTypeListNamesEnum.SCOPES, obj.getScope().id()) + "/references",
              Display.TAB));
    }

    if (objectType != ObjectTypeListNamesEnum.CERTIFICATIONS) {
      links.add(new JsonPathLink("$.name", dataUri(objectType, obj.id()), Display.HREF));
    }

    switch (obj) {
      case ApplicationId _, BlockedPort _, Role _, ServiceTag _, SubnetType _, TargetType _ -> {}
      case ApplicationPhase appPhase -> links.addAll(appPhaseLinks(appPhase));
      case AutomaticTemplateInstance ati -> links.addAll(automaticTemplateInstanceLinks(ati));
      case Component component -> links.addAll(componentLinks(component));
      case ComponentInstance ci -> links.addAll(componentInstanceLinks(ci));
      case ConnectionPhase cp -> links.addAll(connectionPhaseLinks(cp));
      case Continent continent -> links.addAll(continentLinks(continent));
      case Dependency dep -> links.addAll(dependencyLinks(dep));
      case DependencyInstance di -> links.addAll(dependencyInstanceLinks(di));
      case FirewallGroup fg -> links.addAll(firewallGroupLinks(fg));
      case FirewallVendor fv -> links.addAll(firewallVendorLinks(fv));
      case IPAddressInstance ip -> links.addAll(ipAddressInstanceLinks(ip));
      case LandingZone lz -> links.addAll(landingZoneLinks(lz));
      case LandingZoneFunctionalType lzft -> links.addAll(landingZoneFunctionalTypeLinks(lzft));
      case LandingZoneType lzt -> links.addAll(landingZoneTypeLinks(lzt));
      case MacroPhase macroPhase -> links.addAll(macroPhaseLinks(macroPhase));
      case Namespace namespace -> links.addAll(namespaceLinks(namespace));
      case OutboundSNAT snat -> links.addAll(outboundSnatLinks(snat));
      case Provider provider -> links.addAll(providerLinks(provider));
      case Region region -> links.addAll(regionLinks(region));
      case Scope scope -> links.addAll(scopeLinks(scope));
      case Service service -> links.addAll(serviceLinks(service));
      case SubnetInstance subnetInstance -> links.addAll(subnetInstanceLinks(subnetInstance));
      case Target target -> links.addAll(targetLinks(target));
      case ZoneInstance zi -> links.addAll(zoneInstanceLinks(zi));
      case ZoneType zoneType -> links.addAll(zoneTypeLinks(zoneType));
      default -> {}
    }
    links.addAll(this.defaultLinksHandler.getLinks(obj));
    return links;
  }

  public static String dataUri(ObjectTypeListNamesEnum type, String id) {
    return DATA_BASE_URL + type.getUri() + "/" + id;
  }

  private static UriLink connectivityLink(ObjectTypeListNamesEnum type, String id) {
    return new UriLink("Connectivity Table", dataUri(type, id) + "/connectivity", Display.TAB);
  }

  private static List<Link> scopeLinks(Scope baseScope) {
    Scope scope = baseScope;
    List<Link> links = new ArrayList<>();

    links.add(
        new JsonPathPatternLink(
            "$.roles[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.ROLES.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new UriLink(
            "Statistics",
            DATA_BASE_URL + ObjectTypeListNamesEnum.SCOPES.getUri() + "/statistics",
            Display.TAB));
    links.add(
        new UriLink(
            "Release Notes",
            dataUri(ObjectTypeListNamesEnum.SCOPES, scope.id()) + "/release_notes",
            Display.TAB));
    links.add(
        new UriLink(
            "Deployment",
            dataUri(ObjectTypeListNamesEnum.SCOPES, scope.id()) + "/deployment",
            Display.TAB));
    links.add(
        new UriLink(
            "PR Reports",
            dataUri(ObjectTypeListNamesEnum.SCOPES, scope.id()) + "/prs",
            Display.TAB));
    links.add(connectivityLink(ObjectTypeListNamesEnum.SCOPES, scope.id()));
    return links;
  }

  private static List<Link> appPhaseLinks(ApplicationPhase appPhase) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.macro_phase",
            dataUri(ObjectTypeListNamesEnum.MACROPHASES, appPhase.getMacroPhase().id()),
            Display.HREF));
    return links;
  }

  private static List<Link> automaticTemplateInstanceLinks(AutomaticTemplateInstance instance) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.landing_zone",
            dataUri(ObjectTypeListNamesEnum.LANDINGZONES, instance.getLandingZone().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.component",
            dataUri(ObjectTypeListNamesEnum.COMPONENTS, instance.getComponent().id()),
            Display.HREF));
    return links;
  }

  private static List<Link> componentLinks(Component component) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathPatternLink(
            "$.parents[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.COMPONENTS.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.access_control.controlled_components[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.COMPONENTS.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.access_control.allow[*].applied_to_scopes[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.SCOPES.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.access_control.deny[*].applied_to_scopes[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.SCOPES.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.security_zones[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.ZONETYPES.getUri() + "/{value}",
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.COMPONENTS, component.id()));
    links.add(
        new UriLink(
            "Instance Repartition",
            dataUri(ObjectTypeListNamesEnum.COMPONENTS, component.id()) + "/instance-repartition",
            Display.BUTTON));
    return links;
  }

  private static List<Link> componentInstanceLinks(ComponentInstance ci) {
    List<Link> links = new ArrayList<>();

    links.add(
        new JsonPathLink(
            "$.component",
            dataUri(ObjectTypeListNamesEnum.COMPONENTS, ci.getComponent().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.application_phase",
            dataUri(ObjectTypeListNamesEnum.APPLICATIONPHASES, ci.getApplicationPhase().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.landing_zone",
            dataUri(ObjectTypeListNamesEnum.LANDINGZONES, ci.getLandingZone().id()),
            Display.HREF));

    if (ci.getNamespace() != null) {
      links.add(
          new JsonPathLink(
              "$.namespace",
              dataUri(ObjectTypeListNamesEnum.NAMESPACES, ci.getNamespace().id()),
              Display.HREF));
    }

    if (ci.getInSubnetInstance() != null) {
      links.add(
          new JsonPathLink(
              "$.in_subnet_instance",
              dataUri(ObjectTypeListNamesEnum.SUBNETINSTANCES, ci.getInSubnetInstance().id()),
              Display.HREF));
    }
    if (ci.getOutSubnetInstance() != null) {
      links.add(
          new JsonPathLink(
              "$.out_subnet_instance",
              dataUri(ObjectTypeListNamesEnum.SUBNETINSTANCES, ci.getOutSubnetInstance().id()),
              Display.HREF));
    }

    if (ci.getInZoneInstance() != null) {
      links.add(
          new JsonPathLink(
              "$.in_zone_instance",
              dataUri(ObjectTypeListNamesEnum.ZONEINSTANCES, ci.getInZoneInstance().id()),
              Display.HREF));
    }
    if (ci.getOutZoneInstance() != null) {
      links.add(
          new JsonPathLink(
              "$.out_zone_instance",
              dataUri(ObjectTypeListNamesEnum.ZONEINSTANCES, ci.getOutZoneInstance().id()),
              Display.HREF));
    }

    if (ci.getHostingPlatform() != null) {
      links.add(
          new JsonPathLink(
              "$.hosting_platform",
              dataUri(ObjectTypeListNamesEnum.COMPONENTINSTANCES, ci.getHostingPlatform().id()),
              Display.HREF));
    }

    if (ci.getEndpoints() != null && !ci.getEndpoints().isEmpty()) {
      links.add(
          new JsonPathPatternLink(
              "$.endpoints[*].implementationzone_instance",
              DATA_BASE_URL
                  + ObjectTypeListNamesEnum.ZONEINSTANCES.getUri()
                  + "/"
                  + ci.getLandingZone().id()
                  + ID_SEPARATOR
                  + "{value}",
              Display.HREF));
    }

    if (ci.getAutomaticTemplateInstances() != null
        && !ci.getAutomaticTemplateInstances().isEmpty()) {
      links.add(
          new JsonPathPatternLink(
              "$.automatic_template_instances[*].name",
              DATA_BASE_URL
                  + ObjectTypeListNamesEnum.AUTOMATICTEMPLATEINSTANCES.getUri()
                  + "/{value}",
              Display.HREF));
    }

    if (ci.getZoneType() != null) {
      links.add(
          new JsonPathLink(
              "$.zone_type",
              dataUri(ObjectTypeListNamesEnum.ZONETYPES, ci.getZoneType().id()),
              Display.HREF));
    }

    links.add(connectivityLink(ObjectTypeListNamesEnum.COMPONENTINSTANCES, ci.id()));

    return links;
  }

  private static List<Link> connectionPhaseLinks(ConnectionPhase cp) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.source_phase",
            dataUri(ObjectTypeListNamesEnum.MACROPHASES, cp.getSourcePhase().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.destination_phase",
            dataUri(ObjectTypeListNamesEnum.MACROPHASES, cp.getDestinationPhase().id()),
            Display.HREF));
    return links;
  }

  private static List<Link> continentLinks(Continent continent) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathPatternLink(
            "$.targets[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.TARGETS.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.regions[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.REGIONS.getUri() + "/{value}",
            Display.HREF));
    return links;
  }

  private static List<Link> dependencyLinks(Dependency d) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.source_component",
            dataUri(ObjectTypeListNamesEnum.COMPONENTS, d.getSourceComponent().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.target_component",
            dataUri(ObjectTypeListNamesEnum.COMPONENTS, d.getDestinationComponent().id()),
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.DEPENDENCIES, d.id()));
    return links;
  }

  private static List<Link> dependencyInstanceLinks(DependencyInstance di) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.dependency",
            dataUri(ObjectTypeListNamesEnum.DEPENDENCIES, di.getDependency().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.landing_zone",
            dataUri(ObjectTypeListNamesEnum.LANDINGZONES, di.getLandingZone().id()),
            Display.HREF));
    if (di.getViaComponentInstance() != null) {
      links.add(
          new JsonPathLink(
              "$.via_component_instance",
              dataUri(
                  ObjectTypeListNamesEnum.COMPONENTINSTANCES, di.getViaComponentInstance().id()),
              Display.HREF));
    }
    if (di.getViaService() != null) {
      links.add(
          new JsonPathLink(
              "$.via_service",
              dataUri(ObjectTypeListNamesEnum.SERVICES, di.getViaService().id()),
              Display.HREF));
    }
    if (di.getComponentInstance() != null) {
      links.add(
          new JsonPathLink(
              "$.source_component_instance",
              dataUri(ObjectTypeListNamesEnum.COMPONENTINSTANCES, di.getComponentInstance().id()),
              Display.HREF));
    }
    if (di.getTargetComponentInstance() != null) {
      links.add(
          new JsonPathLink(
              "$.target_component_instance",
              dataUri(
                  ObjectTypeListNamesEnum.COMPONENTINSTANCES, di.getTargetComponentInstance().id()),
              Display.HREF));
    }
    links.add(
        new JsonPathPatternLink(
            "$.target_services[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.SERVICES.getUri() + "/{value}",
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES, di.id()));
    return links;
  }

  private static List<Link> firewallGroupLinks(FirewallGroup fg) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.firewall_vendor",
            dataUri(ObjectTypeListNamesEnum.FIREWALLVENDORS, fg.getFirewallVendor().id()),
            Display.HREF));
    if (fg.getTarget() != null) {
      links.add(
          new JsonPathLink(
              "$.target",
              dataUri(ObjectTypeListNamesEnum.TARGETS, fg.getTarget().id()),
              Display.HREF));
    }
    return links;
  }

  private static List<Link> firewallVendorLinks(FirewallVendor fv) {
    List<Link> links = new ArrayList<>();
    if (fv.getFirewallGroups() != null) {
      links.add(
          new JsonPathPatternLink(
              "$.firewall_groups[*]",
              DATA_BASE_URL + ObjectTypeListNamesEnum.FIREWALLGROUPS.getUri() + "/{value}",
              Display.HREF));
    }
    return links;
  }

  private static List<Link> ipAddressInstanceLinks(IPAddressInstance ip) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.landing_zone",
            dataUri(ObjectTypeListNamesEnum.LANDINGZONES, ip.getLandingZone().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.component_instance",
            dataUri(ObjectTypeListNamesEnum.COMPONENTINSTANCES, ip.getComponentInstance().id()),
            Display.HREF));
    return links;
  }

  private static List<Link> landingZoneLinks(LandingZone lz) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.target",
            dataUri(ObjectTypeListNamesEnum.TARGETS, lz.getTarget().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.region",
            dataUri(ObjectTypeListNamesEnum.REGIONS, lz.getRegion().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.landing_zone_type",
            dataUri(ObjectTypeListNamesEnum.LANDINGZONETYPES, lz.getLandingZoneType().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.macro_phase",
            dataUri(ObjectTypeListNamesEnum.MACROPHASES, lz.getMacroPhase().id()),
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.functional_types[*]",
            DATA_BASE_URL
                + ObjectTypeListNamesEnum.LANDINGZONEFUNCTIONALTYPES.getUri()
                + "/{value}",
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.LANDINGZONES, lz.id()));
    return links;
  }

  private static List<Link> landingZoneFunctionalTypeLinks(LandingZoneFunctionalType lzft) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathPatternLink(
            "$.landing_zones[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.LANDINGZONES.getUri() + "/{value}",
            Display.HREF));
    return links;
  }

  private static List<Link> landingZoneTypeLinks(LandingZoneType lzt) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathPatternLink(
            "$.landing_zones[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.LANDINGZONES.getUri() + "/{value}",
            Display.HREF));
    return links;
  }

  private static List<Link> macroPhaseLinks(MacroPhase macroPhase) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathPatternLink(
            "$.application_phases[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.APPLICATIONPHASES.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.targets[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.TARGETS.getUri() + "/{value}",
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.MACROPHASES, macroPhase.id()));
    return links;
  }

  private static List<Link> namespaceLinks(Namespace namespace) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathPatternLink(
            "$.authorized_scopes[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.SCOPES.getUri() + "/{value}",
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.NAMESPACES, namespace.id()));
    return links;
  }

  private static List<Link> outboundSnatLinks(OutboundSNAT snat) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.macro_phase",
            dataUri(ObjectTypeListNamesEnum.MACROPHASES, snat.getMacroPhase().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.continent",
            dataUri(ObjectTypeListNamesEnum.CONTINENTS, snat.getContinent().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.zone_type",
            dataUri(ObjectTypeListNamesEnum.ZONETYPES, snat.getZoneType().id()),
            Display.HREF));
    if (snat.getComponentInstances() != null && !snat.getComponentInstances().isEmpty()) {
      links.add(
          new JsonPathPatternLink(
              "$.component_instances[*]",
              DATA_BASE_URL + ObjectTypeListNamesEnum.COMPONENTINSTANCES.getUri() + "/{value}",
              Display.HREF));
    }
    return links;
  }

  private static List<Link> providerLinks(Provider provider) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathPatternLink(
            "$.regions[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.REGIONS.getUri() + "/{value}",
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.targets[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.TARGETS.getUri() + "/{value}",
            Display.HREF));
    return links;
  }

  private static List<Link> regionLinks(Region region) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.provider",
            dataUri(ObjectTypeListNamesEnum.PROVIDERS, region.getProvider().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.continent",
            dataUri(ObjectTypeListNamesEnum.CONTINENTS, region.getContinent().id()),
            Display.HREF));
    links.add(
        new JsonPathPatternLink(
            "$.landing_zones[*]",
            DATA_BASE_URL + ObjectTypeListNamesEnum.LANDINGZONES.getUri() + "/{value}",
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.REGIONS, region.id()));
    return links;
  }

  private static List<Link> serviceLinks(Service service) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.component",
            dataUri(ObjectTypeListNamesEnum.COMPONENTS, service.getComponent().id()),
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.SERVICES, service.id()));
    return links;
  }

  private static List<Link> subnetInstanceLinks(SubnetInstance subnetInstance) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.landing_zone",
            dataUri(ObjectTypeListNamesEnum.LANDINGZONES, subnetInstance.getLandingZone().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.zone_instance",
            dataUri(ObjectTypeListNamesEnum.ZONEINSTANCES, subnetInstance.getZoneInstance().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.subnet_type",
            dataUri(ObjectTypeListNamesEnum.SUBNETTYPES, subnetInstance.getSubnetType().id()),
            Display.HREF));
    return links;
  }

  private static List<Link> targetLinks(Target target) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.target_type",
            dataUri(ObjectTypeListNamesEnum.TARGETTYPES, target.getTargetType().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.provider",
            dataUri(ObjectTypeListNamesEnum.PROVIDERS, target.getProvider().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.macro_phase",
            dataUri(ObjectTypeListNamesEnum.MACROPHASES, target.getMacroPhase().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.continent",
            dataUri(ObjectTypeListNamesEnum.CONTINENTS, target.getContinent().id()),
            Display.HREF));
    links.add(connectivityLink(ObjectTypeListNamesEnum.TARGETS, target.id()));
    return links;
  }

  private static List<Link> zoneInstanceLinks(ZoneInstance zi) {
    List<Link> links = new ArrayList<>();
    links.add(
        new JsonPathLink(
            "$.zone_type",
            dataUri(ObjectTypeListNamesEnum.ZONETYPES, zi.getZoneType().id()),
            Display.HREF));
    links.add(
        new JsonPathLink(
            "$.landing_zone",
            dataUri(ObjectTypeListNamesEnum.LANDINGZONES, zi.getLandingZone().id()),
            Display.HREF));
    return links;
  }

  private static List<Link> zoneTypeLinks(ZoneType zoneType) {
    List<Link> links = new ArrayList<>();
    links.add(connectivityLink(ObjectTypeListNamesEnum.ZONETYPES, zoneType.id()));
    return links;
  }
}

package com.amadeus.aud.libraries.yaml.models.utils;

import com.amadeus.aud.libraries.yaml.models.V1ApplicationID;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationPhase;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDR;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURL;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPort;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1Connection;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalComponentSkip;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionApprovalScopeSkip;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhase;
import com.amadeus.aud.libraries.yaml.models.V1Continent;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroup;
import com.amadeus.aud.libraries.yaml.models.V1FirewallVendor;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1LandingZone;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneFunctionalType;
import com.amadeus.aud.libraries.yaml.models.V1LandingzoneType;
import com.amadeus.aud.libraries.yaml.models.V1MacroPhase;
import com.amadeus.aud.libraries.yaml.models.V1MappingLandingzoneFunctionaltype;
import com.amadeus.aud.libraries.yaml.models.V1Namespace;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNAT;
import com.amadeus.aud.libraries.yaml.models.V1Provider;
import com.amadeus.aud.libraries.yaml.models.V1Region;
import com.amadeus.aud.libraries.yaml.models.V1Role;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.amadeus.aud.libraries.yaml.models.V1Servicetag;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetType;
import com.amadeus.aud.libraries.yaml.models.V1Target;
import com.amadeus.aud.libraries.yaml.models.V1TargetType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstance;
import com.amadeus.aud.libraries.yaml.models.V1ZoneType;
import java.util.HashMap;
import java.util.Map;

public final class DefaultKindMapper implements KindMapper {
  private static final Map<String, Class<? extends V1Base>> kindToClassMap = new HashMap<>();

  public DefaultKindMapper() {}

  static {
    kindToClassMap.put("ApplicationID", V1ApplicationID.class);
    kindToClassMap.put("ApplicationPhase", V1ApplicationPhase.class);
    kindToClassMap.put("BlackListedCIDR", V1BlackListedCIDR.class);
    kindToClassMap.put("BlackListedURL", V1BlackListedURL.class);
    kindToClassMap.put("BlockedPort", V1BlockedPort.class);
    kindToClassMap.put("Component", V1Component.class);
    kindToClassMap.put("ComponentInstance", V1ComponentInstance.class);
    kindToClassMap.put("Connection", V1Connection.class);
    kindToClassMap.put("ConnectionPhase", V1ConnectionPhase.class);
    kindToClassMap.put("ConnectionApprovalComponentSkip", V1ConnectionApprovalComponentSkip.class);
    kindToClassMap.put("ConnectionApprovalScopeSkip", V1ConnectionApprovalScopeSkip.class);
    kindToClassMap.put("Continent", V1Continent.class);
    kindToClassMap.put("Dependency", V1Dependency.class);
    kindToClassMap.put("DependencyInstance", V1DependencyInstance.class);
    kindToClassMap.put("FirewallGroup", V1FirewallGroup.class);
    kindToClassMap.put("FirewallVendor", V1FirewallVendor.class);
    kindToClassMap.put("IPAddressInstance", V1IPAddressInstance.class);
    kindToClassMap.put("LandingZone", V1LandingZone.class);
    kindToClassMap.put("LandingzoneFunctionalType", V1LandingzoneFunctionalType.class);
    kindToClassMap.put("LandingzoneType", V1LandingzoneType.class);
    kindToClassMap.put("MacroPhase", V1MacroPhase.class);
    kindToClassMap.put(
        "MappingLandingzoneFunctionaltype", V1MappingLandingzoneFunctionaltype.class);
    kindToClassMap.put("Namespace", V1Namespace.class);
    kindToClassMap.put("OutboundSNAT", V1OutboundSNAT.class);
    kindToClassMap.put("Provider", V1Provider.class);
    kindToClassMap.put("Region", V1Region.class);
    kindToClassMap.put("Role", V1Role.class);
    kindToClassMap.put("Scope", V1Scope.class);
    kindToClassMap.put("ScopeReleaseNote", V1ScopeReleaseNote.class);
    kindToClassMap.put("ScopeVersionMapping", V1ScopeVersionMapping.class);
    kindToClassMap.put("Service", V1Service.class);
    kindToClassMap.put("Servicetag", V1Servicetag.class);
    kindToClassMap.put("SubnetInstance", V1SubnetInstance.class);
    kindToClassMap.put("SubnetType", V1SubnetType.class);
    kindToClassMap.put("Target", V1Target.class);
    kindToClassMap.put("TargetType", V1TargetType.class);
    kindToClassMap.put("ZoneInstance", V1ZoneInstance.class);
    kindToClassMap.put("ZoneType", V1ZoneType.class);

    // Add other mappings here
  }

  @Override
  public Map<String, Class<? extends V1Base>> mappings() {
    return kindToClassMap;
  }
}

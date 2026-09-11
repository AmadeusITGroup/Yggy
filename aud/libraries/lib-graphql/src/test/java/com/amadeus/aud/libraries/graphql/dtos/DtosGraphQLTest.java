package com.amadeus.aud.libraries.graphql.dtos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.PageGraphQL;
import com.amadeus.aud.libraries.graphql.utils.PaginationGraphQL;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalComponentSkip;
import com.amadeus.aud.libraries.models.core.db.ConnectionApprovalScopeSkip;
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
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.ConnectivityRule;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.Endpoint;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedCIDRSpec;
import com.amadeus.aud.libraries.yaml.models.V1BlackListedURLSpec;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPortSpec;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DtosGraphQLTest {

  // ─── ConnectivityRuleGraphQL ─────────────────────────────────────────────

  @Nested
  class ConnectivityRuleGraphQLTests {

    private ObjectGroup buildObjectGroup(String name) {
      ObjectGroup og = new ObjectGroup();
      og.setName(name);
      og.setUid("uid-" + name);
      og.setType(ObjectGroup.TypeEnum.ADDRESS_GROUP);
      og.setZoneType("INTERNAL");
      og.setOwner("owner-" + name);
      return og;
    }

    private ConnectivityRule buildFullRule() {
      com.amadeus.aud.libraries.yaml.models.FirewallGroup fg =
          new com.amadeus.aud.libraries.yaml.models.FirewallGroup();
      fg.setName("fw-group-1");
      fg.setVendor("vendor-1");
      ConnectivityRule rule = new ConnectivityRule();
      rule.setName("rule-allow-http");
      rule.setUid("uid-001");
      rule.setSrcObjectGroup(buildObjectGroup("src"));
      rule.setDstObjectGroup(buildObjectGroup("dst"));
      rule.setPorts(List.of("80", "443"));
      rule.setProtocol(ConnectivityRule.ProtocolEnum.TCP);
      rule.setOwner("team-a");
      rule.setScope("internal");
      rule.setInTransitFirewallGroups(List.of(fg));
      rule.setLabels(Map.of("env", "prod", "tier", "web"));
      return rule;
    }

    @Test
    void fromNull_returnsNull() {
      assertThat(ConnectivityRuleGraphQL.from(null)).isNull();
    }

    @Test
    void fromFullRule_mapsAllFields() {
      ConnectivityRuleGraphQL dto = ConnectivityRuleGraphQL.from(buildFullRule());
      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("rule-allow-http");
      assertThat(dto.uid()).isEqualTo("uid-001");
      assertThat(dto.protocol()).isEqualTo("Tcp");
      assertThat(dto.owner()).isEqualTo("team-a");
      assertThat(dto.scope()).isEqualTo("internal");
      assertThat(dto.ports()).containsExactly("80", "443");
      assertThat(dto.labels()).containsEntry("env", "prod");
    }

    @Test
    void fromFullRule_mapsSrcAndDstObjectGroups() {
      ConnectivityRuleGraphQL dto = ConnectivityRuleGraphQL.from(buildFullRule());
      assertThat(dto.srcObjectGroup()).isNotNull();
      assertThat(dto.srcObjectGroup().name()).isEqualTo("src");
      assertThat(dto.dstObjectGroup()).isNotNull();
      assertThat(dto.dstObjectGroup().name()).isEqualTo("dst");
    }

    @Test
    void fromFullRule_mapsInTransitFirewallGroups() {
      ConnectivityRuleGraphQL dto = ConnectivityRuleGraphQL.from(buildFullRule());
      assertThat(dto.inTransitFirewallGroups()).hasSize(1);
      assertThat(dto.inTransitFirewallGroups().getFirst().name()).isEqualTo("fw-group-1");
      assertThat(dto.inTransitFirewallGroups().getFirst().vendor()).isEqualTo("vendor-1");
    }

    @Test
    void fromRuleWithNullInTransitGroups_returnsNullList() {
      ConnectivityRule rule = buildFullRule();
      rule.setInTransitFirewallGroups(null);
      assertThat(ConnectivityRuleGraphQL.from(rule).inTransitFirewallGroups()).isEmpty();
    }

    @Test
    void fromRuleWithNullScope_mapsNullScope() {
      ConnectivityRule rule = buildFullRule();
      rule.setScope(null);
      assertThat(ConnectivityRuleGraphQL.from(rule).scope()).isNull();
    }
  }

  // ─── EndpointGraphQL ─────────────────────────────────────────────────────

  @Nested
  class EndpointGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(EndpointGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      Endpoint ep = new Endpoint();
      ep.setType("CIDR");
      ep.setValue("192.168.1.0/24");
      ep.setDescription("LAN range");
      EndpointGraphQL dto = EndpointGraphQL.from(ep);
      assertThat(dto).isNotNull();
      assertThat(dto.type()).isEqualTo("CIDR");
      assertThat(dto.value()).isEqualTo("192.168.1.0/24");
      assertThat(dto.description()).isEqualTo("LAN range");
    }

    @Test
    void from_mapsNullDescription() {
      Endpoint ep = new Endpoint();
      ep.setType("FQDN");
      ep.setValue("example.com");
      ep.setDescription(null);
      assertThat(EndpointGraphQL.from(ep).description()).isNull();
    }
  }

  // ─── FirewallObjectGroupGraphQL ──────────────────────────────────────────

  @Nested
  class FirewallObjectGroupGraphQLTests {

    private ObjectGroup buildObjectGroup() {
      Endpoint ep = new Endpoint();
      ep.setType("CIDR");
      ep.setValue("10.0.0.0/8");
      ep.setDescription("private range");
      ObjectGroup og = new ObjectGroup();
      og.setName("ag-src");
      og.setUid("uid-src");
      og.setType(ObjectGroup.TypeEnum.ADDRESS_GROUP);
      og.setZoneType("INTERNAL");
      og.setEndpoints(List.of(ep));
      og.setOwner("team-a");
      og.setLabels(Map.of("env", "prod"));
      return og;
    }

    @Test
    void fromNull_returnsNull() {
      assertThat(FirewallObjectGroupGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      FirewallObjectGroupGraphQL dto = FirewallObjectGroupGraphQL.from(buildObjectGroup());
      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("ag-src");
      assertThat(dto.uid()).isEqualTo("uid-src");
      assertThat(dto.type()).isEqualTo("AddressGroup");
      assertThat(dto.zoneType()).isEqualTo("INTERNAL");
      assertThat(dto.owner()).isEqualTo("team-a");
      assertThat(dto.labels()).containsEntry("env", "prod");
    }

    @Test
    void from_mapsEndpoints() {
      FirewallObjectGroupGraphQL dto = FirewallObjectGroupGraphQL.from(buildObjectGroup());
      assertThat(dto.endpoints()).hasSize(1);
      assertThat(dto.endpoints().getFirst().type()).isEqualTo("CIDR");
      assertThat(dto.endpoints().getFirst().value()).isEqualTo("10.0.0.0/8");
      assertThat(dto.endpoints().getFirst().description()).isEqualTo("private range");
    }

    @Test
    void fromObjectGroupWithNullType_mapsNullType() {
      ObjectGroup og = buildObjectGroup();
      og.setType(null);
      assertThat(FirewallObjectGroupGraphQL.from(og).type()).isNull();
    }

    @Test
    void fromObjectGroupWithNullEndpoints_mapsNullEndpoints() {
      ObjectGroup og = buildObjectGroup();
      og.setEndpoints(null);
      assertThat(FirewallObjectGroupGraphQL.from(og).endpoints()).isEmpty();
    }
  }

  // ─── InTransitFirewallGroupGraphQL ───────────────────────────────────────

  @Nested
  class InTransitFirewallGroupGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(InTransitFirewallGroupGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsNameAndVendor() {
      com.amadeus.aud.libraries.yaml.models.FirewallGroup fg =
          new com.amadeus.aud.libraries.yaml.models.FirewallGroup();
      fg.setName("fw-ams");
      fg.setVendor("palo-alto");
      InTransitFirewallGroupGraphQL dto = InTransitFirewallGroupGraphQL.from(fg);
      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("fw-ams");
      assertThat(dto.vendor()).isEqualTo("palo-alto");
    }
  }

  // ─── DependencyGraphQL ───────────────────────────────────────────────────

  @Nested
  class DependencyGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(DependencyGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields_withInstances() {
      Dependency d = mock(Dependency.class);
      when(d.getName()).thenReturn("dep-1");
      when(d.getSourceComponentName()).thenReturn("src");
      when(d.getDestinationComponentName()).thenReturn("dst");
      com.amadeus.aud.libraries.models.core.db.base.AbstractBase<?> di =
          mock(DependencyInstance.class);
      when(di.getName()).thenReturn("di-1");
      when(d.getDependencyInstances()).thenReturn(Set.of((DependencyInstance) di));
      DependencyGraphQL dto = DependencyGraphQL.from(d);
      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("dep-1");
      assertThat(dto.sourceComponentName()).isEqualTo("src");
      assertThat(dto.destinationComponentName()).isEqualTo("dst");
      assertThat(dto.dependencyInstances()).containsExactly("di-1");
    }

    @Test
    void from_nullDependencyInstances_returnsNullSet() {
      Dependency d = mock(Dependency.class);
      when(d.getName()).thenReturn("dep-2");
      when(d.getSourceComponentName()).thenReturn("s");
      when(d.getDestinationComponentName()).thenReturn("t");
      when(d.getDependencyInstances()).thenReturn(null);
      assertThat(DependencyGraphQL.from(d).dependencyInstances()).isEmpty();
    }
  }

  // ─── ServiceGraphQL ──────────────────────────────────────────────────────

  @Nested
  class ServiceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ServiceGraphQL.from(null)).isNull();
    }

    @Test
    void from_allFields() {
      Service s = mock(Service.class);
      Component component = mock(Component.class);
      when(s.getName()).thenReturn("svc-1");
      when(s.getComment()).thenReturn("service description");
      when(s.getComponent()).thenReturn(component);
      when(s.getComponentName()).thenReturn("comp");
      when(component.getDeprecatedStatus()).thenReturn(DeprecatedStatus.WARNING);
      when(s.getApplicationIdName()).thenReturn("appId");
      when(s.getServiceNowId()).thenReturn("RITM001");
      when(s.getProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.TCP);
      when(s.getPorts()).thenReturn("443");
      when(s.getApprovedProtocol()).thenReturn(V1ServiceSpec.ProtocolEnum.UDP);
      when(s.getDeprecatedMessage()).thenReturn("will be removed");
      when(s.isPrivate()).thenReturn(true);
      when(s.isPci()).thenReturn(true);
      when(s.isPii()).thenReturn(false);
      when(s.isEncrypted()).thenReturn(true);
      when(s.isDbSynchro()).thenReturn(false);
      ServiceGraphQL dto = ServiceGraphQL.from(s);
      assertThat(dto.name()).isEqualTo("svc-1");
      assertThat(dto.description()).isEqualTo("service description");
      assertThat(dto.componentName()).isEqualTo("comp");
      assertThat(dto.componentDeprecatedStatus()).isEqualTo("WARNING");
      assertThat(dto.applicationIdName()).isEqualTo("appId");
      assertThat(dto.serviceNowId()).isEqualTo("RITM001");
      assertThat(dto.protocol()).isEqualTo("Tcp");
      assertThat(dto.ports()).isEqualTo("443");
      assertThat(dto.approvedProtocol()).isEqualTo("Udp");
      assertThat(dto.deprecatedMessage()).isEqualTo("will be removed");
      assertThat(dto.isPrivate()).isTrue();
      assertThat(dto.isPci()).isTrue();
      assertThat(dto.isPii()).isFalse();
      assertThat(dto.isEncrypted()).isTrue();
      assertThat(dto.isDbSynchro()).isFalse();
    }

    @Test
    void from_nullProtocolAndDeprecatedStatus_mapsToNull() {
      Service s = mock(Service.class);
      when(s.getName()).thenReturn("svc-2");
      when(s.getComment()).thenReturn(null);
      when(s.getComponent()).thenReturn(null);
      when(s.getComponentName()).thenReturn(null);
      when(s.getApplicationIdName()).thenReturn(null);
      when(s.getServiceNowId()).thenReturn(null);
      when(s.getProtocol()).thenReturn(null);
      when(s.getPorts()).thenReturn(null);
      when(s.getApprovedProtocol()).thenReturn(null);
      when(s.getDeprecatedStatus()).thenReturn(null);
      when(s.getDeprecatedMessage()).thenReturn(null);
      when(s.isPrivate()).thenReturn(false);
      when(s.isPci()).thenReturn(false);
      when(s.isPii()).thenReturn(false);
      when(s.isEncrypted()).thenReturn(false);
      when(s.isDbSynchro()).thenReturn(false);
      ServiceGraphQL dto = ServiceGraphQL.from(s);
      assertThat(dto.description()).isNull();
      assertThat(dto.componentDeprecatedStatus()).isNull();
      assertThat(dto.protocol()).isNull();
      assertThat(dto.approvedProtocol()).isNull();
      assertThat(dto.deprecatedStatus()).isNull();
    }
  }

  // ─── ApplicationIdGraphQL ────────────────────────────────────────────────

  @Nested
  class ApplicationIdGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ApplicationIdGraphQL.from(null)).isNull();
    }

    @Test
    void from_withServices() {
      Service svc1 = mockService("svc-1");
      ApplicationId a = mock(ApplicationId.class);
      when(a.id()).thenReturn("app-1-id");
      when(a.getName()).thenReturn("app-1");
      when(a.getScope()).thenReturn(null);
      when(a.getServices()).thenReturn(Set.of(svc1));
      ApplicationIdGraphQL dto = ApplicationIdGraphQL.from(a);
      assertThat(dto.name()).isEqualTo("app-1");
      assertThat(dto.services()).hasSize(1);
    }

    @Test
    void from_nullServices_returnsNullSet() {
      ApplicationId a = mock(ApplicationId.class);
      when(a.id()).thenReturn("app-2-id");
      when(a.getName()).thenReturn("app-2");
      when(a.getScope()).thenReturn(null);
      when(a.getServices()).thenReturn(null);
      assertThat(ApplicationIdGraphQL.from(a).services()).isEmpty();
    }
  }

  // ─── ApplicationPhaseGraphQL ─────────────────────────────────────────────

  @Nested
  class ApplicationPhaseGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ApplicationPhaseGraphQL.from(null)).isNull();
    }

    @Test
    void from_allFields() {
      MacroPhase mp = mock(MacroPhase.class);
      when(mp.getName()).thenReturn("PRD");
      when(mp.getAutomationOrder()).thenReturn(null);
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-1");
      ApplicationPhase ap = mock(ApplicationPhase.class);
      when(ap.getName()).thenReturn("PRD");
      when(ap.isCustomerFacing()).thenReturn(true);
      when(ap.getMacroPhase()).thenReturn(mp);
      when(ap.getComponentInstances()).thenReturn(Set.of(ci));
      ApplicationPhaseGraphQL dto = ApplicationPhaseGraphQL.from(ap);
      assertThat(dto.name()).isEqualTo("PRD");
      assertThat(dto.isCustomerFacing()).isTrue();
      assertThat(dto.macroPhase()).isNotNull();
      assertThat(dto.componentInstances()).containsExactly("ci-1");
    }

    @Test
    void from_nullComponentInstances() {
      ApplicationPhase ap = mock(ApplicationPhase.class);
      when(ap.getName()).thenReturn("ap");
      when(ap.isCustomerFacing()).thenReturn(false);
      when(ap.getMacroPhase()).thenReturn(null);
      when(ap.getComponentInstances()).thenReturn(null);
      ApplicationPhaseGraphQL dto = ApplicationPhaseGraphQL.from(ap);
      assertThat(dto.componentInstances()).isEmpty();
      assertThat(dto.macroPhase()).isNull();
    }
  }

  // ─── BlockedPortGraphQL ──────────────────────────────────────────────────

  @Nested
  class BlockedPortGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(BlockedPortGraphQL.from(null)).isNull();
    }

    @Test
    void from_withProtocol() {
      BlockedPort bp = mock(BlockedPort.class);
      when(bp.getName()).thenReturn("deny-22");
      when(bp.getPort()).thenReturn(22);
      when(bp.getProtocol()).thenReturn(V1BlockedPortSpec.ProtocolEnum.TCP);
      BlockedPortGraphQL dto = BlockedPortGraphQL.from(bp);
      assertThat(dto.name()).isEqualTo("deny-22");
      assertThat(dto.port()).isEqualTo(22);
      assertThat(dto.protocol()).isEqualTo("Tcp");
    }

    @Test
    void from_nullProtocol() {
      BlockedPort bp = mock(BlockedPort.class);
      when(bp.getName()).thenReturn("deny-x");
      when(bp.getPort()).thenReturn(80);
      when(bp.getProtocol()).thenReturn(null);
      assertThat(BlockedPortGraphQL.from(bp).protocol()).isNull();
    }
  }

  // ─── ContinentGraphQL ────────────────────────────────────────────────────

  @Nested
  class ContinentGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ContinentGraphQL.from(null)).isNull();
    }

    @Test
    void from_allFields() {
      Continent c = mock(Continent.class);
      when(c.getName()).thenReturn("EU");
      when(c.getRegionNames()).thenReturn(List.of("EMEA"));
      when(c.getTargetNames()).thenReturn(List.of("target-1"));
      ContinentGraphQL dto = ContinentGraphQL.from(c);
      assertThat(dto.name()).isEqualTo("EU");
      assertThat(dto.regions()).containsExactly("EMEA");
      assertThat(dto.targets()).containsExactly("target-1");
    }
  }

  // ─── ConnectionApprovalComponentSkipGraphQL ──────────────────────────────

  @Nested
  class ConnectionApprovalComponentSkipGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ConnectionApprovalComponentSkipGraphQL.from(null)).isNull();
    }

    @Test
    void from_allFields() {
      ConnectionApprovalComponentSkip cacs = mock(ConnectionApprovalComponentSkip.class);
      when(cacs.id()).thenReturn("456");
      when(cacs.getName()).thenReturn("skip-1");
      when(cacs.getSkipSource()).thenReturn(true);
      when(cacs.getSkipDestination()).thenReturn(false);
      when(cacs.getStatus()).thenReturn("ACTIVE");
      ConnectionApprovalComponentSkipGraphQL dto =
          ConnectionApprovalComponentSkipGraphQL.from(cacs);
      assertThat(dto.id()).isEqualTo("456");
      assertThat(dto.name()).isEqualTo("skip-1");
      assertThat(dto.isSkipSource()).isTrue();
      assertThat(dto.isSkipDestination()).isFalse();
      assertThat(dto.status()).isEqualTo("ACTIVE");
    }
  }

  // ─── ConnectionApprovalScopeSkipGraphQL ──────────────────────────────────

  @Nested
  class ConnectionApprovalScopeSkipGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ConnectionApprovalScopeSkipGraphQL.from(null)).isNull();
    }

    @Test
    void from_allFields() {
      ConnectionApprovalScopeSkip cass = mock(ConnectionApprovalScopeSkip.class);
      when(cass.id()).thenReturn("789");
      when(cass.getName()).thenReturn("skip-scope-1");
      when(cass.getSkipSource()).thenReturn(false);
      when(cass.getSkipDestination()).thenReturn(true);
      when(cass.getStatus()).thenReturn("INACTIVE");
      ConnectionApprovalScopeSkipGraphQL dto = ConnectionApprovalScopeSkipGraphQL.from(cass);
      assertThat(dto.id()).isEqualTo("789");
      assertThat(dto.name()).isEqualTo("skip-scope-1");
      assertThat(dto.isSkipSource()).isFalse();
      assertThat(dto.isSkipDestination()).isTrue();
      assertThat(dto.status()).isEqualTo("INACTIVE");
    }
  }

  // ─── ConnectionGraphQL ───────────────────────────────────────────────────

  @Nested
  class ConnectionGraphQLTests {

    private ZoneType buildZoneType(String name) {
      ZoneType zt = mock(ZoneType.class);
      when(zt.getName()).thenReturn(name);
      when(zt.isTechnical()).thenReturn(false);
      when(zt.isReal()).thenReturn(true);
      when(zt.isPaas()).thenReturn(false);
      when(zt.getComponent()).thenReturn(null);
      when(zt.getConnections()).thenReturn(null);
      when(zt.getZoneInstances()).thenReturn(null);
      when(zt.getComponentInstances()).thenReturn(null);
      return zt;
    }

    @Test
    void fromNull_returnsNull() {
      assertThat(ConnectionGraphQL.from(null)).isNull();
    }

    @Test
    void from_allFields() {
      ZoneType srcZt = buildZoneType("dmz");
      ZoneType dstZt = buildZoneType("app");
      Connection c = mock(Connection.class);
      when(c.getName()).thenReturn("conn-1");
      when(c.getFirewallLevel()).thenReturn(V1ConnectionSpec.LevelEnum.L7);
      when(c.getSecurityControl()).thenReturn(V1ConnectionSpec.ControlEnum.GREEN);
      when(c.getConnectionType()).thenReturn(V1ConnectionSpec.TypeEnum.INTERNAL);
      when(c.getSourceZoneType()).thenReturn(srcZt);
      when(c.getDestinationZoneType()).thenReturn(dstZt);
      ConnectionGraphQL dto = ConnectionGraphQL.from(c);
      assertThat(dto.name()).isEqualTo("conn-1");
      assertThat(dto.firewallLevel()).isEqualTo("L7");
      assertThat(dto.securityControl()).isEqualTo("green");
      assertThat(dto.connectionType()).isEqualTo("Internal");
      assertThat(dto.sourceZoneType()).isNotNull();
      assertThat(dto.destinationZoneType()).isNotNull();
    }

    @Test
    void from_nullEnums_mapsToNull() {
      Connection c = mock(Connection.class);
      when(c.getName()).thenReturn("conn-2");
      when(c.getFirewallLevel()).thenReturn(null);
      when(c.getSecurityControl()).thenReturn(null);
      when(c.getConnectionType()).thenReturn(null);
      when(c.getSourceZoneType()).thenReturn(null);
      when(c.getDestinationZoneType()).thenReturn(null);
      ConnectionGraphQL dto = ConnectionGraphQL.from(c);
      assertThat(dto.firewallLevel()).isNull();
      assertThat(dto.securityControl()).isNull();
      assertThat(dto.connectionType()).isNull();
      assertThat(dto.sourceZoneType()).isNull();
      assertThat(dto.destinationZoneType()).isNull();
    }
  }

  // ─── ConnectionPhaseGraphQL ──────────────────────────────────────────────

  @Nested
  class ConnectionPhaseGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ConnectionPhaseGraphQL.from(null)).isNull();
    }

    @Test
    void from_withControl() {
      ConnectionPhase cp = mock(ConnectionPhase.class);
      when(cp.getName()).thenReturn("cp-1");
      when(cp.getControl()).thenReturn(V1ConnectionPhaseSpec.ControlEnum.GREEN);
      when(cp.getSourcePhaseName()).thenReturn("PRD");
      when(cp.getDestinationPhaseName()).thenReturn("TST");
      ConnectionPhaseGraphQL dto = ConnectionPhaseGraphQL.from(cp);
      assertThat(dto.name()).isEqualTo("cp-1");
      assertThat(dto.control()).isEqualTo("green");
      assertThat(dto.sourcePhase()).isEqualTo("PRD");
      assertThat(dto.destinationPhase()).isEqualTo("TST");
    }

    @Test
    void from_nullControl_mapsToNull() {
      ConnectionPhase cp = mock(ConnectionPhase.class);
      when(cp.getName()).thenReturn("cp-2");
      when(cp.getControl()).thenReturn(null);
      when(cp.getSourcePhaseName()).thenReturn(null);
      when(cp.getDestinationPhaseName()).thenReturn(null);
      assertThat(ConnectionPhaseGraphQL.from(cp).control()).isNull();
    }
  }

  // ─── DependencyInstanceGraphQL ───────────────────────────────────────────

  @Nested
  class DependencyInstanceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(DependencyInstanceGraphQL.from(null)).isNull();
    }

    @Test
    void from_allBooleanFields_andServices() {
      DependencyInstance di = mock(DependencyInstance.class);
      when(di.getName()).thenReturn("di-1");
      when(di.getDependencyName()).thenReturn("dep");
      when(di.getSourceComponentInstanceName()).thenReturn("src-ci");
      when(di.getTargetComponentInstanceName()).thenReturn("tgt-ci");
      when(di.getViaComponentInstanceName()).thenReturn("via-ci");
      when(di.getViaServiceName()).thenReturn("via-svc");
      when(di.isSourceAsComponent()).thenReturn(true);
      when(di.isSourceLimitedToLegacyOnly()).thenReturn(false);
      when(di.isSourceLimitedToCloudOnly()).thenReturn(true);
      when(di.isSourceLimitedToLandingZone()).thenReturn(false);
      when(di.isSourceLimitedToTarget()).thenReturn(true);
      when(di.isSourceLimitedToContinent()).thenReturn(false);
      when(di.isSourceLimitedToMacroPhase()).thenReturn(true);
      when(di.isSourceLimitedToSelector()).thenReturn(false);
      when(di.isTargetAsComponent()).thenReturn(true);
      when(di.isTargetLimitedToLegacyOnly()).thenReturn(false);
      when(di.isTargetLimitedToCloudOnly()).thenReturn(true);
      when(di.isTargetLimitedToSourceNamespace()).thenReturn(false);
      when(di.isTargetLimitedToSourcePaas()).thenReturn(true);
      when(di.isTargetLimitedToSourceSecurityZone()).thenReturn(false);
      when(di.isTargetLimitedToSourceApplicationPhase()).thenReturn(true);
      when(di.isTargetLimitedToSourceLandingZone()).thenReturn(false);
      when(di.isTargetLimitedToSourceTarget()).thenReturn(true);
      when(di.isTargetLimitedToSourceContinent()).thenReturn(false);
      when(di.isTargetLimitedToSourceMacroPhase()).thenReturn(true);
      Service svcX = mockService("svc-x");
      when(di.getServices()).thenReturn(Set.of(svcX));
      DependencyInstanceGraphQL dto = DependencyInstanceGraphQL.from(di);
      assertThat(dto.name()).isEqualTo("di-1");
      assertThat(dto.dependencyName()).isEqualTo("dep");
      assertThat(dto.sourceComponentInstanceName()).isEqualTo("src-ci");
      assertThat(dto.targetComponentInstanceName()).isEqualTo("tgt-ci");
      assertThat(dto.viaComponentInstanceName()).isEqualTo("via-ci");
      assertThat(dto.viaServiceName()).isEqualTo("via-svc");
      assertThat(dto.isSourceAsComponent()).isTrue();
      assertThat(dto.isSourceLimitedToLegacyOnly()).isFalse();
      assertThat(dto.isSourceLimitedToCloudOnly()).isTrue();
      assertThat(dto.isSourceLimitedToLandingZone()).isFalse();
      assertThat(dto.isSourceLimitedToTarget()).isTrue();
      assertThat(dto.isSourceLimitedToContinent()).isFalse();
      assertThat(dto.isSourceLimitedToMacroPhase()).isTrue();
      assertThat(dto.isSourceLimitedToSelector()).isFalse();
      assertThat(dto.isTargetAsComponent()).isTrue();
      assertThat(dto.isTargetLimitedToLegacyOnly()).isFalse();
      assertThat(dto.isTargetLimitedToCloudOnly()).isTrue();
      assertThat(dto.isTargetLimitedToSourceNamespace()).isFalse();
      assertThat(dto.isTargetLimitedToSourcePaas()).isTrue();
      assertThat(dto.isTargetLimitedToSourceSecurityZone()).isFalse();
      assertThat(dto.isTargetLimitedToSourceApplicationPhase()).isTrue();
      assertThat(dto.isTargetLimitedToSourceLandingZone()).isFalse();
      assertThat(dto.isTargetLimitedToSourceTarget()).isTrue();
      assertThat(dto.isTargetLimitedToSourceContinent()).isFalse();
      assertThat(dto.isTargetLimitedToSourceMacroPhase()).isTrue();
      assertThat(dto.services()).hasSize(1);
    }

    @Test
    void from_nullServices_returnsNullSet() {
      DependencyInstance di = mock(DependencyInstance.class);
      when(di.getName()).thenReturn("di-2");
      when(di.getDependencyName()).thenReturn(null);
      when(di.getSourceComponentInstanceName()).thenReturn(null);
      when(di.getTargetComponentInstanceName()).thenReturn(null);
      when(di.getViaComponentInstanceName()).thenReturn(null);
      when(di.getViaServiceName()).thenReturn(null);
      when(di.isSourceAsComponent()).thenReturn(false);
      when(di.isSourceLimitedToLegacyOnly()).thenReturn(false);
      when(di.isSourceLimitedToCloudOnly()).thenReturn(false);
      when(di.isSourceLimitedToLandingZone()).thenReturn(false);
      when(di.isSourceLimitedToTarget()).thenReturn(false);
      when(di.isSourceLimitedToContinent()).thenReturn(false);
      when(di.isSourceLimitedToMacroPhase()).thenReturn(false);
      when(di.isSourceLimitedToSelector()).thenReturn(false);
      when(di.isTargetAsComponent()).thenReturn(false);
      when(di.isTargetLimitedToLegacyOnly()).thenReturn(false);
      when(di.isTargetLimitedToCloudOnly()).thenReturn(false);
      when(di.isTargetLimitedToSourceNamespace()).thenReturn(false);
      when(di.isTargetLimitedToSourcePaas()).thenReturn(false);
      when(di.isTargetLimitedToSourceSecurityZone()).thenReturn(false);
      when(di.isTargetLimitedToSourceApplicationPhase()).thenReturn(false);
      when(di.isTargetLimitedToSourceLandingZone()).thenReturn(false);
      when(di.isTargetLimitedToSourceTarget()).thenReturn(false);
      when(di.isTargetLimitedToSourceContinent()).thenReturn(false);
      when(di.isTargetLimitedToSourceMacroPhase()).thenReturn(false);
      when(di.getServices()).thenReturn(null);
      assertThat(DependencyInstanceGraphQL.from(di).services()).isEmpty();
    }
  }

  // ─── FirewallGroupGraphQL ────────────────────────────────────────────────

  @Nested
  class FirewallGroupGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(FirewallGroupGraphQL.from(null)).isNull();
    }

    @Test
    void from_withZoneInstances() {
      ZoneInstance zi = mock(ZoneInstance.class);
      when(zi.getName()).thenReturn("zi-1");
      FirewallGroup fg = mock(FirewallGroup.class);
      when(fg.getName()).thenReturn("fg-1");
      when(fg.getExternalIps()).thenReturn("1.2.3.4");
      when(fg.getFirewallVendorName()).thenReturn("Palo");
      when(fg.getTargetName()).thenReturn("tgt-1");
      when(fg.getZoneInstances()).thenReturn(Set.of(zi));
      FirewallGroupGraphQL dto = FirewallGroupGraphQL.from(fg);
      assertThat(dto.name()).isEqualTo("fg-1");
      assertThat(dto.externalIps()).isEqualTo("1.2.3.4");
      assertThat(dto.firewallVendor()).isEqualTo("Palo");
      assertThat(dto.target()).isEqualTo("tgt-1");
      assertThat(dto.zoneInstances()).containsExactly("zi-1");
    }

    @Test
    void from_nullZoneInstances() {
      FirewallGroup fg = mock(FirewallGroup.class);
      when(fg.getName()).thenReturn("fg-2");
      when(fg.getExternalIps()).thenReturn(null);
      when(fg.getFirewallVendorName()).thenReturn(null);
      when(fg.getTargetName()).thenReturn(null);
      when(fg.getZoneInstances()).thenReturn(null);
      assertThat(FirewallGroupGraphQL.from(fg).zoneInstances()).isEmpty();
    }
  }

  // ─── FirewallVendorGraphQL ───────────────────────────────────────────────

  @Nested
  class FirewallVendorGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(FirewallVendorGraphQL.from(null)).isNull();
    }

    @Test
    void from_withGroups() {
      ZoneInstance zi = mock(ZoneInstance.class);
      when(zi.getName()).thenReturn("zi-1");
      FirewallGroup fg = mock(FirewallGroup.class);
      when(fg.getName()).thenReturn("fg-1");
      when(fg.getExternalIps()).thenReturn(null);
      when(fg.getFirewallVendorName()).thenReturn("Palo");
      when(fg.getTargetName()).thenReturn(null);
      when(fg.getZoneInstances()).thenReturn(Set.of(zi));
      FirewallVendor fv = mock(FirewallVendor.class);
      when(fv.getName()).thenReturn("Palo");
      when(fv.getFirewallGroups()).thenReturn(Set.of(fg));
      FirewallVendorGraphQL dto = FirewallVendorGraphQL.from(fv);
      assertThat(dto.name()).isEqualTo("Palo");
      assertThat(dto.firewallGroups()).hasSize(1);
    }

    @Test
    void from_nullGroups_returnsNullSet() {
      FirewallVendor fv = mock(FirewallVendor.class);
      when(fv.getName()).thenReturn("Cisco");
      when(fv.getFirewallGroups()).thenReturn(null);
      assertThat(FirewallVendorGraphQL.from(fv).firewallGroups()).isEmpty();
    }
  }

  // ─── IPAddressInstanceGraphQL ────────────────────────────────────────────

  @Nested
  class IPAddressInstanceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(IPAddressInstanceGraphQL.from(null)).isNull();
    }

    @Test
    void from_withAdvertisedTo() {
      IPAddressInstance ip = mock(IPAddressInstance.class);
      when(ip.getName()).thenReturn("1.2.3.4");
      when(ip.getHost()).thenReturn("my-host");
      when(ip.getAdvertisedTo()).thenReturn(V1IPAddressInstanceSpec.AdvertisedToEnum.INTERNET);
      when(ip.getComponentInstanceName()).thenReturn("ci-1");
      IPAddressInstanceGraphQL dto = IPAddressInstanceGraphQL.from(ip);
      assertThat(dto.name()).isEqualTo("1.2.3.4");
      assertThat(dto.host()).isEqualTo("my-host");
      assertThat(dto.advertisedTo()).isEqualTo("internet");
      assertThat(dto.componentInstance()).isEqualTo("ci-1");
    }

    @Test
    void from_nullAdvertisedTo_mapsToNull() {
      IPAddressInstance ip = mock(IPAddressInstance.class);
      when(ip.getName()).thenReturn("5.6.7.8");
      when(ip.getHost()).thenReturn(null);
      when(ip.getAdvertisedTo()).thenReturn(null);
      when(ip.getComponentInstanceName()).thenReturn(null);
      assertThat(IPAddressInstanceGraphQL.from(ip).advertisedTo()).isNull();
    }
  }

  // ─── LandingZoneFunctionalTypeGraphQL ────────────────────────────────────

  @Nested
  class LandingZoneFunctionalTypeGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(LandingZoneFunctionalTypeGraphQL.from(null)).isNull();
    }

    @Test
    void from_withLandingZones() {
      LandingZone lz = mock(LandingZone.class);
      when(lz.getName()).thenReturn("lz-1");
      when(lz.getTargetName()).thenReturn("t1");
      when(lz.getMacroPhaseName()).thenReturn("PRD");
      when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(null);
      LandingZoneFunctionalType lzft = mock(LandingZoneFunctionalType.class);
      when(lzft.getName()).thenReturn("lzft-1");
      when(lzft.isGenerated()).thenReturn(true);
      when(lzft.getLandingZones()).thenReturn(Set.of(lz));
      LandingZoneFunctionalTypeGraphQL dto = LandingZoneFunctionalTypeGraphQL.from(lzft);
      assertThat(dto.name()).isEqualTo("lzft-1");
      assertThat(dto.isGenerated()).isTrue();
      assertThat(dto.landingZones()).hasSize(1);
    }

    @Test
    void from_nullLandingZones_returnsNullSet() {
      LandingZoneFunctionalType lzft = mock(LandingZoneFunctionalType.class);
      when(lzft.getName()).thenReturn("lzft-2");
      when(lzft.isGenerated()).thenReturn(false);
      when(lzft.getLandingZones()).thenReturn(null);
      assertThat(LandingZoneFunctionalTypeGraphQL.from(lzft).landingZones()).isEmpty();
    }
  }

  // ─── LandingZoneTypeGraphQL ──────────────────────────────────────────────

  @Nested
  class LandingZoneTypeGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(LandingZoneTypeGraphQL.from(null)).isNull();
    }

    @Test
    void from_withLandingZones() {
      LandingZone lz = mock(LandingZone.class);
      when(lz.getName()).thenReturn("lz-1");
      when(lz.getTargetName()).thenReturn("t1");
      when(lz.getMacroPhaseName()).thenReturn("PRD");
      when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(null);
      LandingZoneType lzt = mock(LandingZoneType.class);
      when(lzt.getName()).thenReturn("lzt-1");
      when(lzt.getLandingZones()).thenReturn(Set.of(lz));
      LandingZoneTypeGraphQL dto = LandingZoneTypeGraphQL.from(lzt);
      assertThat(dto.name()).isEqualTo("lzt-1");
      assertThat(dto.landingZones()).hasSize(1);
    }

    @Test
    void from_nullLandingZones_returnsNullSet() {
      LandingZoneType lzt = mock(LandingZoneType.class);
      when(lzt.getName()).thenReturn("lzt-2");
      when(lzt.getLandingZones()).thenReturn(null);
      assertThat(LandingZoneTypeGraphQL.from(lzt).landingZones()).isEmpty();
    }
  }

  // ─── AutomaticTemplateInstanceGraphQL ────────────────────────────────────

  @Nested
  class AutomaticTemplateInstanceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(AutomaticTemplateInstanceGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      AutomaticTemplateInstance ati = mock(AutomaticTemplateInstance.class);
      when(ati.getName()).thenReturn("ati-main");
      Component comp = mock(Component.class);
      when(comp.getName()).thenReturn("comp-api");
      when(ati.getComponent()).thenReturn(comp);
      LandingZone lz = mockLandingZone();
      when(ati.getLandingZone()).thenReturn(lz);
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-app");
      when(ati.getComponentInstances()).thenReturn(Set.of(ci));
      when(ati.getAssociatedFunctionalTypes()).thenReturn(List.of(Set.of("Cloud")));
      when(ati.getExcludedFunctionalTypes()).thenReturn(List.of(Set.of("Legacy")));
      AutomaticTemplateInstanceGraphQL dto = AutomaticTemplateInstanceGraphQL.from(ati);
      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("ati-main");
      assertThat(dto.component()).isEqualTo("comp-api");
      assertThat(dto.landingZone()).isEqualTo("lz-ams");
      assertThat(dto.componentInstances()).containsExactly("ci-app");
      assertThat(dto.associatedFunctionalTypes()).containsExactly(Set.of("Cloud"));
      assertThat(dto.excludedFunctionalTypes()).containsExactly(Set.of("Legacy"));
    }

    @Test
    void from_nullCollections() {
      AutomaticTemplateInstance ati = mock(AutomaticTemplateInstance.class);
      when(ati.getName()).thenReturn("ati-minimal");
      when(ati.getComponent()).thenReturn(null);
      when(ati.getLandingZone()).thenReturn(null);
      when(ati.getComponentInstances()).thenReturn(null);
      when(ati.getAssociatedFunctionalTypes()).thenReturn(null);
      when(ati.getExcludedFunctionalTypes()).thenReturn(null);
      AutomaticTemplateInstanceGraphQL dto = AutomaticTemplateInstanceGraphQL.from(ati);
      assertThat(dto.component()).isNull();
      assertThat(dto.landingZone()).isNull();
      assertThat(dto.componentInstances()).isEmpty();
    }
  }

  // ─── MacroPhaseGraphQL ───────────────────────────────────────────────────

  @Nested
  class MacroPhaseGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(MacroPhaseGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      MacroPhase mp = mock(MacroPhase.class);
      when(mp.getName()).thenReturn("tst");
      when(mp.getAutomationOrder()).thenReturn(1);
      ApplicationPhase ap = mock(ApplicationPhase.class);
      when(ap.getName()).thenReturn("phase-prod");
      when(mp.getApplicationPhases()).thenReturn(Set.of(ap));
      Target t = mock(Target.class);
      when(t.getName()).thenReturn("target-ams");
      when(mp.getTargets()).thenReturn(Set.of(t));
      ConnectionPhase cp = mock(ConnectionPhase.class);
      when(cp.getName()).thenReturn("conn-phase-1");
      when(mp.getConnectionPhases()).thenReturn(Set.of(cp));
      MacroPhaseGraphQL dto = MacroPhaseGraphQL.from(mp);
      assertThat(dto.name()).isEqualTo("tst");
      assertThat(dto.automationOrder()).isOne();
      assertThat(dto.applicationPhases()).containsExactly("phase-prod");
      assertThat(dto.targets()).containsExactly("target-ams");
      assertThat(dto.connectionPhases()).containsExactly("conn-phase-1");
    }

    @Test
    void from_nullCollections() {
      MacroPhase mp = mock(MacroPhase.class);
      when(mp.getName()).thenReturn("tst");
      when(mp.getAutomationOrder()).thenReturn(null);
      when(mp.getApplicationPhases()).thenReturn(null);
      when(mp.getTargets()).thenReturn(null);
      when(mp.getConnectionPhases()).thenReturn(null);
      MacroPhaseGraphQL dto = MacroPhaseGraphQL.from(mp);
      assertThat(dto.applicationPhases()).isEmpty();
      assertThat(dto.targets()).isEmpty();
      assertThat(dto.connectionPhases()).isEmpty();
    }
  }

  // ─── NamespaceGraphQL ────────────────────────────────────────────────────

  @Nested
  class NamespaceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(NamespaceGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      Namespace ns = mock(Namespace.class);
      when(ns.getName()).thenReturn("ns-default");
      when(ns.isShared()).thenReturn(true);
      when(ns.isAutomaticallyGenerated()).thenReturn(false);
      Scope scope = mock(Scope.class);
      when(scope.getName()).thenReturn("scope-prod");
      when(ns.getAuthorizedScopes()).thenReturn(Set.of(scope));
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-app");
      when(ns.getComponentInstances()).thenReturn(Set.of(ci));
      NamespaceGraphQL dto = NamespaceGraphQL.from(ns);
      assertThat(dto.name()).isEqualTo("ns-default");
      assertThat(dto.isShared()).isTrue();
      assertThat(dto.isAutomaticallyGenerated()).isFalse();
      assertThat(dto.authorizedScopes()).containsExactly("scope-prod");
      assertThat(dto.componentInstances()).containsExactly("ci-app");
    }

    @Test
    void from_nullCollections() {
      Namespace ns = mock(Namespace.class);
      when(ns.getName()).thenReturn("ns-x");
      when(ns.isShared()).thenReturn(false);
      when(ns.isAutomaticallyGenerated()).thenReturn(true);
      when(ns.getAuthorizedScopes()).thenReturn(null);
      when(ns.getComponentInstances()).thenReturn(null);
      NamespaceGraphQL dto = NamespaceGraphQL.from(ns);
      assertThat(dto.authorizedScopes()).isEmpty();
      assertThat(dto.componentInstances()).isEmpty();
    }
  }

  // ─── ZoneInstanceGraphQL ─────────────────────────────────────────────────

  @Nested
  class ZoneInstanceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ZoneInstanceGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      ZoneInstance zi = mock(ZoneInstance.class);
      when(zi.getName()).thenReturn("zi-main");
      when(zi.getLandingZoneName()).thenReturn("lz-ams");
      when(zi.getZoneTypeName()).thenReturn("dmz");
      when(zi.getFirewallGroupName()).thenReturn("fg-main");
      when(zi.isNsgNeeded()).thenReturn(true);
      SubnetInstance si = mock(SubnetInstance.class);
      when(si.getName()).thenReturn("subnet-a");
      when(zi.getSubnetInstances()).thenReturn(Set.of(si));
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-app");
      when(zi.getComponentInstances()).thenReturn(Set.of(ci));
      ZoneInstanceGraphQL dto = ZoneInstanceGraphQL.from(zi);
      assertThat(dto.name()).isEqualTo("zi-main");
      assertThat(dto.landingZoneName()).isEqualTo("lz-ams");
      assertThat(dto.zoneTypeName()).isEqualTo("dmz");
      assertThat(dto.firewallGroupName()).isEqualTo("fg-main");
      assertThat(dto.isNsgNeeded()).isTrue();
      assertThat(dto.subnetInstances()).containsExactly("subnet-a");
      assertThat(dto.componentInstances()).containsExactly("ci-app");
    }

    @Test
    void from_nullCollections() {
      ZoneInstance zi = mock(ZoneInstance.class);
      when(zi.getName()).thenReturn("zi-x");
      when(zi.getLandingZoneName()).thenReturn(null);
      when(zi.getZoneTypeName()).thenReturn(null);
      when(zi.getFirewallGroupName()).thenReturn(null);
      when(zi.isNsgNeeded()).thenReturn(false);
      when(zi.getSubnetInstances()).thenReturn(null);
      when(zi.getComponentInstances()).thenReturn(null);
      ZoneInstanceGraphQL dto = ZoneInstanceGraphQL.from(zi);
      assertThat(dto.subnetInstances()).isEmpty();
      assertThat(dto.componentInstances()).isEmpty();
    }
  }

  // ─── ZoneTypeGraphQL ─────────────────────────────────────────────────────

  @Nested
  class ZoneTypeGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ZoneTypeGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      ZoneType zt = mockZoneType();
      Component comp = mock(Component.class);
      when(comp.getName()).thenReturn("comp-api");
      when(zt.getComponent()).thenReturn(comp);
      when(zt.getConnections()).thenReturn(null);
      when(zt.getZoneInstances()).thenReturn(null);
      when(zt.getComponentInstances()).thenReturn(null);
      ZoneTypeGraphQL dto = ZoneTypeGraphQL.from(zt);
      assertThat(dto.name()).isEqualTo("dmz");
      assertThat(dto.isTechnical()).isFalse();
      assertThat(dto.isReal()).isTrue();
      assertThat(dto.isPaas()).isFalse();
      assertThat(dto.componentName()).isEqualTo("comp-api");
      assertThat(dto.connections()).isEmpty();
      assertThat(dto.zoneInstances()).isEmpty();
      assertThat(dto.componentInstances()).isEmpty();
    }

    @Test
    void from_nullComponent() {
      ZoneType zt = mockZoneType();
      when(zt.getComponent()).thenReturn(null);
      when(zt.getConnections()).thenReturn(null);
      when(zt.getZoneInstances()).thenReturn(null);
      when(zt.getComponentInstances()).thenReturn(null);
      assertThat(ZoneTypeGraphQL.from(zt).componentName()).isNull();
    }

    @Test
    void from_withNonNullCollections() {
      Connection conn = mock(Connection.class);
      when(conn.getName()).thenReturn("conn-1");
      when(conn.getFirewallLevel()).thenReturn(null);
      when(conn.getSecurityControl()).thenReturn(null);
      when(conn.getConnectionType()).thenReturn(null);
      when(conn.getSourceZoneType()).thenReturn(null);
      when(conn.getDestinationZoneType()).thenReturn(null);
      ZoneInstance zi = mock(ZoneInstance.class);
      when(zi.getName()).thenReturn("zi-1");
      when(zi.getLandingZoneName()).thenReturn(null);
      when(zi.getZoneTypeName()).thenReturn(null);
      when(zi.getFirewallGroupName()).thenReturn(null);
      when(zi.isNsgNeeded()).thenReturn(false);
      when(zi.getSubnetInstances()).thenReturn(null);
      when(zi.getComponentInstances()).thenReturn(null);
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-1");
      ZoneType zt = mockZoneType();
      when(zt.getComponent()).thenReturn(null);
      when(zt.getConnections()).thenReturn(Set.of(conn));
      when(zt.getZoneInstances()).thenReturn(Set.of(zi));
      when(zt.getComponentInstances()).thenReturn(Set.of(ci));
      ZoneTypeGraphQL dto = ZoneTypeGraphQL.from(zt);
      assertThat(dto.connections()).containsExactly("conn-1");
      assertThat(dto.zoneInstances()).hasSize(1);
      assertThat(dto.componentInstances()).containsExactly("ci-1");
    }
  }

  // ─── ScopeGraphQL ────────────────────────────────────────────────────────

  @Nested
  class ScopeGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ScopeGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      Scope s = mock(Scope.class);
      when(s.getName()).thenReturn("scope-prod");
      when(s.getServiceNowId()).thenReturn("SN-001");
      when(s.getAssigneeGroup()).thenReturn("team-x");
      when(s.getRepositoryVersion()).thenReturn("v1");
      when(s.getNanoId()).thenReturn("nano-1");
      when(s.isDeleted()).thenReturn(false);
      when(s.getOwners()).thenReturn(Set.of("owner1"));
      when(s.getRolesJsonRepresentation()).thenReturn(Set.of("role1"));
      when(s.getTargets()).thenReturn(null);
      when(s.getLandingZones()).thenReturn(null);
      when(s.getComponents()).thenReturn(null);
      when(s.getComponentInstances()).thenReturn(null);
      when(s.getZoneInstances()).thenReturn(null);
      when(s.getSubnetInstances()).thenReturn(null);
      ScopeGraphQL dto = ScopeGraphQL.from(s);
      assertThat(dto.name()).isEqualTo("scope-prod");
      assertThat(dto.serviceNowId()).isEqualTo("SN-001");
      assertThat(dto.assigneeGroup()).isEqualTo("team-x");
      assertThat(dto.repositoryVersion()).isEqualTo("v1");
      assertThat(dto.nanoId()).isEqualTo("nano-1");
      assertThat(dto.isDeleted()).isFalse();
      assertThat(dto.owners()).containsExactly("owner1");
      assertThat(dto.roles()).containsExactly("role1");
      assertThat(dto.targets()).isEmpty();
      assertThat(dto.landingZones()).isEmpty();
      assertThat(dto.components()).isEmpty();
      assertThat(dto.componentInstances()).isEmpty();
      assertThat(dto.zoneInstances()).isEmpty();
      assertThat(dto.subnetInstances()).isEmpty();
    }

    @Test
    void from_withNonNullCollections() {
      Target target = mock(Target.class);
      when(target.getName()).thenReturn("t-1");
      LandingZone lz = mock(LandingZone.class);
      when(lz.getName()).thenReturn("lz-1");
      Component comp = mock(Component.class);
      when(comp.getName()).thenReturn("comp-1");
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-1");
      ZoneInstance zi = mock(ZoneInstance.class);
      when(zi.getName()).thenReturn("zi-1");
      when(zi.getLandingZoneName()).thenReturn(null);
      when(zi.getZoneTypeName()).thenReturn(null);
      when(zi.getFirewallGroupName()).thenReturn(null);
      when(zi.isNsgNeeded()).thenReturn(false);
      when(zi.getSubnetInstances()).thenReturn(null);
      when(zi.getComponentInstances()).thenReturn(null);
      SubnetInstance si = mock(SubnetInstance.class);
      when(si.getName()).thenReturn("si-1");
      Scope s = mock(Scope.class);
      when(s.getName()).thenReturn("scope-x");
      when(s.getServiceNowId()).thenReturn(null);
      when(s.getAssigneeGroup()).thenReturn(null);
      when(s.getRepositoryVersion()).thenReturn(null);
      when(s.getNanoId()).thenReturn(null);
      when(s.isDeleted()).thenReturn(false);
      when(s.getOwners()).thenReturn(null);
      when(s.getRolesJsonRepresentation()).thenReturn(null);
      when(s.getTargets()).thenReturn(Set.of(target));
      when(s.getLandingZones()).thenReturn(Set.of(lz));
      when(s.getComponents()).thenReturn(Set.of(comp));
      when(s.getComponentInstances()).thenReturn(Set.of(ci));
      when(s.getZoneInstances()).thenReturn(Set.of(zi));
      when(s.getSubnetInstances()).thenReturn(Set.of(si));
      ScopeGraphQL dto = ScopeGraphQL.from(s);
      assertThat(dto.targets()).containsExactly("t-1");
      assertThat(dto.landingZones()).containsExactly("lz-1");
      assertThat(dto.components()).containsExactly("comp-1");
      assertThat(dto.componentInstances()).containsExactly("ci-1");
      assertThat(dto.zoneInstances()).hasSize(1);
      assertThat(dto.subnetInstances()).containsExactly("si-1");
    }
  }

  // ─── SubnetInstanceGraphQL ──────────────────────────────────────────────

  @Nested
  class SubnetInstanceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(SubnetInstanceGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      SubnetInstance subnetInstance = mock(SubnetInstance.class);
      when(subnetInstance.getName()).thenReturn("subnet-a");
      Scope scope = mock(Scope.class);
      when(scope.getName()).thenReturn("scope-prod");
      when(subnetInstance.getScope()).thenReturn(scope);
      when(subnetInstance.getLandingZoneName()).thenReturn("lz-ams");
      when(subnetInstance.getVlan()).thenReturn("123");
      when(subnetInstance.isShared()).thenReturn(true);
      when(subnetInstance.isGroup()).thenReturn(false);
      when(subnetInstance.isNetworkPrivate()).thenReturn(true);
      when(subnetInstance.getCidr()).thenReturn("10.0.0.0/24");
      when(subnetInstance.getSubnetTypeName()).thenReturn("Private");
      when(subnetInstance.getZoneInstanceName()).thenReturn("zi-main");
      when(subnetInstance.getParentName()).thenReturn("parent-a");
      when(subnetInstance.getDeprecatedStatus()).thenReturn(DeprecatedStatus.WARNING);
      when(subnetInstance.getDeprecatedMessage()).thenReturn("deprecated soon");

      SubnetInstance child = mock(SubnetInstance.class);
      when(child.getName()).thenReturn("child-a");
      when(subnetInstance.getChildren()).thenReturn(Set.of(child));

      ComponentInstance componentInstance = mock(ComponentInstance.class);
      when(componentInstance.getName()).thenReturn("ci-app");
      when(subnetInstance.getComponentInstances()).thenReturn(Set.of(componentInstance));

      SubnetInstanceGraphQL dto = SubnetInstanceGraphQL.from(subnetInstance);

      assertThat(dto.name()).isEqualTo("subnet-a");
      assertThat(dto.scope()).isNotNull();
      assertThat(dto.scope().name()).isEqualTo("scope-prod");
      assertThat(dto.landingZoneName()).isEqualTo("lz-ams");
      assertThat(dto.vlan()).isEqualTo("123");
      assertThat(dto.isShared()).isTrue();
      assertThat(dto.isGroup()).isFalse();
      assertThat(dto.isNetworkPrivate()).isTrue();
      assertThat(dto.cidr()).isEqualTo("10.0.0.0/24");
      assertThat(dto.subnetTypeName()).isEqualTo("Private");
      assertThat(dto.zoneInstanceName()).isEqualTo("zi-main");
      assertThat(dto.parentName()).isEqualTo("parent-a");
      assertThat(dto.deprecatedStatus()).isEqualTo("WARNING");
      assertThat(dto.deprecatedMessage()).isEqualTo("deprecated soon");
      assertThat(dto.children()).containsExactly("child-a");
      assertThat(dto.componentInstances()).containsExactly("ci-app");
    }

    @Test
    void from_nullCollections() {
      SubnetInstance subnetInstance = mock(SubnetInstance.class);
      when(subnetInstance.getName()).thenReturn("subnet-x");
      when(subnetInstance.getScope()).thenReturn(null);
      when(subnetInstance.getLandingZoneName()).thenReturn(null);
      when(subnetInstance.getVlan()).thenReturn(null);
      when(subnetInstance.isShared()).thenReturn(false);
      when(subnetInstance.isGroup()).thenReturn(false);
      when(subnetInstance.isNetworkPrivate()).thenReturn(false);
      when(subnetInstance.getCidr()).thenReturn(null);
      when(subnetInstance.getSubnetTypeName()).thenReturn(null);
      when(subnetInstance.getZoneInstanceName()).thenReturn(null);
      when(subnetInstance.getParentName()).thenReturn(null);
      when(subnetInstance.getDeprecatedStatus()).thenReturn(null);
      when(subnetInstance.getDeprecatedMessage()).thenReturn(null);
      when(subnetInstance.getChildren()).thenReturn(null);
      when(subnetInstance.getComponentInstances()).thenReturn(null);

      SubnetInstanceGraphQL dto = SubnetInstanceGraphQL.from(subnetInstance);

      assertThat(dto.scope()).isNull();
      assertThat(dto.deprecatedStatus()).isNull();
      assertThat(dto.children()).isEmpty();
      assertThat(dto.componentInstances()).isEmpty();
    }
  }

  // ─── TargetGraphQL ───────────────────────────────────────────────────────

  @Nested
  class TargetGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(TargetGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      Target t = mock(Target.class);
      when(t.getName()).thenReturn("target-ams");
      when(t.getProviderName()).thenReturn("Azure");
      when(t.getTargetTypeName()).thenReturn("Cloud");
      when(t.getMacroPhase()).thenReturn(null);
      when(t.getContinentName()).thenReturn("EMEA");
      when(t.getPopPrimaryName()).thenReturn("pop-1");
      when(t.getPopSecondaryName()).thenReturn("pop-2");
      when(t.getCidrs()).thenReturn(List.of("10.0.0.0/8"));
      when(t.isCloud()).thenReturn(true);
      when(t.isAmadeus()).thenReturn(false);
      when(t.isAudManaged()).thenReturn(true);
      when(t.isEnableOfficeConnectivity()).thenReturn(false);
      when(t.isEnableJumpServerAccess()).thenReturn(true);
      when(t.isEnableVnetToVnet()).thenReturn(false);
      when(t.getLandingZones()).thenReturn(null);
      when(t.getFirewallGroups()).thenReturn(null);
      TargetGraphQL dto = TargetGraphQL.from(t);
      assertThat(dto.name()).isEqualTo("target-ams");
      assertThat(dto.providerName()).isEqualTo("Azure");
      assertThat(dto.targetTypeName()).isEqualTo("Cloud");
      assertThat(dto.macroPhase()).isNull();
      assertThat(dto.continentName()).isEqualTo("EMEA");
      assertThat(dto.popPrimaryName()).isEqualTo("pop-1");
      assertThat(dto.popSecondaryName()).isEqualTo("pop-2");
      assertThat(dto.cidrs()).containsExactly("10.0.0.0/8");
      assertThat(dto.isCloud()).isTrue();
      assertThat(dto.isAmadeus()).isFalse();
      assertThat(dto.isAudManaged()).isTrue();
      assertThat(dto.isEnableOfficeConnectivity()).isFalse();
      assertThat(dto.isEnableJumpServerAccess()).isTrue();
      assertThat(dto.isEnableVnetToVnet()).isFalse();
      assertThat(dto.landingZones()).isEmpty();
      assertThat(dto.firewallGroups()).isEmpty();
    }

    @Test
    void from_withNonNullLandingZonesAndFirewallGroups() {
      LandingZone lz = mock(LandingZone.class);
      when(lz.getName()).thenReturn("lz-1");
      FirewallGroup fg = mock(FirewallGroup.class);
      when(fg.getName()).thenReturn("fg-1");
      Target t = mock(Target.class);
      when(t.getName()).thenReturn("target-x");
      when(t.getProviderName()).thenReturn(null);
      when(t.getTargetTypeName()).thenReturn(null);
      when(t.getMacroPhase()).thenReturn(null);
      when(t.getContinentName()).thenReturn(null);
      when(t.getPopPrimaryName()).thenReturn(null);
      when(t.getPopSecondaryName()).thenReturn(null);
      when(t.getCidrs()).thenReturn(null);
      when(t.isCloud()).thenReturn(false);
      when(t.isAmadeus()).thenReturn(false);
      when(t.isAudManaged()).thenReturn(false);
      when(t.isEnableOfficeConnectivity()).thenReturn(false);
      when(t.isEnableJumpServerAccess()).thenReturn(false);
      when(t.isEnableVnetToVnet()).thenReturn(false);
      when(t.getLandingZones()).thenReturn(Set.of(lz));
      when(t.getFirewallGroups()).thenReturn(Set.of(fg));
      TargetGraphQL dto = TargetGraphQL.from(t);
      assertThat(dto.landingZones()).containsExactly("lz-1");
      assertThat(dto.firewallGroups()).containsExactly("fg-1");
    }
  }

  // ─── LandingZoneGraphQL ──────────────────────────────────────────────────

  @Nested
  class LandingZoneGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(LandingZoneGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      LandingZone lz = mockLandingZone();
      ZoneInstance zi = mock(ZoneInstance.class);
      when(zi.getName()).thenReturn("zi-main");
      when(zi.getLandingZoneName()).thenReturn("lz-ams");
      when(zi.getZoneTypeName()).thenReturn("dmz");
      when(zi.getFirewallGroupName()).thenReturn(null);
      when(zi.isNsgNeeded()).thenReturn(false);
      when(zi.getSubnetInstances()).thenReturn(null);
      when(zi.getComponentInstances()).thenReturn(null);
      when(lz.getZoneInstances()).thenReturn(Set.of(zi));
      SubnetInstance si = mock(SubnetInstance.class);
      when(si.getName()).thenReturn("subnet-a");
      when(lz.getSubnetInstances()).thenReturn(Set.of(si));
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-app");
      when(lz.getComponentInstances()).thenReturn(Set.of(ci));
      when(lz.getCidrs()).thenReturn(List.of("10.1.0.0/16"));
      when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(Set.of("Cloud"));
      LandingZoneGraphQL dto = LandingZoneGraphQL.from(lz);
      assertThat(dto.name()).isEqualTo("lz-ams");
      assertThat(dto.regionName()).isEqualTo("region-weu");
      assertThat(dto.landingZoneTypeName()).isEqualTo("lzt-azure");
      assertThat(dto.macroPhase()).isNull();
      assertThat(dto.cidrs()).containsExactly("10.1.0.0/16");
      assertThat(dto.functionalTypes()).containsExactly("Cloud");
      assertThat(dto.zoneInstances()).hasSize(1);
      assertThat(dto.subnetInstances()).containsExactly("subnet-a");
      assertThat(dto.componentInstances()).containsExactly("ci-app");
    }

    @Test
    void from_nullCollections() {
      LandingZone lz = mockLandingZone();
      when(lz.getZoneInstances()).thenReturn(null);
      when(lz.getSubnetInstances()).thenReturn(null);
      when(lz.getComponentInstances()).thenReturn(null);
      LandingZoneGraphQL dto = LandingZoneGraphQL.from(lz);
      assertThat(dto.zoneInstances()).isEmpty();
      assertThat(dto.subnetInstances()).isEmpty();
      assertThat(dto.componentInstances()).isEmpty();
    }
  }

  // ─── ComponentGraphQL ────────────────────────────────────────────────────

  @Nested
  class ComponentGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ComponentGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      Component c = mock(Component.class);
      when(c.getName()).thenReturn("comp-api");
      when(c.getScope()).thenReturn(null);
      when(c.getDeprecatedMessage()).thenReturn("msg");
      when(c.isPublic()).thenReturn(true);
      when(c.isPublicInheritance()).thenReturn(false);
      when(c.isPublicDependency()).thenReturn(false);
      when(c.isPrivate()).thenReturn(false);
      when(c.isHosting()).thenReturn(true);
      when(c.isHosted()).thenReturn(false);
      when(c.isTechnical()).thenReturn(false);
      when(c.isInterface()).thenReturn(false);
      when(c.isContainer()).thenReturn(false);
      when(c.getSecurityZones()).thenReturn(Set.of("sz-internet"));
      when(c.getServiceNowIds()).thenReturn(Set.of("SN-001"));
      when(c.getParentsNames()).thenReturn(Set.of("parent-comp"));
      Component child = mock(Component.class);
      when(child.getName()).thenReturn("child-comp");
      when(c.getChildren()).thenReturn(Set.of(child));
      when(c.getZoneTypes()).thenReturn(null);
      when(c.getServices()).thenReturn(null);
      ComponentGraphQL dto = ComponentGraphQL.from(c);
      assertThat(dto.name()).isEqualTo("comp-api");
      assertThat(dto.deprecatedMessage()).isEqualTo("msg");
      assertThat(dto.isPublic()).isTrue();
      assertThat(dto.securityZones()).containsExactly("sz-internet");
      assertThat(dto.serviceNowIds()).containsExactly("SN-001");
      assertThat(dto.parents()).containsExactly("parent-comp");
      assertThat(dto.children()).containsExactly("child-comp");
      assertThat(dto.zoneTypes()).isEmpty();
      assertThat(dto.services()).isEmpty();
    }

    @Test
    void from_nullDeprecatedStatus() {
      Component c = mock(Component.class);
      when(c.getName()).thenReturn("comp-api");
      when(c.getScope()).thenReturn(null);
      when(c.getDeprecatedStatus()).thenReturn(null);
      when(c.getDeprecatedMessage()).thenReturn(null);
      when(c.isPublic()).thenReturn(false);
      when(c.isPublicInheritance()).thenReturn(false);
      when(c.isPublicDependency()).thenReturn(false);
      when(c.isPrivate()).thenReturn(false);
      when(c.isHosting()).thenReturn(false);
      when(c.isHosted()).thenReturn(false);
      when(c.isTechnical()).thenReturn(false);
      when(c.isInterface()).thenReturn(false);
      when(c.isContainer()).thenReturn(false);
      when(c.getSecurityZones()).thenReturn(null);
      when(c.getServiceNowIds()).thenReturn(null);
      when(c.getParentsNames()).thenReturn(null);
      when(c.getChildren()).thenReturn(null);
      when(c.getZoneTypes()).thenReturn(null);
      when(c.getServices()).thenReturn(null);
      ComponentGraphQL dto = ComponentGraphQL.from(c);
      assertThat(dto.deprecatedStatus()).isNull();
      assertThat(dto.children()).isEmpty();
    }

    @Test
    void from_withNonNullZoneTypesAndServices() {
      ZoneType zt = mock(ZoneType.class);
      when(zt.getName()).thenReturn("dmz");
      when(zt.isTechnical()).thenReturn(false);
      when(zt.isReal()).thenReturn(true);
      when(zt.isPaas()).thenReturn(false);
      when(zt.getComponent()).thenReturn(null);
      when(zt.getConnections()).thenReturn(null);
      when(zt.getZoneInstances()).thenReturn(null);
      when(zt.getComponentInstances()).thenReturn(null);
      Service svc = mockService("svc-1");
      Component c = mock(Component.class);
      when(c.getName()).thenReturn("comp-x");
      when(c.getScope()).thenReturn(null);
      when(c.getDeprecatedStatus()).thenReturn(null);
      when(c.getDeprecatedMessage()).thenReturn(null);
      when(c.isPublic()).thenReturn(false);
      when(c.isPublicInheritance()).thenReturn(false);
      when(c.isPublicDependency()).thenReturn(false);
      when(c.isPrivate()).thenReturn(false);
      when(c.isHosting()).thenReturn(false);
      when(c.isHosted()).thenReturn(false);
      when(c.isTechnical()).thenReturn(false);
      when(c.isInterface()).thenReturn(false);
      when(c.isContainer()).thenReturn(false);
      when(c.getSecurityZones()).thenReturn(null);
      when(c.getServiceNowIds()).thenReturn(null);
      when(c.getParentsNames()).thenReturn(null);
      when(c.getChildren()).thenReturn(null);
      when(c.getZoneTypes()).thenReturn(Set.of(zt));
      when(c.getServices()).thenReturn(Set.of(svc));
      ComponentGraphQL dto = ComponentGraphQL.from(c);
      assertThat(dto.zoneTypes()).hasSize(1);
      assertThat(dto.services()).hasSize(1);
    }
  }

  // ─── ComponentInstanceGraphQL ─────────────────────────────────────────────

  @Nested
  class ComponentInstanceGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(ComponentInstanceGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-app");
      when(ci.getComponent()).thenReturn(null);
      when(ci.getZoneType()).thenReturn(null);
      when(ci.getApplicationPhaseName()).thenReturn("phase-prod");
      when(ci.getNamespace()).thenReturn(null);
      when(ci.getServiceTagName()).thenReturn("tag-svc");
      when(ci.getDeployComponentInstanceName()).thenReturn("deploy-ci");
      when(ci.getInSubnetInstanceName()).thenReturn("subnet-in");
      when(ci.getOutSubnetInstanceName()).thenReturn("subnet-out");
      when(ci.getInZoneInstance()).thenReturn(null);
      when(ci.getOutZoneInstance()).thenReturn(null);
      when(ci.getEndpointType()).thenReturn(V1ComponentInstanceSpec.EndpointTypeEnum.CID_RLIST);
      when(ci.getDeprecatedMessage()).thenReturn("deprecated-msg");
      when(ci.getHostingPlatformType())
          .thenReturn(V1ComponentInstanceSpec.HostingComponentTypeEnum.AKS);
      when(ci.getK8sIdentifier()).thenReturn("k8s-id");
      when(ci.isAutomaticallyInstantiated()).thenReturn(true);
      when(ci.getLandingZone()).thenReturn(null);
      when(ci.getHostingPlatformName()).thenReturn("iaas-ci");
      when(ci.getAutomaticTemplateInstances()).thenReturn(null);
      ComponentInstanceGraphQL dto = ComponentInstanceGraphQL.from(ci);
      assertThat(dto.name()).isEqualTo("ci-app");
      assertThat(dto.applicationPhase()).isEqualTo("phase-prod");
      assertThat(dto.serviceTag()).isEqualTo("tag-svc");
      assertThat(dto.deployComponentInstance()).isEqualTo("deploy-ci");
      assertThat(dto.inSubnetInstance()).isEqualTo("subnet-in");
      assertThat(dto.outSubnetInstance()).isEqualTo("subnet-out");
      assertThat(dto.endpointType())
          .isEqualTo(V1ComponentInstanceSpec.EndpointTypeEnum.CID_RLIST.name());
      assertThat(dto.deprecatedMessage()).isEqualTo("deprecated-msg");
      assertThat(dto.hostingPlatformType())
          .isEqualTo(V1ComponentInstanceSpec.HostingComponentTypeEnum.AKS.name());
      assertThat(dto.k8sIdentifier()).isEqualTo("k8s-id");
      assertThat(dto.isAutomaticallyInstantiated()).isTrue();
      assertThat(dto.iaasComponentInstanceName()).isEqualTo("iaas-ci");
      assertThat(dto.automaticTemplateInstances()).isEmpty();
    }

    @Test
    void from_nullEnums() {
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-min");
      when(ci.getComponent()).thenReturn(null);
      when(ci.getZoneType()).thenReturn(null);
      when(ci.getApplicationPhaseName()).thenReturn(null);
      when(ci.getNamespace()).thenReturn(null);
      when(ci.getServiceTagName()).thenReturn(null);
      when(ci.getDeployComponentInstanceName()).thenReturn(null);
      when(ci.getInSubnetInstanceName()).thenReturn(null);
      when(ci.getOutSubnetInstanceName()).thenReturn(null);
      when(ci.getInZoneInstance()).thenReturn(null);
      when(ci.getOutZoneInstance()).thenReturn(null);
      when(ci.getEndpointType()).thenReturn(null);
      when(ci.getDeprecatedStatus()).thenReturn(null);
      when(ci.getDeprecatedMessage()).thenReturn(null);
      when(ci.getHostingPlatformType()).thenReturn(null);
      when(ci.getK8sIdentifier()).thenReturn(null);
      when(ci.isAutomaticallyInstantiated()).thenReturn(false);
      when(ci.getLandingZone()).thenReturn(null);
      when(ci.getHostingPlatformName()).thenReturn(null);
      when(ci.getAutomaticTemplateInstances()).thenReturn(null);
      ComponentInstanceGraphQL dto = ComponentInstanceGraphQL.from(ci);
      assertThat(dto.endpointType()).isNull();
      assertThat(dto.deprecatedStatus()).isNull();
      assertThat(dto.hostingPlatformType()).isNull();
    }

    @Test
    void from_withInOutZoneInstances() {
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-zi");
      when(ci.getComponent()).thenReturn(null);
      when(ci.getZoneType()).thenReturn(null);
      when(ci.getApplicationPhaseName()).thenReturn(null);
      when(ci.getNamespace()).thenReturn(null);
      when(ci.getServiceTagName()).thenReturn(null);
      when(ci.getDeployComponentInstanceName()).thenReturn(null);
      when(ci.getInSubnetInstanceName()).thenReturn(null);
      when(ci.getOutSubnetInstanceName()).thenReturn(null);
      when(ci.getEndpointType()).thenReturn(null);
      when(ci.getDeprecatedStatus()).thenReturn(null);
      when(ci.getDeprecatedMessage()).thenReturn(null);
      when(ci.getHostingPlatformType()).thenReturn(null);
      when(ci.getK8sIdentifier()).thenReturn(null);
      when(ci.isAutomaticallyInstantiated()).thenReturn(false);
      when(ci.getLandingZone()).thenReturn(null);
      when(ci.getHostingPlatformName()).thenReturn(null);
      when(ci.getAutomaticTemplateInstances()).thenReturn(null);
      ZoneInstance inZi = mock(ZoneInstance.class);
      when(inZi.getName()).thenReturn("zi-in");
      when(inZi.getLandingZoneName()).thenReturn(null);
      when(inZi.getZoneTypeName()).thenReturn(null);
      when(inZi.getFirewallGroupName()).thenReturn(null);
      when(inZi.isNsgNeeded()).thenReturn(false);
      when(inZi.getSubnetInstances()).thenReturn(null);
      when(inZi.getComponentInstances()).thenReturn(null);
      when(ci.getInZoneInstance()).thenReturn(inZi);
      ZoneInstance outZi = mock(ZoneInstance.class);
      when(outZi.getName()).thenReturn("zi-out");
      when(outZi.getLandingZoneName()).thenReturn(null);
      when(outZi.getZoneTypeName()).thenReturn(null);
      when(outZi.getFirewallGroupName()).thenReturn(null);
      when(outZi.isNsgNeeded()).thenReturn(false);
      when(outZi.getSubnetInstances()).thenReturn(null);
      when(outZi.getComponentInstances()).thenReturn(null);
      when(ci.getOutZoneInstance()).thenReturn(outZi);
      ComponentInstanceGraphQL dto = ComponentInstanceGraphQL.from(ci);
      assertThat(dto.inZoneInstance()).isNotNull();
      assertThat(dto.inZoneInstance().name()).isEqualTo("zi-in");
      assertThat(dto.outZoneInstance()).isNotNull();
      assertThat(dto.outZoneInstance().name()).isEqualTo("zi-out");
    }

    @Test
    void from_withAutomaticTemplateInstances() {
      ComponentInstance ci = mock(ComponentInstance.class);
      when(ci.getName()).thenReturn("ci-ati");
      when(ci.getComponent()).thenReturn(null);
      when(ci.getZoneType()).thenReturn(null);
      when(ci.getApplicationPhaseName()).thenReturn(null);
      when(ci.getNamespace()).thenReturn(null);
      when(ci.getServiceTagName()).thenReturn(null);
      when(ci.getDeployComponentInstanceName()).thenReturn(null);
      when(ci.getInSubnetInstanceName()).thenReturn(null);
      when(ci.getOutSubnetInstanceName()).thenReturn(null);
      when(ci.getInZoneInstance()).thenReturn(null);
      when(ci.getOutZoneInstance()).thenReturn(null);
      when(ci.getEndpointType()).thenReturn(null);
      when(ci.getDeprecatedStatus()).thenReturn(null);
      when(ci.getDeprecatedMessage()).thenReturn(null);
      when(ci.getHostingPlatformType()).thenReturn(null);
      when(ci.getK8sIdentifier()).thenReturn(null);
      when(ci.isAutomaticallyInstantiated()).thenReturn(false);
      when(ci.getLandingZone()).thenReturn(null);
      when(ci.getHostingPlatformName()).thenReturn(null);
      AutomaticTemplateInstance ati = mock(AutomaticTemplateInstance.class);
      when(ati.getName()).thenReturn("ati-1");
      when(ati.getComponent()).thenReturn(null);
      when(ati.getLandingZone()).thenReturn(null);
      when(ati.getComponentInstances()).thenReturn(null);
      when(ati.getAssociatedFunctionalTypes()).thenReturn(null);
      when(ati.getExcludedFunctionalTypes()).thenReturn(null);
      when(ci.getAutomaticTemplateInstances()).thenReturn(Set.of(ati));
      ComponentInstanceGraphQL dto = ComponentInstanceGraphQL.from(ci);
      assertThat(dto.automaticTemplateInstances()).hasSize(1);
      assertThat(dto.automaticTemplateInstances().iterator().next()).isEqualTo("ati-1");
    }
  }

  // ─── Shared helpers ───────────────────────────────────────────────────────

  private Service mockService(String name) {
    Service svc = mock(Service.class);
    when(svc.getName()).thenReturn(name);
    when(svc.getComponentName()).thenReturn(null);
    when(svc.getApplicationIdName()).thenReturn(null);
    when(svc.getServiceNowId()).thenReturn(null);
    when(svc.getProtocol()).thenReturn(null);
    when(svc.getPorts()).thenReturn(null);
    when(svc.getApprovedProtocol()).thenReturn(null);
    when(svc.getDeprecatedStatus()).thenReturn(null);
    when(svc.getDeprecatedMessage()).thenReturn(null);
    when(svc.isPrivate()).thenReturn(false);
    when(svc.isPci()).thenReturn(false);
    when(svc.isPii()).thenReturn(false);
    when(svc.isEncrypted()).thenReturn(false);
    when(svc.isDbSynchro()).thenReturn(false);
    return svc;
  }

  private ZoneType mockZoneType() {
    ZoneType zt = mock(ZoneType.class);
    when(zt.getName()).thenReturn("dmz");
    when(zt.isTechnical()).thenReturn(false);
    when(zt.isReal()).thenReturn(true);
    when(zt.isPaas()).thenReturn(false);
    return zt;
  }

  private LandingZone mockLandingZone() {
    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn("lz-ams");
    when(lz.getTarget()).thenReturn(null);
    when(lz.getRegionName()).thenReturn("region-weu");
    when(lz.getLandingZoneTypeName()).thenReturn("lzt-azure");
    when(lz.getMacroPhase()).thenReturn(null);
    when(lz.getCidrs()).thenReturn(null);
    when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(null);
    when(lz.getZoneInstances()).thenReturn(null);
    when(lz.getSubnetInstances()).thenReturn(null);
    when(lz.getComponentInstances()).thenReturn(null);
    return lz;
  }

  @Nested
  class SearchResultGraphQLTests {

    @Test
    void constructor_withAllFields_mapsCorrectly() {
      SearchResultGraphQL result =
          new SearchResultGraphQL("Component", "my-comp", "my-comp", "scope-a", 0);

      assertThat(result.type()).isEqualTo("Component");
      assertThat(result.name()).isEqualTo("my-comp");
      assertThat(result.id()).isEqualTo("my-comp");
      assertThat(result.scope()).isEqualTo("scope-a");
      assertThat(result.relevance()).isZero();
    }

    @Test
    void constructor_withNullScopeAndId_mapsNulls() {
      SearchResultGraphQL result = new SearchResultGraphQL("Service", "svc-foo", null, null, 5);

      assertThat(result.type()).isEqualTo("Service");
      assertThat(result.name()).isEqualTo("svc-foo");
      assertThat(result.id()).isNull();
      assertThat(result.scope()).isNull();
      assertThat(result.relevance()).isEqualTo(5);
    }
  }

  @Nested
  class PaginationGraphQLTests {

    @Test
    void constructor_storesAllFields() {
      PaginationGraphQL pagination = new PaginationGraphQL(42, 3, 10);

      assertThat(pagination.count()).isEqualTo(42);
      assertThat(pagination.pageNum()).isEqualTo(3);
      assertThat(pagination.pageSize()).isEqualTo(10);
    }

    @Test
    void constructor_withZeroValues() {
      PaginationGraphQL pagination = new PaginationGraphQL(0, 1, 0);

      assertThat(pagination.count()).isZero();
      assertThat(pagination.pageNum()).isOne();
      assertThat(pagination.pageSize()).isZero();
    }
  }

  @Nested
  class PageGraphQLTests {

    @Test
    void constructor_storesItemsAndPagination() {
      PaginationGraphQL pagination = new PaginationGraphQL(3, 1, 10);
      List<String> items = List.of("a", "b", "c");

      PageGraphQL<String> page = new PageGraphQL<>(items, pagination);

      assertThat(page.items()).containsExactly("a", "b", "c");
      assertThat(page.pagination()).isSameAs(pagination);
      assertThat(page.pagination().count()).isEqualTo(3);
    }

    @Test
    void constructor_withEmptyItems() {
      PaginationGraphQL pagination = new PaginationGraphQL(0, 1, 10);

      PageGraphQL<String> page = new PageGraphQL<>(List.of(), pagination);

      assertThat(page.items()).isEmpty();
      assertThat(page.pagination().count()).isZero();
    }

    @Test
    void constructor_withNullPagination() {
      List<String> items = List.of("x");

      PageGraphQL<String> page = new PageGraphQL<>(items, null);

      assertThat(page.items()).containsExactly("x");
      assertThat(page.pagination()).isNull();
    }
  }

  // ─── PAASComponentGraphQL ────────────────────────────────────────────────

  @Nested
  class PAASComponentGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(PAASComponentGraphQL.from(null)).isNull();
    }

    @Test
    void fromFullComponent_mapsAllFields() {
      com.amadeus.aud.libraries.yaml.models.PAASComponent component =
          new com.amadeus.aud.libraries.yaml.models.PAASComponent();
      component.setName("test-component");
      component.setNamespace("default");
      component.setIdentifier(Map.of("app", "test"));
      component.setInNetwork("network-in");
      component.setOutNetwork("network-out");
      component.setLabels(Map.of("label1", "value1"));

      PAASComponentGraphQL dto = PAASComponentGraphQL.from(component);

      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("test-component");
      assertThat(dto.namespace()).isEqualTo("default");
      assertThat(dto.identifier()).containsEntry("app", "test");
      assertThat(dto.inNetwork()).isEqualTo("network-in");
      assertThat(dto.outNetwork()).isEqualTo("network-out");
      assertThat(dto.labels()).containsEntry("label1", "value1");
    }
  }

  // ─── NetworkSetGraphQL ───────────────────────────────────────────────────

  @Nested
  class NetworkSetGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(NetworkSetGraphQL.from(null)).isNull();
    }

    @Test
    void fromFullNetworkSet_mapsAllFields() {
      com.amadeus.aud.libraries.yaml.models.Endpoint endpoint1 =
          new com.amadeus.aud.libraries.yaml.models.Endpoint();
      endpoint1.setType("cidr");
      endpoint1.setValue("10.0.0.0/24");

      com.amadeus.aud.libraries.yaml.models.Endpoint endpoint2 =
          new com.amadeus.aud.libraries.yaml.models.Endpoint();
      endpoint2.setType("fqdn");
      endpoint2.setValue("example.com");

      com.amadeus.aud.libraries.yaml.models.NetworkSet networkSet =
          new com.amadeus.aud.libraries.yaml.models.NetworkSet();
      networkSet.setName("test-network");
      networkSet.setEndpoints(List.of(endpoint1, endpoint2));
      networkSet.setLabels(Map.of("zone", "production"));

      NetworkSetGraphQL dto = NetworkSetGraphQL.from(networkSet);

      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("test-network");
      assertThat(dto.endpoints()).hasSize(2);
      assertThat(dto.endpoints().get(0).type()).isEqualTo("cidr");
      assertThat(dto.endpoints().get(0).value()).isEqualTo("10.0.0.0/24");
      assertThat(dto.endpoints().get(1).type()).isEqualTo("fqdn");
      assertThat(dto.endpoints().get(1).value()).isEqualTo("example.com");
      assertThat(dto.labels()).containsEntry("zone", "production");
    }

    @Test
    void fromNetworkSetWithNullEndpoints_returnsEmptyList() {
      com.amadeus.aud.libraries.yaml.models.NetworkSet networkSet =
          new com.amadeus.aud.libraries.yaml.models.NetworkSet();
      networkSet.setName("test-network");
      networkSet.setEndpoints(null);
      networkSet.setLabels(Map.of("zone", "production"));

      NetworkSetGraphQL dto = NetworkSetGraphQL.from(networkSet);

      assertThat(dto).isNotNull();
      assertThat(dto.name()).isEqualTo("test-network");
      assertThat(dto.endpoints()).isEmpty();
      assertThat(dto.labels()).containsEntry("zone", "production");
    }
  }

  @Nested
  class BlackListedURLGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(BlackListedURLGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      com.amadeus.aud.libraries.models.core.db.BlackListedURL url =
          mock(com.amadeus.aud.libraries.models.core.db.BlackListedURL.class);
      when(url.id()).thenReturn("block-example-com");
      when(url.getName()).thenReturn("block-example-com");
      when(url.getScope()).thenReturn(null);
      when(url.getFqdn()).thenReturn("example.com");
      when(url.getTransportLayer())
          .thenReturn(
              List.of(
                  V1BlackListedURLSpec.TransportLayerEnum.TCP,
                  V1BlackListedURLSpec.TransportLayerEnum.UDP));
      when(url.getPorts()).thenReturn(List.of("80", "443"));

      BlackListedURLGraphQL dto = BlackListedURLGraphQL.from(url);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo("block-example-com");
      assertThat(dto.name()).isEqualTo("block-example-com");
      assertThat(dto.fqdn()).isEqualTo("example.com");
      assertThat(dto.transportLayer()).containsExactly("Tcp", "Udp");
      assertThat(dto.ports()).containsExactly("80", "443");
      assertThat(dto.scope()).isNull();
    }

    @Test
    void from_nullTransportLayer_returnsEmptyList() {
      com.amadeus.aud.libraries.models.core.db.BlackListedURL url =
          mock(com.amadeus.aud.libraries.models.core.db.BlackListedURL.class);
      when(url.id()).thenReturn("block-x");
      when(url.getName()).thenReturn("block-x");
      when(url.getScope()).thenReturn(null);
      when(url.getFqdn()).thenReturn("x.com");
      when(url.getTransportLayer()).thenReturn(null);
      when(url.getPorts()).thenReturn(List.of("443"));

      assertThat(BlackListedURLGraphQL.from(url).transportLayer()).isEmpty();
    }

    @Test
    void from_nullPorts_returnsEmptyList() {
      com.amadeus.aud.libraries.models.core.db.BlackListedURL url =
          mock(com.amadeus.aud.libraries.models.core.db.BlackListedURL.class);
      when(url.id()).thenReturn("block-y");
      when(url.getName()).thenReturn("block-y");
      when(url.getScope()).thenReturn(null);
      when(url.getFqdn()).thenReturn("y.com");
      when(url.getTransportLayer())
          .thenReturn(List.of(V1BlackListedURLSpec.TransportLayerEnum.TCP));
      when(url.getPorts()).thenReturn(null);

      assertThat(BlackListedURLGraphQL.from(url).ports()).isEmpty();
    }
  }

  @Nested
  class BlackListedCIDRGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(BlackListedCIDRGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      com.amadeus.aud.libraries.models.core.db.BlackListedCIDR cidr =
          mock(com.amadeus.aud.libraries.models.core.db.BlackListedCIDR.class);
      when(cidr.id()).thenReturn("block-subnet");
      when(cidr.getName()).thenReturn("block-subnet");
      when(cidr.getScope()).thenReturn(null);
      when(cidr.getCidr()).thenReturn("10.0.0.0/8");
      when(cidr.getTransportLayer())
          .thenReturn(List.of(V1BlackListedCIDRSpec.TransportLayerEnum.TCP));
      when(cidr.getPorts()).thenReturn(List.of("22", "443"));

      BlackListedCIDRGraphQL dto = BlackListedCIDRGraphQL.from(cidr);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo("block-subnet");
      assertThat(dto.name()).isEqualTo("block-subnet");
      assertThat(dto.cidr()).isEqualTo("10.0.0.0/8");
      assertThat(dto.transportLayer()).containsExactly("Tcp");
      assertThat(dto.ports()).containsExactly("22", "443");
      assertThat(dto.scope()).isNull();
    }

    @Test
    void from_nullTransportLayer_returnsEmptyList() {
      com.amadeus.aud.libraries.models.core.db.BlackListedCIDR cidr =
          mock(com.amadeus.aud.libraries.models.core.db.BlackListedCIDR.class);
      when(cidr.id()).thenReturn("block-a");
      when(cidr.getName()).thenReturn("block-a");
      when(cidr.getScope()).thenReturn(null);
      when(cidr.getCidr()).thenReturn("192.168.0.0/16");
      when(cidr.getTransportLayer()).thenReturn(null);
      when(cidr.getPorts()).thenReturn(List.of("80"));

      assertThat(BlackListedCIDRGraphQL.from(cidr).transportLayer()).isEmpty();
    }

    @Test
    void from_nullPorts_returnsEmptyList() {
      com.amadeus.aud.libraries.models.core.db.BlackListedCIDR cidr =
          mock(com.amadeus.aud.libraries.models.core.db.BlackListedCIDR.class);
      when(cidr.id()).thenReturn("block-b");
      when(cidr.getName()).thenReturn("block-b");
      when(cidr.getScope()).thenReturn(null);
      when(cidr.getCidr()).thenReturn("172.16.0.0/12");
      when(cidr.getTransportLayer())
          .thenReturn(List.of(V1BlackListedCIDRSpec.TransportLayerEnum.UDP));
      when(cidr.getPorts()).thenReturn(null);

      assertThat(BlackListedCIDRGraphQL.from(cidr).ports()).isEmpty();
    }
  }

  // ─── BlacklistGraphQL ────────────────────────────────────────────────

  @Nested
  class BlacklistGraphQLTests {

    @Test
    void fromNull_returnsNull() {
      assertThat(BlacklistGraphQL.from(null)).isNull();
    }

    @Test
    void from_mapsAllFields() {
      Blacklist blacklist = mock(Blacklist.class);
      when(blacklist.id()).thenReturn("block-google");
      when(blacklist.getName()).thenReturn("block-google");
      when(blacklist.getScope()).thenReturn(null);
      when(blacklist.getSourceComponent()).thenReturn("my-app");
      when(blacklist.getDestinationHostFqdns()).thenReturn(List.of("*.google.com"));
      when(blacklist.getDestinationHostCidrs()).thenReturn(null);
      when(blacklist.getSourceZones()).thenReturn(List.of("CSZ", "TNZ"));
      when(blacklist.getSourceMacrophases()).thenReturn(List.of("TST", "PRD"));
      when(blacklist.getSourceTargets()).thenReturn(List.of("prd-we"));
      when(blacklist.getDestinationApplicationProtocols()).thenReturn(List.of("http", "https"));
      when(blacklist.getDestinationTransportProtocols()).thenReturn(List.of("Tcp"));
      when(blacklist.getDestinationPortRanges()).thenReturn(List.of("443", "8080"));

      BlacklistGraphQL dto = BlacklistGraphQL.from(blacklist);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo("block-google");
      assertThat(dto.name()).isEqualTo("block-google");
      assertThat(dto.sourceComponent()).isEqualTo("my-app");
      assertThat(dto.destinationHostFqdns()).contains("*.google.com");
      assertThat(dto.destinationHostCidrs()).isEmpty();
      assertThat(dto.sourceZones()).containsExactlyInAnyOrder("CSZ", "TNZ");
      assertThat(dto.sourceMacrophases()).containsExactlyInAnyOrder("TST", "PRD");
      assertThat(dto.sourceTargets()).containsExactly("prd-we");
      assertThat(dto.destinationApplicationProtocols()).containsExactlyInAnyOrder("http", "https");
      assertThat(dto.destinationTransportProtocols()).containsExactly("Tcp");
      assertThat(dto.destinationPortRanges()).containsExactlyInAnyOrder("443", "8080");
    }

    @Test
    void from_mapsWithCIDR() {
      Blacklist blacklist = mock(Blacklist.class);
      when(blacklist.id()).thenReturn("block-subnet");
      when(blacklist.getName()).thenReturn("block-subnet");
      when(blacklist.getScope()).thenReturn(null);
      when(blacklist.getSourceComponent()).thenReturn("infra-team");
      when(blacklist.getDestinationHostFqdns()).thenReturn(null);
      when(blacklist.getDestinationHostCidrs()).thenReturn(List.of("10.0.0.0/8"));
      when(blacklist.getSourceZones()).thenReturn(List.of("*"));
      when(blacklist.getSourceMacrophases()).thenReturn(List.of("*"));
      when(blacklist.getSourceTargets()).thenReturn(List.of("*"));
      when(blacklist.getDestinationApplicationProtocols()).thenReturn(null);
      when(blacklist.getDestinationTransportProtocols()).thenReturn(List.of("*"));
      when(blacklist.getDestinationPortRanges()).thenReturn(List.of("*"));

      BlacklistGraphQL dto = BlacklistGraphQL.from(blacklist);

      assertThat(dto).isNotNull();
      assertThat(dto.destinationHostCidrs()).contains("10.0.0.0/8");
      assertThat(dto.destinationHostFqdns()).isEmpty();
      assertThat(dto.destinationApplicationProtocols()).isEmpty();
    }

    @Test
    void from_nullCollections_returnsEmptySets() {
      Blacklist blacklist = mock(Blacklist.class);
      when(blacklist.id()).thenReturn("block-minimal");
      when(blacklist.getName()).thenReturn("block-minimal");
      when(blacklist.getScope()).thenReturn(null);
      when(blacklist.getSourceComponent()).thenReturn("app");
      when(blacklist.getDestinationHostFqdns()).thenReturn(List.of("example.com"));
      when(blacklist.getDestinationHostCidrs()).thenReturn(null);
      when(blacklist.getSourceZones()).thenReturn(null);
      when(blacklist.getSourceMacrophases()).thenReturn(null);
      when(blacklist.getSourceTargets()).thenReturn(null);
      when(blacklist.getDestinationApplicationProtocols()).thenReturn(null);
      when(blacklist.getDestinationTransportProtocols()).thenReturn(null);
      when(blacklist.getDestinationPortRanges()).thenReturn(null);

      BlacklistGraphQL dto = BlacklistGraphQL.from(blacklist);

      assertThat(dto).isNotNull();
      assertThat(dto.destinationHostFqdns()).contains("example.com");
      assertThat(dto.destinationHostCidrs()).isEmpty();
      assertThat(dto.sourceZones()).isEmpty();
      assertThat(dto.sourceMacrophases()).isEmpty();
      assertThat(dto.sourceTargets()).isEmpty();
      assertThat(dto.destinationApplicationProtocols()).isEmpty();
      assertThat(dto.destinationTransportProtocols()).isEmpty();
      assertThat(dto.destinationPortRanges()).isEmpty();
    }
  }
}

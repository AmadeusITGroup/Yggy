package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.componentInstance;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.instanceMetadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.named;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallGroupsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.FirewallVendorsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ProvidersRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.SubnetTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.FirewallGroup;
import com.amadeus.aud.libraries.models.core.db.FirewallVendor;
import com.amadeus.aud.libraries.models.core.db.IPAddressInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetType;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneInstance;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroup;
import com.amadeus.aud.libraries.yaml.models.V1FirewallGroupSpec;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstance;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1Region;
import com.amadeus.aud.libraries.yaml.models.V1RegionSpec;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstance;
import com.amadeus.aud.libraries.yaml.models.V1ZoneInstanceSpec;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NetworkRelationshipServicesTest {
  @Nested
  class RegionTests {
    @Test
    void initStopsAtMissingProviderAndContinentRepositories() {
      // Arrange
      RegionService sut = configure(new RegionService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getRegionsRepository()).isNull();
      version.setProvidersRepository(new ProvidersRepository());
      sut.init(version);
      assertThat(version.getRegionsRepository()).isNull();
      version.setContinentsRepository(new ContinentsRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getRegionsRepository()).isNotNull();
    }

    @Test
    void initLinksFoundReferencesAndLeavesMissingOrBlankReferencesUnlinked() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      Provider provider = named(new Provider(), "provider");
      Continent continent = named(new Continent(), "continent");
      ProvidersRepository providersRepository = new ProvidersRepository();
      ContinentsRepository continentsRepository = new ContinentsRepository();
      providersRepository.put(provider.id(), provider);
      continentsRepository.put(continent.id(), continent);
      fixture.version.setProvidersRepository(providersRepository);
      fixture.version.setContinentsRepository(continentsRepository);
      fixture.addData(
          V1Region.class,
          List.of(
              region("linked", CoreDbServiceTestFixture.SCOPE_NAME, "provider", "continent"),
              region(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-provider",
                  "missing-continent"),
              region("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", ""),
              region("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null),
              region("orphan", "missing-scope", "", "")));
      RegionService sut = configure(new RegionService());

      // Act
      sut.init(fixture.version);

      // Assert
      Region linked = fixture.version.getRegionsRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getRegions()).contains(linked);
      assertThat(linked.getProvider()).isSameAs(provider);
      assertThat(provider.getRegions()).contains(linked);
      assertThat(linked.getContinent()).isSameAs(continent);
      assertThat(continent.getRegions()).contains(linked);
      assertThat(fixture.version.getRegionsRepository().get("missing").getProvider()).isNull();
      assertThat(fixture.version.getRegionsRepository().get("empty").getContinent()).isNull();
      assertThat(fixture.version.getRegionsRepository().get("null").getProvider()).isNull();
      assertThat(fixture.version.getRegionsRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class FirewallGroupTests {
    @Test
    void initStopsAtMissingVendorAndTargetRepositories() {
      // Arrange
      FirewallGroupService sut = configure(new FirewallGroupService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getFirewallGroupsRepository()).isNull();
      version.setFirewallVendorsRepository(new FirewallVendorsRepository());
      sut.init(version);
      assertThat(version.getFirewallGroupsRepository()).isNull();
      version.setTargetsRepository(new TargetsRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getFirewallGroupsRepository()).isNotNull();
    }

    @Test
    void initLinksFoundReferencesAndHandlesMissingEmptyAndNullReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      FirewallVendor vendor = named(new FirewallVendor(), "vendor");
      Target target = named(new Target(), "target");
      FirewallVendorsRepository vendorsRepository = new FirewallVendorsRepository();
      TargetsRepository targetsRepository = new TargetsRepository();
      vendorsRepository.put(vendor.id(), vendor);
      targetsRepository.put(target.id(), target);
      fixture.version.setFirewallVendorsRepository(vendorsRepository);
      fixture.version.setTargetsRepository(targetsRepository);
      fixture.addData(
          V1FirewallGroup.class,
          List.of(
              firewallGroup("linked", CoreDbServiceTestFixture.SCOPE_NAME, "vendor", "target"),
              firewallGroup(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-vendor",
                  "missing-target"),
              firewallGroup("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", ""),
              firewallGroup("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null),
              firewallGroup("orphan", "missing-scope", "", "")));
      FirewallGroupService sut = configure(new FirewallGroupService());

      // Act
      sut.init(fixture.version);

      // Assert
      FirewallGroup linked = fixture.version.getFirewallGroupsRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getFirewallGroups()).contains(linked);
      assertThat(linked.getFirewallVendor()).isSameAs(vendor);
      assertThat(vendor.getFirewallGroups()).contains(linked);
      assertThat(linked.getTarget()).isSameAs(target);
      assertThat(target.getFirewallGroups()).contains(linked);
      assertThat(fixture.version.getFirewallGroupsRepository().get("missing").getTarget()).isNull();
      assertThat(fixture.version.getFirewallGroupsRepository().get("empty").getFirewallVendor())
          .isNull();
      assertThat(fixture.version.getFirewallGroupsRepository().get("null").getTarget()).isNull();
      assertThat(fixture.version.getFirewallGroupsRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class ZoneInstanceTests {
    @Test
    void initStopsAtEachMissingPrerequisite() {
      // Arrange
      ZoneInstanceService sut = configure(new ZoneInstanceService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getZoneInstancesRepository()).isNull();
      version.setLandingZonesRepository(new LandingZonesRepository());
      sut.init(version);
      assertThat(version.getZoneInstancesRepository()).isNull();
      version.setZoneTypesRepository(new ZoneTypesRepository());
      sut.init(version);
      assertThat(version.getZoneInstancesRepository()).isNull();
      version.setFirewallGroupsRepository(new FirewallGroupsRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getZoneInstancesRepository()).isNotNull();
    }

    @Test
    void initLinksFoundReferencesAndHandlesMissingEmptyAndNullReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      LandingZone landingZone = named(new LandingZone(), "landing-zone");
      ZoneType zoneType = named(new ZoneType(), "zone-type");
      FirewallGroup firewallGroup = named(new FirewallGroup(), "firewall-group");
      LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
      ZoneTypesRepository zoneTypesRepository = new ZoneTypesRepository();
      FirewallGroupsRepository firewallGroupsRepository = new FirewallGroupsRepository();
      landingZonesRepository.put(landingZone.id(), landingZone);
      zoneTypesRepository.put(zoneType.id(), zoneType);
      firewallGroupsRepository.put(firewallGroup.id(), firewallGroup);
      fixture.version.setLandingZonesRepository(landingZonesRepository);
      fixture.version.setZoneTypesRepository(zoneTypesRepository);
      fixture.version.setFirewallGroupsRepository(firewallGroupsRepository);
      fixture.addData(
          V1ZoneInstance.class,
          List.of(
              zoneInstance(
                  "linked",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "landing-zone",
                  "zone-type",
                  "firewall-group"),
              zoneInstance(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-zone",
                  "missing-type",
                  "missing-firewall"),
              zoneInstance("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", "", ""),
              zoneInstance("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null, null),
              zoneInstance("orphan", "missing-scope", "", "", "")));
      ZoneInstanceService sut = configure(new ZoneInstanceService());

      // Act
      sut.init(fixture.version);
      sut.initInternals(fixture.version);

      // Assert
      ZoneInstance linked =
          fixture
              .version
              .getZoneInstancesRepository()
              .get(BaseInstance.generateUniqueId("landing-zone", "linked"));
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getZoneInstances()).contains(linked);
      assertThat(linked.getLandingZone()).isSameAs(landingZone);
      assertThat(landingZone.getZoneInstances()).contains(linked);
      assertThat(linked.getZoneType()).isSameAs(zoneType);
      assertThat(zoneType.getZoneInstances()).contains(linked);
      assertThat(linked.getFirewallGroup()).isSameAs(firewallGroup);
      assertThat(firewallGroup.getZoneInstances()).contains(linked);
      assertThat(linked.getInternalDstCidrRangeSetForNSG().isEmpty()).isTrue();
      assertThat(
              instance(fixture.version.getZoneInstancesRepository(), "missing-zone", "missing")
                  .getLandingZone())
          .isNull();
      assertThat(instance(fixture.version.getZoneInstancesRepository(), "", "empty").getZoneType())
          .isNull();
      assertThat(instance(fixture.version.getZoneInstancesRepository(), null, "null").getScope())
          .isSameAs(fixture.scope);
      assertThat(instance(fixture.version.getZoneInstancesRepository(), "", "orphan").getScope())
          .isNull();
    }

    @Test
    void initInternalsHandlesMissingAndDataLessRepositories() {
      // Arrange
      ZoneInstanceService sut = configure(new ZoneInstanceService());
      Version missingRepositoryVersion = new Version();
      Version missingDataVersion = new Version();
      ZoneInstancesRepository repository = new ZoneInstancesRepository();
      repository.setData(null);
      missingDataVersion.setZoneInstancesRepository(repository);

      // Act and assert
      assertThatCode(() -> sut.initInternals(missingRepositoryVersion)).doesNotThrowAnyException();
      assertThatCode(() -> sut.initInternals(missingDataVersion)).doesNotThrowAnyException();
    }
  }

  @Nested
  class SubnetInstanceTests {
    @Test
    void initStopsAtEachMissingPrerequisite() {
      // Arrange
      SubnetInstanceService sut = configure(new SubnetInstanceService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getSubnetInstancesRepository()).isNull();
      version.setLandingZonesRepository(new LandingZonesRepository());
      sut.init(version);
      assertThat(version.getSubnetInstancesRepository()).isNull();
      version.setSubnetTypesRepository(new SubnetTypesRepository());
      sut.init(version);
      assertThat(version.getSubnetInstancesRepository()).isNull();
      version.setZoneInstancesRepository(new ZoneInstancesRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getSubnetInstancesRepository()).isNotNull();
    }

    @Test
    void initLinksTopologyBuildsHierarchyAndInitializesEndpoints() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      LandingZone landingZone = named(new LandingZone(), "landing-zone");
      SubnetType subnetType = named(new SubnetType(), "subnet-type");
      ZoneInstance zoneInstance = named(new ZoneInstance(), "zone");
      zoneInstance.setUniqueIdFrom(BaseInstance.generateUniqueId("landing-zone", "zone"));
      LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
      SubnetTypesRepository subnetTypesRepository = new SubnetTypesRepository();
      ZoneInstancesRepository zoneInstancesRepository = new ZoneInstancesRepository();
      landingZonesRepository.put(landingZone.id(), landingZone);
      subnetTypesRepository.put(subnetType.id(), subnetType);
      zoneInstancesRepository.put(zoneInstance.id(), zoneInstance);
      fixture.version.setLandingZonesRepository(landingZonesRepository);
      fixture.version.setSubnetTypesRepository(subnetTypesRepository);
      fixture.version.setZoneInstancesRepository(zoneInstancesRepository);
      fixture.addData(
          V1SubnetInstance.class,
          List.of(
              subnetInstance(
                  "parent",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "landing-zone",
                  "subnet-type",
                  "zone",
                  null,
                  "10.0.0.0/25"),
              subnetInstance(
                  "child",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "landing-zone",
                  "subnet-type",
                  "zone",
                  "parent",
                  "10.0.0.128/25"),
              subnetInstance(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-zone",
                  "missing-type",
                  "missing-zone-instance",
                  "missing-parent",
                  "10.1.0.0/24"),
              subnetInstance(
                  "empty", CoreDbServiceTestFixture.SCOPE_NAME, "", "", "", "", "10.2.0.0/24"),
              subnetInstance(
                  "null",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  null,
                  null,
                  null,
                  null,
                  "10.3.0.0/24"),
              subnetInstance("orphan", "missing-scope", "", "", "", null, "10.4.0.0/24")));
      SubnetInstanceService sut = configure(new SubnetInstanceService());

      // Act
      sut.init(fixture.version);

      // Assert
      SubnetInstance parent =
          fixture
              .version
              .getSubnetInstancesRepository()
              .get(BaseInstance.generateUniqueId("landing-zone", "parent"));
      SubnetInstance child =
          fixture
              .version
              .getSubnetInstancesRepository()
              .get(BaseInstance.generateUniqueId("landing-zone", "child"));
      assertThat(child.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getSubnetInstances()).contains(parent, child);
      assertThat(child.getLandingZone()).isSameAs(landingZone);
      assertThat(landingZone.getSubnetInstances()).contains(parent, child);
      assertThat(child.getSubnetType()).isSameAs(subnetType);
      assertThat(subnetType.getSubnetInstances()).contains(parent, child);
      assertThat(child.getZoneInstance()).isSameAs(zoneInstance);
      assertThat(zoneInstance.getSubnetInstances()).contains(parent, child);
      assertThat(child.getParent()).isSameAs(parent);
      assertThat(parent.getChildren()).contains(child);
      assertThat(child.getInternalEndpoints()).isNotEmpty();
      assertThat(
              fixture
                  .version
                  .getSubnetInstancesRepository()
                  .get(BaseInstance.generateUniqueId("missing-zone", "missing"))
                  .getParent())
          .isNull();
      assertThat(
              fixture
                  .version
                  .getSubnetInstancesRepository()
                  .get(BaseInstance.generateUniqueId("", "orphan"))
                  .getScope())
          .isNull();
    }
  }

  @Nested
  class IPAddressInstanceTests {
    @Test
    void initStopsAtMissingLandingZoneAndComponentInstanceRepositories() {
      // Arrange
      IPAddressInstanceService sut = configure(new IPAddressInstanceService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getIpAddressInstancesRepository()).isNull();
      version.setLandingZonesRepository(new LandingZonesRepository());
      sut.init(version);
      assertThat(version.getIpAddressInstancesRepository()).isNull();
      version.setComponentInstancesRepository(new ComponentInstancesRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getIpAddressInstancesRepository()).isNotNull();
    }

    @Test
    void initLinksFoundReferencesAndHandlesMissingEmptyAndNullReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      LandingZone landingZone = named(new LandingZone(), "landing-zone");
      Component component = fixture.addComponent("component");
      ComponentInstance componentInstance =
          componentInstance(
              "component-instance", CoreDbServiceTestFixture.SCOPE_NAME, "landing-zone", component);
      LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
      ComponentInstancesRepository componentInstancesRepository =
          new ComponentInstancesRepository();
      landingZonesRepository.put(landingZone.id(), landingZone);
      componentInstancesRepository.put(componentInstance.id(), componentInstance);
      fixture.version.setLandingZonesRepository(landingZonesRepository);
      fixture.version.setComponentInstancesRepository(componentInstancesRepository);
      fixture.addData(
          V1IPAddressInstance.class,
          List.of(
              ipAddress(
                  "linked",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "landing-zone",
                  "component-instance"),
              ipAddress(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-zone",
                  "missing-instance"),
              ipAddress("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", ""),
              ipAddress("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null),
              ipAddress("orphan", "missing-scope", "", "")));
      IPAddressInstanceService sut = configure(new IPAddressInstanceService());

      // Act
      sut.init(fixture.version);

      // Assert
      IPAddressInstance linked =
          fixture
              .version
              .getIpAddressInstancesRepository()
              .get(BaseInstance.generateUniqueId("component-instance", "linked"));
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getIpAddressInstances()).contains(linked);
      assertThat(linked.getLandingZone()).isSameAs(landingZone);
      assertThat(landingZone.getIpAddressInstances()).contains(linked);
      assertThat(linked.getComponentInstance()).isSameAs(componentInstance);
      assertThat(componentInstance.getIpAddressInstances()).contains(linked);
      assertThat(
              fixture
                  .version
                  .getIpAddressInstancesRepository()
                  .get(BaseInstance.generateUniqueId("missing-zone", "missing"))
                  .getLandingZone())
          .isNull();
      assertThat(
              fixture
                  .version
                  .getIpAddressInstancesRepository()
                  .get(BaseInstance.generateUniqueId("", "orphan"))
                  .getScope())
          .isNull();
    }
  }

  private static V1Region region(String name, String scopeName, String provider, String continent) {
    return new V1Region()
        .metadata(metadata(name, scopeName))
        .spec(new V1RegionSpec().provider(provider).continent(continent));
  }

  private static V1FirewallGroup firewallGroup(
      String name, String scopeName, String vendor, String target) {
    return new V1FirewallGroup()
        .metadata(metadata(name, scopeName))
        .spec(new V1FirewallGroupSpec().vendor(vendor).target(target));
  }

  private static V1ZoneInstance zoneInstance(
      String name, String scopeName, String landingZone, String zoneType, String firewallGroup) {
    V1ZoneInstance data =
        new V1ZoneInstance()
            .spec(new V1ZoneInstanceSpec().zoneType(zoneType).firewallGroup(firewallGroup));
    data.setMetadata(instanceMetadata(name, scopeName, landingZone));
    return data;
  }

  private static ZoneInstance instance(
      ZoneInstancesRepository repository, String landingZone, String name) {
    return repository.get(BaseInstance.generateUniqueId(landingZone, name));
  }

  private static V1SubnetInstance subnetInstance(
      String name,
      String scopeName,
      String landingZone,
      String subnetType,
      String zoneInstance,
      String parent,
      String cidr) {
    V1SubnetInstance data =
        new V1SubnetInstance()
            .spec(
                new V1SubnetInstanceSpec()
                    .subnetType(subnetType)
                    .zoneInstance(zoneInstance)
                    .parent(parent)
                    .cidr(cidr));
    data.setMetadata(instanceMetadata(name, scopeName, landingZone));
    return data;
  }

  private static V1IPAddressInstance ipAddress(
      String name, String scopeName, String landingZone, String componentInstance) {
    V1IPAddressInstance data =
        new V1IPAddressInstance()
            .spec(
                new V1IPAddressInstanceSpec()
                    .componentInstance(componentInstance)
                    .componentInstanceLandingZone(landingZone));
    data.setMetadata(instanceMetadata(name, scopeName, landingZone));
    return data;
  }
}

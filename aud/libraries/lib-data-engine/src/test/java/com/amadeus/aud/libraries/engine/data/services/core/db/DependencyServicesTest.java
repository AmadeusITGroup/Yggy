package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.component;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.componentInstance;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.instanceMetadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.named;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependenciesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1DependencySpec;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DependencyServicesTest {
  @Nested
  class DependencyTests {
    @Test
    void initStopsWithoutComponentsRepository() {
      // Arrange
      Version version = new Version();
      DependencyService sut = configure(new DependencyService());

      // Act
      sut.init(version);

      // Assert
      assertThat(version.getDependenciesRepository()).isNull();
    }

    @Test
    void initLinksFoundComponentsAndHandlesMissingBlankAndNullReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      Component source = fixture.addComponent("source");
      Component destination = fixture.addComponent("destination");
      ComponentsRepository componentsRepository = new ComponentsRepository();
      componentsRepository.put(source.id(), source);
      componentsRepository.put(destination.id(), destination);
      fixture.version.setComponentsRepository(componentsRepository);
      fixture.addData(
          V1Dependency.class,
          List.of(
              dependency(
                  "linked",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "source",
                  "destination",
                  "required connectivity"),
              dependency(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-source",
                  "missing-destination",
                  null),
              dependency("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", "", ""),
              dependency("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null, null),
              dependency("orphan", "missing-scope", "", "", null)));
      DependencyService sut = configure(new DependencyService());

      // Act
      sut.init(fixture.version);

      // Assert
      Dependency linked = fixture.version.getDependenciesRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getDependencies()).contains(linked);
      assertThat(linked.getSourceComponent()).isSameAs(source);
      assertThat(linked.getDestinationComponent()).isSameAs(destination);
      assertThat(source.getDependencies()).contains(linked);
      assertThat(destination.getDependencies()).contains(linked);
      assertThat(linked.getSecurityJustification()).isEqualTo("required connectivity");
      assertThat(fixture.version.getDependenciesRepository().get("missing").getSourceComponent())
          .isNull();
      assertThat(fixture.version.getDependenciesRepository().get("empty").getDestinationComponent())
          .isNull();
      assertThat(fixture.version.getDependenciesRepository().get("null").getSourceComponent())
          .isNull();
      assertThat(fixture.version.getDependenciesRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class DependencyInstanceTests {
    @Test
    void initStopsAtEachMissingPrerequisite() {
      // Arrange
      DependencyInstanceService sut = configure(new DependencyInstanceService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getDependencyInstancesRepository()).isNull();
      version.setComponentsRepository(new ComponentsRepository());
      sut.init(version);
      assertThat(version.getDependencyInstancesRepository()).isNull();
      version.setLandingZonesRepository(new LandingZonesRepository());
      sut.init(version);
      assertThat(version.getDependencyInstancesRepository()).isNull();
      version.setComponentInstancesRepository(new ComponentInstancesRepository());
      sut.init(version);
      assertThat(version.getDependencyInstancesRepository()).isNull();
      version.setDependenciesRepository(new DependenciesRepository());
      sut.init(version);
      assertThat(version.getDependencyInstancesRepository()).isNull();
      version.setServicesRepository(new ServicesRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getDependencyInstancesRepository()).isNotNull();
    }

    @Test
    void initLinksInstancesDependencyViaAndInheritedServices() {
      // Arrange
      DependencyInstanceFixture fixture = new DependencyInstanceFixture();
      V1DependencyInstanceSpec spec =
          new V1DependencyInstanceSpec()
              .componentInstance(fixture.sourceInstance.getName())
              .viaComponentInstance(fixture.viaInstance.getName())
              .viaService("proxy")
              .dependency(fixture.dependency.getName())
              .targetComponentInstance(fixture.targetInstance.getName())
              .isTargetAsComponent(false)
              .targetServices(List.of("https", "missing-service"));
      fixture.addData(dependencyInstance("linked", "landing-zone", spec));
      DependencyInstanceService sut = configure(new DependencyInstanceService());

      // Act
      sut.init(fixture.fixture.version);

      // Assert
      DependencyInstance linked = fixture.get("linked", "landing-zone");
      assertThat(linked.getScope()).isSameAs(fixture.fixture.scope);
      assertThat(fixture.fixture.scope.getDependencyInstances()).contains(linked);
      assertThat(linked.getLandingZone()).isSameAs(fixture.landingZone);
      assertThat(fixture.landingZone.getDependencyInstances()).contains(linked);
      assertThat(linked.getComponentInstance()).isSameAs(fixture.sourceInstance);
      assertThat(fixture.sourceInstance.getDependencyInstances()).contains(linked);
      assertThat(linked.getViaComponentInstance()).isSameAs(fixture.viaInstance);
      assertThat(fixture.viaInstance.getDependencyInstances()).contains(linked);
      assertThat(linked.getViaService()).isSameAs(fixture.viaService);
      assertThat(fixture.viaService.getDependencyInstances()).contains(linked);
      assertThat(linked.getDependency()).isSameAs(fixture.dependency);
      assertThat(fixture.dependency.getDependencyInstances()).contains(linked);
      assertThat(linked.getTargetComponentInstance()).isSameAs(fixture.targetInstance);
      assertThat(fixture.targetInstance.getDependencyInstances()).contains(linked);
      assertThat(linked.getServices()).containsExactly(fixture.targetService);
      assertThat(fixture.targetService.getDependencyInstances()).contains(linked);
      assertThat(linked.getInternalUseWideSubnets()).isEmpty();
    }

    @Test
    void initHonorsSourceAndTargetAsComponentAssociationFlags() {
      // Arrange
      DependencyInstanceFixture fixture = new DependencyInstanceFixture();
      ComponentInstance genericSource =
          componentInstance(
              "generic-source",
              CoreDbServiceTestFixture.SCOPE_NAME,
              "landing-zone",
              fixture.sourceComponent);
      ComponentInstance genericVia =
          componentInstance(
              "generic-via",
              CoreDbServiceTestFixture.SCOPE_NAME,
              "landing-zone",
              fixture.viaComponent);
      ComponentInstance genericTarget =
          componentInstance(
              "generic-target",
              CoreDbServiceTestFixture.SCOPE_NAME,
              "landing-zone",
              fixture.targetComponent);
      fixture.componentInstancesRepository.put(genericSource.id(), genericSource);
      fixture.componentInstancesRepository.put(genericVia.id(), genericVia);
      fixture.componentInstancesRepository.put(genericTarget.id(), genericTarget);
      V1DependencyInstanceSpec spec =
          new V1DependencyInstanceSpec()
              .componentInstance(genericSource.getName())
              .viaComponentInstance(genericVia.getName())
              .viaService("")
              .dependency(fixture.dependency.getName())
              .targetComponentInstance(genericTarget.getName())
              .isSourceAsComponent(true)
              .isTargetAsComponent(true)
              .targetServices(List.of())
              .targetService("https");
      fixture.addData(dependencyInstance("generic", "landing-zone", spec));
      DependencyInstanceService sut = configure(new DependencyInstanceService());

      // Act
      sut.init(fixture.fixture.version);

      // Assert
      DependencyInstance generic = fixture.get("generic", "landing-zone");
      assertThat(generic.isSourceAsComponent()).isTrue();
      assertThat(genericSource.getDependencyInstances()).isNull();
      assertThat(genericVia.getDependencyInstances()).isNull();
      assertThat(genericTarget.getDependencyInstances()).isNull();
      assertThat(generic.getServices()).containsExactly(fixture.targetService);
    }

    @Test
    void initUsesDependencyDestinationWhenTargetInstanceIsAbsent() {
      // Arrange
      DependencyInstanceFixture fixture = new DependencyInstanceFixture();
      V1DependencyInstanceSpec spec =
          new V1DependencyInstanceSpec()
              .dependency(fixture.dependency.getName())
              .targetService("https");
      fixture.addData(dependencyInstance("component-target", "landing-zone", spec));
      DependencyInstanceService sut = configure(new DependencyInstanceService());

      // Act
      sut.init(fixture.fixture.version);

      // Assert
      assertThat(fixture.get("component-target", "landing-zone").getServices())
          .containsExactly(fixture.targetService);
    }

    @Test
    void initForcesOfficeUsersDependencySourceToComponentMode() {
      // Arrange
      DependencyInstanceFixture fixture = new DependencyInstanceFixture();
      Component officeUsers =
          component(
              "Office users", CoreDbServiceTestFixture.SCOPE_NAME, false, false, false, List.of());
      fixture.componentsRepository.put(officeUsers.id(), officeUsers);
      Dependency officeDependency =
          dependencyEntity("office-dependency", officeUsers, fixture.targetComponent);
      fixture.dependenciesRepository.put(officeDependency.id(), officeDependency);
      V1DependencyInstanceSpec spec =
          new V1DependencyInstanceSpec()
              .componentInstance(fixture.sourceInstance.getName())
              .dependency(officeDependency.getName());
      fixture.addData(dependencyInstance("office", "landing-zone", spec));
      DependencyInstanceService sut = configure(new DependencyInstanceService());

      // Act
      sut.init(fixture.fixture.version);

      // Assert
      DependencyInstance office = fixture.get("office", "landing-zone");
      assertThat(office.isSourceAsComponent()).isTrue();
      assertThat(office.getDependency()).isSameAs(officeDependency);
    }

    @Test
    void initTracksLinuxAndWindowsAncestorsForWideSubnetUse() {
      // Arrange
      DependencyInstanceFixture fixture = new DependencyInstanceFixture();
      Component commonParent =
          component(
              "operating-system",
              CoreDbServiceTestFixture.SCOPE_NAME,
              true,
              false,
              false,
              List.of());
      Component linux =
          component("Linux", CoreDbServiceTestFixture.SCOPE_NAME, true, false, false, List.of());
      Component windows =
          component("Windows", CoreDbServiceTestFixture.SCOPE_NAME, true, false, false, List.of());
      linux.addParent(commonParent);
      windows.addParent(commonParent);
      fixture.componentsRepository.put(commonParent.id(), commonParent);
      fixture.componentsRepository.put(linux.id(), linux);
      fixture.componentsRepository.put(windows.id(), windows);
      Dependency wideDependency = dependencyEntity("wide", commonParent, fixture.targetComponent);
      fixture.dependenciesRepository.put(wideDependency.id(), wideDependency);
      fixture.addData(
          dependencyInstance(
              "wide",
              "landing-zone",
              new V1DependencyInstanceSpec()
                  .dependency(wideDependency.getName())
                  .isSourceAsComponent(true)));
      DependencyInstanceService sut = configure(new DependencyInstanceService());

      // Act
      sut.init(fixture.fixture.version);

      // Assert
      DependencyInstance wide = fixture.get("wide", "landing-zone");
      assertThat(wide.getInternalUseWideSubnets()).containsEntry(commonParent, true);
      assertThat(wide.isUseWideSubnet()).isTrue();
    }

    @Test
    void initHandlesMissingEmptyNullAndUnknownServiceReferences() {
      // Arrange
      DependencyInstanceFixture fixture = new DependencyInstanceFixture();
      List<String> blankServices = new ArrayList<>();
      blankServices.add(null);
      blankServices.add("");
      fixture.addData(
          dependencyInstance(
              "missing",
              "missing-zone",
              new V1DependencyInstanceSpec()
                  .componentInstance("missing-source")
                  .dependency("missing-dependency")
                  .targetComponentInstance("missing-target")),
          dependencyInstance(
              "empty",
              "",
              new V1DependencyInstanceSpec()
                  .componentInstance("")
                  .viaComponentInstance("")
                  .dependency("")
                  .targetComponentInstance("")
                  .targetServices(List.of())
                  .targetService("")),
          dependencyInstance(
              "null",
              null,
              new V1DependencyInstanceSpec()
                  .dependency(fixture.dependency.getName())
                  .targetServices(blankServices)),
          dependencyInstance("orphan", "", "missing-scope", new V1DependencyInstanceSpec()));
      DependencyInstanceService sut = configure(new DependencyInstanceService());

      // Act
      sut.init(fixture.fixture.version);

      // Assert
      DependencyInstance missing = fixture.get("missing", "missing-zone");
      assertThat(missing.getLandingZone()).isNull();
      assertThat(missing.getComponentInstance()).isNull();
      assertThat(missing.getDependency()).isNull();
      assertThat(missing.getTargetComponentInstance()).isNull();
      assertThat(fixture.get("empty", "").getServices()).isNull();
      assertThat(fixture.get("null", null).getServices()).isNull();
      assertThat(fixture.get("orphan", "").getScope()).isNull();
    }

    @Test
    void initReportsMissingViaInstanceBeforeCurrentNullDereferenceBehavior() {
      // Arrange
      DependencyInstanceFixture fixture = new DependencyInstanceFixture();
      fixture.addData(
          dependencyInstance(
              "missing-via",
              "landing-zone",
              new V1DependencyInstanceSpec()
                  .viaComponentInstance("missing-via")
                  .viaService("proxy")));
      DependencyInstanceService sut = configure(new DependencyInstanceService());

      // Act and assert
      assertThatThrownBy(() -> sut.init(fixture.fixture.version))
          .isInstanceOf(NullPointerException.class);
    }
  }

  private static V1Dependency dependency(
      String name, String scopeName, String source, String destination, String justification) {
    return new V1Dependency()
        .metadata(metadata(name, scopeName))
        .spec(
            new V1DependencySpec()
                .component(source)
                .targetComponent(destination)
                .securityJustification(justification));
  }

  private static Dependency dependencyEntity(String name, Component source, Component destination) {
    Dependency dependency =
        new Dependency(
            dependency(
                name,
                CoreDbServiceTestFixture.SCOPE_NAME,
                source.getName(),
                destination.getName(),
                null));
    dependency.setSourceComponent(source);
    dependency.setDestinationComponent(destination);
    return dependency;
  }

  private static V1DependencyInstance dependencyInstance(
      String name, String landingZone, V1DependencyInstanceSpec spec) {
    return dependencyInstance(name, landingZone, CoreDbServiceTestFixture.SCOPE_NAME, spec);
  }

  private static V1DependencyInstance dependencyInstance(
      String name, String landingZone, String scopeName, V1DependencyInstanceSpec spec) {
    V1DependencyInstance data = new V1DependencyInstance().spec(spec);
    data.setMetadata(instanceMetadata(name, scopeName, landingZone));
    return data;
  }

  private static Service service(String name, Component component) {
    Service service =
        new Service(
            new V1Service()
                .metadata(metadata(name, CoreDbServiceTestFixture.SCOPE_NAME))
                .spec(
                    new V1ServiceSpec()
                        .component(component.getName())
                        .ports(List.of("443"))
                        .protocol(V1ServiceSpec.ProtocolEnum.TCP)));
    service.setComponent(component);
    return service;
  }

  private static final class DependencyInstanceFixture {
    final CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
    final ComponentsRepository componentsRepository = new ComponentsRepository();
    final LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
    final ComponentInstancesRepository componentInstancesRepository =
        new ComponentInstancesRepository();
    final DependenciesRepository dependenciesRepository = new DependenciesRepository();
    final ServicesRepository servicesRepository = new ServicesRepository();
    final Component sourceComponent = fixture.addComponent("source");
    final Component targetServiceParent = fixture.addComponent("service-parent");
    final Component targetComponent = fixture.addComponent("target");
    final Component viaComponent = fixture.addComponent("via");
    final ComponentInstance sourceInstance =
        componentInstance(
            "source-instance",
            CoreDbServiceTestFixture.SCOPE_NAME,
            "landing-zone",
            sourceComponent);
    final ComponentInstance targetInstance =
        componentInstance(
            "target-instance",
            CoreDbServiceTestFixture.SCOPE_NAME,
            "landing-zone",
            targetComponent);
    final ComponentInstance viaInstance =
        componentInstance(
            "via-instance", CoreDbServiceTestFixture.SCOPE_NAME, "landing-zone", viaComponent);
    final LandingZone landingZone = named(new LandingZone(), "landing-zone");
    final Dependency dependency = dependencyEntity("dependency", sourceComponent, targetComponent);
    final Service targetService = service("https", targetServiceParent);
    final Service viaService = service("proxy", viaComponent);

    DependencyInstanceFixture() {
      targetComponent.addParent(targetServiceParent);
      for (Component component :
          List.of(sourceComponent, targetServiceParent, targetComponent, viaComponent)) {
        componentsRepository.put(component.id(), component);
      }
      for (ComponentInstance componentInstance :
          List.of(sourceInstance, targetInstance, viaInstance)) {
        componentInstancesRepository.put(componentInstance.id(), componentInstance);
      }
      landingZonesRepository.put(landingZone.id(), landingZone);
      dependenciesRepository.put(dependency.id(), dependency);
      servicesRepository.put(targetService.id(), targetService);
      servicesRepository.put(viaService.id(), viaService);
      fixture.version.setComponentsRepository(componentsRepository);
      fixture.version.setLandingZonesRepository(landingZonesRepository);
      fixture.version.setComponentInstancesRepository(componentInstancesRepository);
      fixture.version.setDependenciesRepository(dependenciesRepository);
      fixture.version.setServicesRepository(servicesRepository);
    }

    void addData(V1DependencyInstance... data) {
      List<V1DependencyInstance> models = List.of(data);
      fixture.addData(V1DependencyInstance.class, models);
    }

    DependencyInstance get(String name, String landingZone) {
      return fixture
          .version
          .getDependencyInstancesRepository()
          .get(BaseInstance.generateUniqueId(landingZone, name));
    }
  }
}

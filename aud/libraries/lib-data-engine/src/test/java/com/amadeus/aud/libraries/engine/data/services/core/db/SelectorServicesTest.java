package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.componentInstance;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.named;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.models.core.db.AccessControlEntity;
import com.amadeus.aud.libraries.models.core.db.AccessRightsEntryEntity;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.OutboundSNAT;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.AccessControl;
import com.amadeus.aud.libraries.yaml.models.AccessControlAllow;
import com.amadeus.aud.libraries.yaml.models.AccessControlDeny;
import com.amadeus.aud.libraries.yaml.models.AccessControlDenyAnyOf;
import com.amadeus.aud.libraries.yaml.models.AccessDenialMessage;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntry;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntryScopes;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNAT;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNATSpec;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SelectorServicesTest {
  @Nested
  class OutboundSNATTests {
    @Test
    void initStopsAtEachMissingPrerequisite() {
      // Arrange
      OutboundSNATService sut = configure(new OutboundSNATService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getOutboundSNATsRepository()).isNull();
      version.setMacroPhasesRepository(new MacroPhasesRepository());
      sut.init(version);
      assertThat(version.getOutboundSNATsRepository()).isNull();
      version.setContinentsRepository(new ContinentsRepository());
      sut.init(version);
      assertThat(version.getOutboundSNATsRepository()).isNull();
      version.setZoneTypesRepository(new ZoneTypesRepository());
      sut.init(version);
      assertThat(version.getOutboundSNATsRepository()).isNull();
      version.setComponentsRepository(new ComponentsRepository());
      sut.init(version);
      assertThat(version.getOutboundSNATsRepository()).isNull();
      version.setComponentInstancesRepository(new ComponentInstancesRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getOutboundSNATsRepository()).isNotNull();
    }

    @Test
    void initLinksScalarAndCollectionSelectorsAndHandlesMissingEntries() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      MacroPhase macroPhase = named(new MacroPhase(), "macro-phase");
      Continent continent = named(new Continent(), "continent");
      ZoneType zoneType = named(new ZoneType(), "zone-type");
      Component component = fixture.addComponent("component");
      ComponentInstance componentInstance =
          componentInstance(
              "component-instance", CoreDbServiceTestFixture.SCOPE_NAME, "landing-zone", component);
      MacroPhasesRepository macroPhasesRepository = new MacroPhasesRepository();
      ContinentsRepository continentsRepository = new ContinentsRepository();
      ZoneTypesRepository zoneTypesRepository = new ZoneTypesRepository();
      ComponentsRepository componentsRepository = new ComponentsRepository();
      ComponentInstancesRepository componentInstancesRepository =
          new ComponentInstancesRepository();
      macroPhasesRepository.put(macroPhase.id(), macroPhase);
      continentsRepository.put(continent.id(), continent);
      zoneTypesRepository.put(zoneType.id(), zoneType);
      componentsRepository.put(component.id(), component);
      componentInstancesRepository.put(componentInstance.id(), componentInstance);
      fixture.version.setMacroPhasesRepository(macroPhasesRepository);
      fixture.version.setContinentsRepository(continentsRepository);
      fixture.version.setZoneTypesRepository(zoneTypesRepository);
      fixture.version.setComponentsRepository(componentsRepository);
      fixture.version.setComponentInstancesRepository(componentInstancesRepository);
      fixture.addData(
          V1OutboundSNAT.class,
          List.of(
              outboundSNAT(
                  "linked",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "macro-phase",
                  "continent",
                  "zone-type",
                  List.of("component", "missing-component"),
                  List.of("component-instance", "missing-instance")),
              outboundSNAT(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-phase",
                  "missing-continent",
                  "missing-type",
                  List.of("missing-component"),
                  List.of("missing-instance")),
              outboundSNAT(
                  "empty", CoreDbServiceTestFixture.SCOPE_NAME, "", "", "", List.of(), List.of()),
              outboundSNAT(
                  "null", CoreDbServiceTestFixture.SCOPE_NAME, null, null, null, null, null),
              outboundSNAT("orphan", "missing-scope", "", "", "", List.of(), List.of())));
      OutboundSNATService sut = configure(new OutboundSNATService());

      // Act
      sut.init(fixture.version);

      // Assert
      OutboundSNAT linked = fixture.version.getOutboundSNATsRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getOutboundSNATs()).contains(linked);
      assertThat(linked.getMacroPhase()).isSameAs(macroPhase);
      assertThat(linked.getContinent()).isSameAs(continent);
      assertThat(linked.getZoneType()).isSameAs(zoneType);
      assertThat(linked.getComponents()).containsExactly(component);
      assertThat(linked.getComponentInstances()).containsExactly(componentInstance);
      assertThat(fixture.version.getOutboundSNATsRepository().get("missing").getMacroPhase())
          .isNull();
      assertThat(fixture.version.getOutboundSNATsRepository().get("empty").getComponents())
          .isNull();
      assertThat(fixture.version.getOutboundSNATsRepository().get("null").getComponentInstances())
          .isNull();
      assertThat(fixture.version.getOutboundSNATsRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class AccessControlTests {
    @Test
    void initWithoutDataOrComponentEntriesIsANoOp() {
      // Arrange
      AccessControlService sut = configure(new AccessControlService());
      Version missingDataVersion = new Version();
      Version emptyDataVersion = new Version();
      emptyDataVersion.setAudDataRepository(new AudDataRepository());

      // Act and assert
      assertThatCode(() -> sut.init(missingDataVersion)).doesNotThrowAnyException();
      assertThatCode(() -> sut.init(emptyDataVersion)).doesNotThrowAnyException();
      assertThat(missingDataVersion.getAccessControlEntitiesRepository()).isNull();
      assertThat(emptyDataVersion.getAccessControlEntitiesRepository()).isNull();
    }

    @Test
    void initCreatesDeduplicatedAclRightsRepositoriesAndComponentLinks() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      Scope allowedScope = fixture.addScope("allowed-scope");
      AccessRightsEntry denyEntry =
          accessRight(
              AccessRightsEntry.PermissionEnum.COMPONENT_DEPENDENCY_SRC,
              List.of("allowed-scope", "missing-scope"),
              "Denied {edge}",
              AccessDenialMessage.UserMessageSeverityEnum.ERROR);
      AccessRightsEntry allowEntry =
          accessRight(
              AccessRightsEntry.PermissionEnum.COMPONENT_DEPENDENCY_DST,
              List.of("allowed-scope"),
              null,
              null);
      AccessControl accessControl =
          new AccessControl()
              .deny(
                  new AccessControlDeny(
                      new AccessControlDenyAnyOf().accessRights(List.of(denyEntry))))
              .allow(new AccessControlAllow().accessRights(List.of(allowEntry)));
      V1Component firstData = component("first", accessControl);
      V1Component secondData = component("second", accessControl);
      Component first = new Component(firstData);
      Component second = new Component(secondData);
      ComponentsRepository componentsRepository = new ComponentsRepository();
      componentsRepository.put(first.id(), first);
      componentsRepository.put(second.id(), second);
      fixture.version.setComponentsRepository(componentsRepository);
      fixture.addData(V1Component.class, List.of(firstData, secondData));
      AccessControlService sut = configure(new AccessControlService());

      // Act
      sut.init(fixture.version);
      sut.init(fixture.version);

      // Assert
      assertThat(fixture.version.getAccessControlEntitiesRepository().getData()).hasSize(1);
      assertThat(fixture.version.getAccessRightsEntryEntitiesRepository().getData()).hasSize(2);
      AccessControlEntity entity =
          fixture.version.getAccessControlEntitiesRepository().getData().values().iterator().next();
      assertThat(entity.getControlledComponents()).containsExactlyInAnyOrder(first, second);
      assertThat(entity.getDenyAccessRights()).hasSize(1);
      assertThat(entity.getAllowAccessRights()).hasSize(1);
      AccessRightsEntryEntity storedDeny = entity.getDenyAccessRights().iterator().next();
      AccessRightsEntryEntity storedAllow = entity.getAllowAccessRights().iterator().next();
      assertThat(storedDeny.getApplyToScopes()).containsExactly(allowedScope);
      assertThat(storedAllow.getApplyToScopes()).containsExactly(allowedScope);
    }

    @Test
    void initHandlesNullAndEmptyRightsListsAndGlobalDenyVariant() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      AccessControl emptyRights =
          new AccessControl()
              .deny(new AccessControlDeny(new AccessControlDenyAnyOf().accessRights(null)))
              .allow(new AccessControlAllow().accessRights(List.of()));
      AccessControl globalDeny =
          new AccessControl()
              .deny(
                  new AccessControlDeny(
                      new AccessDenialMessage()
                          .userMessage("globally denied")
                          .userMessageSeverity(
                              AccessDenialMessage.UserMessageSeverityEnum.WARNING)));
      V1Component emptyData = component("empty-rights", emptyRights);
      V1Component globalData = component("global-deny", globalDeny);
      Component emptyComponent = new Component(emptyData);
      Component globalComponent = new Component(globalData);
      ComponentsRepository componentsRepository = new ComponentsRepository();
      componentsRepository.put(emptyComponent.id(), emptyComponent);
      componentsRepository.put(globalComponent.id(), globalComponent);
      fixture.version.setComponentsRepository(componentsRepository);
      fixture.addData(V1Component.class, List.of(emptyData, globalData, component("no-acl", null)));
      AccessControlService sut = configure(new AccessControlService());

      // Act
      sut.init(fixture.version);

      // Assert
      assertThat(fixture.version.getAccessControlEntitiesRepository().getData()).hasSize(2);
      assertThat(fixture.version.getAccessRightsEntryEntitiesRepository().getData()).isEmpty();
      assertThat(fixture.version.getAccessControlEntitiesRepository().getData().values())
          .anySatisfy(
              (AccessControlEntity entity) -> {
                assertThat(entity.getGlobalDenyMessageText()).isEqualTo("globally denied");
                assertThat(entity.getControlledComponents()).containsExactly(globalComponent);
              });
    }

    @Test
    void initRightsWithoutScopesRepositoryProducesEmptyScopeAssociations() {
      // Arrange
      Version version = new Version();
      AudDataRepository dataRepository = new AudDataRepository();
      ComponentsRepository componentsRepository = new ComponentsRepository();
      AccessControl accessControl =
          new AccessControl()
              .allow(
                  new AccessControlAllow()
                      .accessRights(
                          List.of(
                              accessRight(
                                  AccessRightsEntry.PermissionEnum.COMPONENT_INHERIT,
                                  null,
                                  null,
                                  null))));
      V1Component data = component("component", accessControl);
      Component component = new Component(data);
      componentsRepository.put(component.id(), component);
      dataRepository.getData().put(V1Component.class, List.of(data));
      version.setAudDataRepository(dataRepository);
      version.setComponentsRepository(componentsRepository);
      AccessControlService sut = configure(new AccessControlService());

      // Act
      sut.init(version);

      // Assert
      AccessControlEntity entity =
          version.getAccessControlEntitiesRepository().getData().values().iterator().next();
      assertThat(entity.getAllowAccessRights()).isEmpty();
      assertThat(version.getAccessRightsEntryEntitiesRepository().getData()).isEmpty();
    }

    @Test
    void initCurrentlyRejectsAclForUnknownComponent() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.version.setComponentsRepository(new ComponentsRepository());
      fixture.addData(V1Component.class, List.of(component("missing", new AccessControl())));
      AccessControlService sut = configure(new AccessControlService());

      // Act and assert
      assertThatThrownBy(() -> sut.init(fixture.version)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void initCurrentlyRejectsNullAllowRightsList() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      AccessControl accessControl =
          new AccessControl().allow(new AccessControlAllow().accessRights(null));
      V1Component data = component("component", accessControl);
      ComponentsRepository componentsRepository = new ComponentsRepository();
      componentsRepository.put("component", new Component(data));
      fixture.version.setComponentsRepository(componentsRepository);
      fixture.addData(V1Component.class, List.of(data));
      AccessControlService sut = configure(new AccessControlService());

      // Act and assert
      assertThatThrownBy(() -> sut.init(fixture.version)).isInstanceOf(NullPointerException.class);
    }
  }

  private static V1OutboundSNAT outboundSNAT(
      String name,
      String scopeName,
      String macroPhase,
      String continent,
      String zoneType,
      List<String> components,
      List<String> componentInstances) {
    V1OutboundSNATSpec spec =
        new V1OutboundSNATSpec()
            .translatedip("192.0.2.1")
            .macrophase(macroPhase)
            .continent(continent)
            .zonetype(zoneType)
            .components(components)
            .componentInstances(componentInstances)
            .cidrs(List.of("10.0.0.0/24"))
            .destinationCidrs(List.of("0.0.0.0/0"))
            .protocol(V1OutboundSNATSpec.ProtocolEnum.TCP);
    return new V1OutboundSNAT().metadata(metadata(name, scopeName)).spec(spec);
  }

  private static V1Component component(String name, AccessControl accessControl) {
    return new V1Component()
        .metadata(metadata(name, CoreDbServiceTestFixture.SCOPE_NAME))
        .spec(new V1ComponentSpec().parents(List.of()).accessControl(accessControl));
  }

  private static AccessRightsEntry accessRight(
      AccessRightsEntry.PermissionEnum permission,
      List<String> scopes,
      String message,
      AccessDenialMessage.UserMessageSeverityEnum severity) {
    AccessDenialMessage denialMessage =
        message == null
            ? null
            : new AccessDenialMessage().userMessage(message).userMessageSeverity(severity);
    return new AccessRightsEntry()
        .permission(permission)
        .reportIfDenied(denialMessage)
        .applyTo(new AccessRightsEntryScopes().scopes(scopes));
  }
}

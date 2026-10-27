package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.named;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationIdsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlockedPortsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ZoneTypesRepository;
import com.amadeus.aud.libraries.models.core.db.ApplicationId;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ConnectionPhase;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.ProtocolEnum;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationPhase;
import com.amadeus.aud.libraries.yaml.models.V1ApplicationPhaseSpec;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPort;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPortSpec;
import com.amadeus.aud.libraries.yaml.models.V1Connection;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhase;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ServiceAndConnectionServicesTest {
  @Nested
  class ServiceTests {
    @Test
    void initStopsAtEachMissingPrerequisite() {
      // Arrange
      ServiceService sut = configure(new ServiceService());
      Version version = new Version();

      // Act and assert
      sut.init(version);
      assertThat(version.getServicesRepository()).isNull();
      version.setComponentsRepository(new ComponentsRepository());
      sut.init(version);
      assertThat(version.getServicesRepository()).isNull();
      version.setApplicationIdsRepository(new ApplicationIdsRepository());
      sut.init(version);
      assertThat(version.getServicesRepository()).isNull();
      version.setBlockedPortsRepository(new BlockedPortsRepository());
      version.setAudDataRepository(new AudDataRepository());
      version.setScopesRepository(new CoreDbServiceTestFixture().scopesRepository);
      sut.init(version);
      assertThat(version.getServicesRepository()).isNotNull();
    }

    @Test
    void initLinksReferencesBuildsPortRangesAndInstallsDefaultServices() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      Component component = fixture.addComponent("component");
      ApplicationId applicationId = named(new ApplicationId(), "application-id");
      ComponentsRepository componentsRepository = new ComponentsRepository();
      ApplicationIdsRepository applicationIdsRepository = new ApplicationIdsRepository();
      BlockedPortsRepository blockedPortsRepository = new BlockedPortsRepository();
      componentsRepository.put(component.id(), component);
      applicationIdsRepository.put(applicationId.id(), applicationId);
      V1BlockedPort blockedPortData =
          new V1BlockedPort()
              .metadata(metadata("udp-53", CoreDbServiceTestFixture.SCOPE_NAME))
              .spec(new V1BlockedPortSpec().port(53).protocol(V1BlockedPortSpec.ProtocolEnum.UDP));
      BlockedPort blockedPort = new BlockedPort(blockedPortData);
      blockedPortsRepository.put(blockedPort.id(), blockedPort);
      fixture.version.setComponentsRepository(componentsRepository);
      fixture.version.setApplicationIdsRepository(applicationIdsRepository);
      fixture.version.setBlockedPortsRepository(blockedPortsRepository);
      fixture.addData(
          V1Service.class,
          List.of(
              service(
                  "https",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "component",
                  "application-id",
                  List.of("443", "8443-8444"),
                  ProtocolEnum.TCP),
              service(
                  "non-blocked",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "component",
                  "",
                  List.of("NonBlockedPorts"),
                  ProtocolEnum.UDP),
              service(
                  "ping",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "",
                  null,
                  List.of(),
                  ProtocolEnum.ICMP),
              service(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-component",
                  "missing-application",
                  List.of("80"),
                  ProtocolEnum.TCP),
              service(
                  "null",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  null,
                  null,
                  List.of("80"),
                  ProtocolEnum.TCP),
              service("orphan", "missing-scope", "", "", List.of("80"), ProtocolEnum.TCP)));
      ServiceService sut = configure(new ServiceService());

      // Act
      sut.init(fixture.version);

      // Assert
      ServicesRepository repository = fixture.version.getServicesRepository();
      Service https = repository.get(String.join(BaseInstance.ID_SEPARATOR, "component", "https"));
      Service nonBlocked =
          repository.get(String.join(BaseInstance.ID_SEPARATOR, "component", "non-blocked"));
      Service ping = repository.get(String.join(BaseInstance.ID_SEPARATOR, "null", "ping"));
      assertThat(https.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getServices()).contains(https, nonBlocked, ping);
      assertThat(https.getComponent()).isSameAs(component);
      assertThat(component.getServices()).contains(https, nonBlocked);
      assertThat(https.getApplicationId()).isSameAs(applicationId);
      assertThat(applicationId.getServices()).contains(https);
      assertThat(https.getPortsRange().contains(443)).isTrue();
      assertThat(https.getPortsRange().contains(8444)).isTrue();
      assertThat(nonBlocked.getPortsRange().contains(52)).isTrue();
      assertThat(nonBlocked.getPortsRange().contains(53)).isFalse();
      assertThat(ping.getProtocol()).isEqualTo(ProtocolEnum.ICMP);
      assertThat(ping.getApprovedProtocol()).isEqualTo(ProtocolEnum.ICMP);
      assertThat(ping.getServiceNowId()).isEqualTo("ICMP AUTO approved");
      assertThat(
              repository
                  .get(String.join(BaseInstance.ID_SEPARATOR, "null", "missing"))
                  .getComponent())
          .isNull();
      assertThat(
              repository
                  .get(String.join(BaseInstance.ID_SEPARATOR, "null", "null"))
                  .getApplicationId())
          .isNull();
      assertThat(
              repository.get(String.join(BaseInstance.ID_SEPARATOR, "null", "orphan")).getScope())
          .isNull();
      assertThat(repository.getUdpService().getPortsRange().contains(53)).isFalse();
      assertThat(repository.getTcpService()).isNotNull();
      assertThat(repository.getAllService()).isNotNull();
    }
  }

  @Nested
  class ConnectionTests {
    @Test
    void initStopsWithoutZoneTypesRepository() {
      // Arrange
      Version version = new Version();
      ConnectionService sut = configure(new ConnectionService());

      // Act
      sut.init(version);

      // Assert
      assertThat(version.getConnectionsRepository()).isNull();
    }

    @Test
    void initLinksSourceAndDestinationAndHandlesMissingEmptyAndNullReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      ZoneType source = named(new ZoneType(), "source-zone");
      ZoneType destination = named(new ZoneType(), "destination-zone");
      ZoneTypesRepository repository = new ZoneTypesRepository();
      repository.put(source.id(), source);
      repository.put(destination.id(), destination);
      fixture.version.setZoneTypesRepository(repository);
      fixture.addData(
          V1Connection.class,
          List.of(
              connection(
                  "linked", CoreDbServiceTestFixture.SCOPE_NAME, "source-zone", "destination-zone"),
              connection(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-source",
                  "missing-destination"),
              connection("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", ""),
              connection("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null),
              connection("orphan", "missing-scope", "", "")));
      ConnectionService sut = configure(new ConnectionService());

      // Act
      sut.init(fixture.version);

      // Assert
      Connection linked = fixture.version.getConnectionsRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getConnections()).contains(linked);
      assertThat(linked.getSourceZoneType()).isSameAs(source);
      assertThat(linked.getDestinationZoneType()).isSameAs(destination);
      assertThat(source.getConnections()).contains(linked);
      assertThat(destination.getConnections()).contains(linked);
      assertThat(fixture.version.getConnectionsRepository().get("missing").getSourceZoneType())
          .isNull();
      assertThat(fixture.version.getConnectionsRepository().get("empty").getDestinationZoneType())
          .isNull();
      assertThat(fixture.version.getConnectionsRepository().get("null").getSourceZoneType())
          .isNull();
      assertThat(fixture.version.getConnectionsRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class ConnectionPhaseTests {
    @Test
    void initStopsWithoutMacroPhasesRepository() {
      // Arrange
      Version version = new Version();
      ConnectionPhaseService sut = configure(new ConnectionPhaseService());

      // Act
      sut.init(version);

      // Assert
      assertThat(version.getConnectionPhasesRepository()).isNull();
    }

    @Test
    void initLinksSourceAndDestinationAndHandlesMissingEmptyAndNullReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      MacroPhase source = named(new MacroPhase(), "source-phase");
      MacroPhase destination = named(new MacroPhase(), "destination-phase");
      MacroPhasesRepository repository = new MacroPhasesRepository();
      repository.put(source.id(), source);
      repository.put(destination.id(), destination);
      fixture.version.setMacroPhasesRepository(repository);
      fixture.addData(
          V1ConnectionPhase.class,
          List.of(
              connectionPhase(
                  "linked",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "source-phase",
                  "destination-phase"),
              connectionPhase(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-source",
                  "missing-destination"),
              connectionPhase("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", ""),
              connectionPhase("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null),
              connectionPhase("orphan", "missing-scope", "", "")));
      ConnectionPhaseService sut = configure(new ConnectionPhaseService());

      // Act
      sut.init(fixture.version);

      // Assert
      ConnectionPhase linked = fixture.version.getConnectionPhasesRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getConnectionPhases()).contains(linked);
      assertThat(linked.getSourcePhase()).isSameAs(source);
      assertThat(linked.getDestinationPhase()).isSameAs(destination);
      assertThat(source.getConnectionPhases()).contains(linked);
      assertThat(destination.getConnectionPhases()).contains(linked);
      assertThat(fixture.version.getConnectionPhasesRepository().get("missing").getSourcePhase())
          .isNull();
      assertThat(fixture.version.getConnectionPhasesRepository().get("empty").getDestinationPhase())
          .isNull();
      assertThat(fixture.version.getConnectionPhasesRepository().get("null").getSourcePhase())
          .isNull();
      assertThat(fixture.version.getConnectionPhasesRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class ApplicationPhaseTests {
    @Test
    void initStopsWithoutMacroPhasesRepository() {
      // Arrange
      Version version = new Version();
      ApplicationPhaseService sut = configure(new ApplicationPhaseService());

      // Act
      sut.init(version);

      // Assert
      assertThat(version.getApplicationPhasesRepository()).isNull();
    }

    @Test
    void initLinksMacroPhaseAndHandlesMissingEmptyNullAndOrphanReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      MacroPhase macroPhase = named(new MacroPhase(), "macro-phase");
      MacroPhasesRepository repository = new MacroPhasesRepository();
      repository.put(macroPhase.id(), macroPhase);
      fixture.version.setMacroPhasesRepository(repository);
      fixture.addData(
          V1ApplicationPhase.class,
          List.of(
              applicationPhase("linked", CoreDbServiceTestFixture.SCOPE_NAME, "macro-phase"),
              applicationPhase("missing", CoreDbServiceTestFixture.SCOPE_NAME, "missing-phase"),
              applicationPhase("empty", CoreDbServiceTestFixture.SCOPE_NAME, ""),
              applicationPhase("null", CoreDbServiceTestFixture.SCOPE_NAME, null),
              applicationPhase("orphan", "missing-scope", "")));
      ApplicationPhaseService sut = configure(new ApplicationPhaseService());

      // Act
      sut.init(fixture.version);

      // Assert
      ApplicationPhase linked = fixture.version.getApplicationPhasesRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getApplicationPhases()).contains(linked);
      assertThat(linked.getMacroPhase()).isSameAs(macroPhase);
      assertThat(macroPhase.getApplicationPhases()).contains(linked);
      assertThat(fixture.version.getApplicationPhasesRepository().get("missing").getMacroPhase())
          .isNull();
      assertThat(fixture.version.getApplicationPhasesRepository().get("empty").getMacroPhase())
          .isNull();
      assertThat(fixture.version.getApplicationPhasesRepository().get("null").getMacroPhase())
          .isNull();
      assertThat(fixture.version.getApplicationPhasesRepository().get("orphan").getScope())
          .isNull();
    }
  }

  private static V1Service service(
      String name,
      String scopeName,
      String component,
      String applicationId,
      List<String> ports,
      ProtocolEnum protocol) {
    return new V1Service()
        .metadata(metadata(name, scopeName))
        .spec(
            new V1ServiceSpec()
                .component(component)
                .applicationId(applicationId)
                .ports(ports)
                .protocol(protocol)
                .securityJustification("service justification"));
  }

  private static V1Connection connection(
      String name, String scopeName, String source, String destination) {
    return new V1Connection()
        .metadata(metadata(name, scopeName))
        .spec(new V1ConnectionSpec().sourceZoneType(source).destinationZoneType(destination));
  }

  private static V1ConnectionPhase connectionPhase(
      String name, String scopeName, String source, String destination) {
    return new V1ConnectionPhase()
        .metadata(metadata(name, scopeName))
        .spec(new V1ConnectionPhaseSpec().sourcePhase(source).destinationPhase(destination));
  }

  private static V1ApplicationPhase applicationPhase(
      String name, String scopeName, String macroPhase) {
    return new V1ApplicationPhase()
        .metadata(metadata(name, scopeName))
        .spec(new V1ApplicationPhaseSpec().macroPhase(macroPhase));
  }
}

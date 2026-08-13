package com.amadeus.aud.libraries.engine.graph.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.graph.models.GraphBuilder;
import com.amadeus.aud.libraries.models.core.db.ApplicationPhase;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.ServiceTag;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GraphPopulationTest {

  @Nested
  class DefaultGraphServiceTests {

    @Test
    void throwsWhenDependencyRepositoryIsMissing() {
      // Arrange
      DefaultGraphService sut = new DefaultGraphService();
      Version version = mock(Version.class);
      GraphBuilder builder = new GraphBuilder("v1");
      doReturn(null).when(version).getDependencyInstancesRepository();

      // Act / Assert
      assertThatThrownBy(() -> sut.populateGraphFromDependencies(builder, version))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void populatesGraphForNonGenericSourceAndTarget() {
      // Arrange
      DefaultGraphService sut = new DefaultGraphService();
      GraphBuilder builder = new GraphBuilder("v1", true);
      Version version = mock(Version.class);
      DependencyInstancesRepository repository = mock(DependencyInstancesRepository.class);
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ConcurrentHashMap<String, DependencyInstance> data = new ConcurrentHashMap<>();
      data.put("dep-id", dependencyInstance);
      doReturn(repository).when(version).getDependencyInstancesRepository();
      doReturn(data).when(repository).getData();

      // Act
      sut.populateGraphFromDependencies(builder, version);

      // Assert
      assertThat(builder.nodesSize()).isEqualTo(2);
      assertThat(builder.edgesSize()).isOne();
    }

    @Test
    void populatesGraphForGenericSourceAndGenericTarget() {
      // Arrange
      DefaultGraphService sut = new DefaultGraphService();
      GraphBuilder builder = new GraphBuilder("v1", true);
      Version version = mock(Version.class);
      DependencyInstancesRepository repository = mock(DependencyInstancesRepository.class);
      DependencyInstance dependencyInstance = baseDependencyInstance(true, true);
      ConcurrentHashMap<String, DependencyInstance> data = new ConcurrentHashMap<>();
      data.put("dep-id", dependencyInstance);
      doReturn(repository).when(version).getDependencyInstancesRepository();
      doReturn(data).when(repository).getData();

      // Act
      sut.populateGraphFromDependencies(builder, version);

      // Assert
      assertThat(builder.nodesSize()).isEqualTo(4);
      assertThat(builder.edgesSize()).isEqualTo(4);
    }

    @Test
    void returnsWhenDependencyRepositoryIsEmpty() {
      // Arrange
      DefaultGraphService sut = new DefaultGraphService();
      GraphBuilder builder = new GraphBuilder("v1", true);
      Version version = mock(Version.class);
      DependencyInstancesRepository repository = mock(DependencyInstancesRepository.class);
      doReturn(repository).when(version).getDependencyInstancesRepository();
      doReturn(new ConcurrentHashMap<String, DependencyInstance>()).when(repository).getData();

      // Act
      sut.populateGraphFromDependencies(builder, version);

      // Assert
      assertThat(builder.nodesSize()).isZero();
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void populatesGraphForNonGenericSourceAndGenericTarget() {
      // Arrange
      DefaultGraphService sut = new DefaultGraphService();
      GraphBuilder builder = new GraphBuilder("v1", true);
      Version version = mock(Version.class);
      DependencyInstancesRepository repository = mock(DependencyInstancesRepository.class);
      DependencyInstance dependencyInstance = baseDependencyInstance(false, true);
      ConcurrentHashMap<String, DependencyInstance> data = new ConcurrentHashMap<>();
      data.put("dep-id", dependencyInstance);
      doReturn(repository).when(version).getDependencyInstancesRepository();
      doReturn(data).when(repository).getData();

      // Act
      sut.populateGraphFromDependencies(builder, version);

      // Assert
      assertThat(builder.edgesSize()).isEqualTo(2);
    }

    @Test
    void populatesGraphForGenericSourceAndNonGenericTarget() {
      // Arrange
      DefaultGraphService sut = new DefaultGraphService();
      GraphBuilder builder = new GraphBuilder("v1", true);
      Version version = mock(Version.class);
      DependencyInstancesRepository repository = mock(DependencyInstancesRepository.class);
      DependencyInstance dependencyInstance = baseDependencyInstance(true, false);
      ConcurrentHashMap<String, DependencyInstance> data = new ConcurrentHashMap<>();
      data.put("dep-id", dependencyInstance);
      doReturn(repository).when(version).getDependencyInstancesRepository();
      doReturn(data).when(repository).getData();

      // Act
      sut.populateGraphFromDependencies(builder, version);

      // Assert
      assertThat(builder.edgesSize()).isEqualTo(2);
    }

    @Test
    void skipsTopLevelGenericDependencyInstances() {
      // Arrange
      DefaultGraphService sut = new DefaultGraphService();
      GraphBuilder builder = new GraphBuilder("v1", true);
      Version version = mock(Version.class);
      DependencyInstancesRepository repository = mock(DependencyInstancesRepository.class);
      DependencyInstance dependencyInstance = baseDependencyInstance(true, true);
      Component sourceComponent = dependencyInstance.getDependency().getSourceComponent();
      Component destinationComponent = dependencyInstance.getDependency().getDestinationComponent();
      ConcurrentHashMap<String, DependencyInstance> data = new ConcurrentHashMap<>();
      data.put("dep-id", dependencyInstance);
      doReturn(repository).when(version).getDependencyInstancesRepository();
      doReturn(data).when(repository).getData();
      doReturn("toplevel").when(sourceComponent).getName();
      doReturn("toplevel").when(destinationComponent).getName();

      // Act
      sut.populateGraphFromDependencies(builder, version);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }
  }

  @Nested
  class EdgeFactoryTests {

    @Test
    void addsEdgeForValidInstances() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1", true);
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.nodesSize()).isEqualTo(2);
      assertThat(builder.edgesSize()).isOne();
    }

    @Test
    void doesNotAddEdgeWhenFilteredByProviderLimits() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      doReturn(true).when(dependencyInstance).isSourceLimitedToCloudOnly();
      Target sourceTarget = source.getLandingZone().getTarget();
      doReturn("Amadeus").when(sourceTarget).getProviderName();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenTargetProviderLegacyConstraintFails() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      doReturn(true).when(dependencyInstance).isTargetLimitedToCloudOnly();
      Target destinationTarget = destination.getLandingZone().getTarget();
      doReturn("Amadeus").when(destinationTarget).getProviderName();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          destination,
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenFilteredBySourceLandingZoneConstraint() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      doReturn(true).when(dependencyInstance).isSourceLimitedToLandingZone();
      doReturn(componentLandingZone("other-lz", "other-target", "OtherProvider"))
          .when(dependencyInstance)
          .getLandingZone();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenFilteredBySourceTargetConstraint() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isSourceLimitedToTarget();
      doReturn(componentLandingZone("dep-lz", "other-target", "ProviderX"))
          .when(dependencyInstance)
          .getLandingZone();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenFilteredBySourceContinentConstraint() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isSourceLimitedToContinent();
      doReturn(componentLandingZone("dep-lz", "target-src-name", "ProviderX"))
          .when(dependencyInstance)
          .getLandingZone();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenFilteredBySourceMacroPhaseConstraint() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isSourceLimitedToMacroPhase();
      doReturn(componentLandingZone("other-lz", "target-src-name", "ProviderX"))
          .when(dependencyInstance)
          .getLandingZone();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenTargetSecurityZoneDiffers() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceSecurityZone();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenTargetApplicationPhaseDiffers() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceApplicationPhase();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenTargetLandingZoneDiffers() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceLandingZone();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenTargetContinentDiffers() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceContinent();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenTargetTargetDiffers() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceTarget();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenTargetMacroPhaseDiffers() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceMacroPhase();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void returnsFalseWhenNodeArgumentsAreNull() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");

      // Act / Assert
      assertThat(EdgeFactory.addNode(null, builder)).isFalse();
      assertThat(EdgeFactory.addNode(validComponentInstance("id", "name", "ProviderX"), null))
          .isFalse();
    }

    @Test
    void doesNotAddEdgeWhenFilteredByTargetPaasConstraint() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourcePaas();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void addsEdgeWhenContainerPaasConstraintMatchesDeployInstance() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      Component sourceComponent = source.getComponent();
      ComponentInstance sourceDeploy = source.getDeployComponentInstance();
      doReturn(mock(ZoneType.class)).when(source).getZoneType();
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourcePaas();
      doReturn(true).when(sourceComponent).isContainer();
      doReturn(false).when(sourceComponent).isHosting();
      doReturn(sourceDeploy).when(destination).getDeployComponentInstance();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isOne();
    }

    @Test
    void addsEdgeWhenHostingPaasConstraintMatchesSourceInstance() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      Component sourceComponent = source.getComponent();
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourcePaas();
      doReturn(false).when(sourceComponent).isContainer();
      doReturn(true).when(sourceComponent).isHosting();
      doReturn(source).when(destination).getDeployComponentInstance();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isOne();
    }

    @Test
    void doesNotAddEdgeWhenNamespacesDoNotMatch() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceNamespace();
      Component sourceComponent = source.getComponent();
      Component destinationComponent = destination.getComponent();
      doReturn(true).when(sourceComponent).isContainer();
      doReturn(true).when(destinationComponent).isContainer();
      doReturn("namespace-a").when(source).getNamespaceName();
      doReturn("namespace-b").when(destination).getNamespaceName();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenSourceNamespaceIsBlank() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      Component sourceComponent = source.getComponent();
      Component destinationComponent = destination.getComponent();
      doReturn(true).when(dependencyInstance).isTargetLimitedToSourceNamespace();
      doReturn(true).when(sourceComponent).isContainer();
      doReturn(true).when(destinationComponent).isContainer();
      doReturn(" ").when(source).getNamespaceName();
      doReturn("namespace-a").when(destination).getNamespaceName();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void addsEdgeWhenSelectorsMatchAllConstraints() {
      // Arrange - minimal mocks: only what EdgeFactory.addEdge actually invokes unconditionally
      GraphBuilder builder = new GraphBuilder("v1");

      // isFilteredByProviderLimits always calls getLandingZone().getTarget()
      LandingZone srcLz = mock(LandingZone.class);
      Target srcTarget = mock(Target.class);
      Component srcComponent = mock(Component.class);
      ComponentInstance source = mock(ComponentInstance.class);
      doReturn("src-id").when(source).id();
      doReturn(srcComponent).when(source).getComponent();
      doReturn(srcLz).when(source).getLandingZone();
      doReturn(srcTarget).when(srcLz).getTarget();

      LandingZone dstLz = mock(LandingZone.class);
      Target dstTarget = mock(Target.class);
      Component dstComponent = mock(Component.class);
      ComponentInstance destination = mock(ComponentInstance.class);
      doReturn("dst-id").when(destination).id();
      doReturn(dstComponent).when(destination).getComponent();
      doReturn(dstLz).when(destination).getLandingZone();
      doReturn(dstTarget).when(dstLz).getTarget();

      // dependency + service (minimum for edge creation)
      Service service = mock(Service.class);
      DependencyInstance dependencyInstance = mock(DependencyInstance.class);
      doReturn("dep-id").when(dependencyInstance).id();
      doReturn("service-id").when(service).id();
      doReturn(Set.of(service)).when(dependencyInstance).getServices();

      configureSelectorMocks(dependencyInstance, source, destination);

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isOne();
    }

    @Test
    void doesNotDuplicateExistingEdge() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1", true);
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isOne();
    }

    @Test
    void doesNotAddEdgeWhenServicesAreMissing() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      doReturn(Set.<Service>of()).when(dependencyInstance).getServices();

      // Act
      EdgeFactory.addEdge(
          dependencyInstance.getComponentInstance(),
          dependencyInstance.getTargetComponentInstance(),
          dependencyInstance,
          builder,
          NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void doesNotAddEdgeWhenSelectorsDoNotMatch() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      DependencyInstance dependencyInstance = baseDependencyInstance(false, false);
      ComponentInstance source = dependencyInstance.getComponentInstance();
      ComponentInstance destination = dependencyInstance.getTargetComponentInstance();
      Set<java.util.Map<String, String>> sourceSelectors =
          Set.of(java.util.Map.of("app", "source"));
      Set<java.util.Map<String, String>> destinationSelectors =
          Set.of(java.util.Map.of("app", "dest"));
      doReturn(sourceSelectors).when(dependencyInstance).getSourcesSelectors();
      doReturn(destinationSelectors).when(dependencyInstance).getTargetsSelectors();
      doReturn(false).when(source).areLabelsMatching(sourceSelectors);
      doReturn(false).when(destination).areLabelsMatching(destinationSelectors);

      // Act
      EdgeFactory.addEdge(
          source, destination, dependencyInstance, builder, NetworkComplianceStrategy.NONE);

      // Assert
      assertThat(builder.edgesSize()).isZero();
    }

    @Test
    void rejectsInvalidContainerNodeWithoutDeployment() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      ComponentInstance componentInstance =
          validComponentInstance("node-id", "node-name", "ProviderX");
      Component component = componentInstance.getComponent();
      doReturn(true).when(component).isContainer();
      doReturn(mock(ZoneType.class)).when(componentInstance).getZoneType();
      doReturn("ApplicationZone").when(componentInstance).getZoneTypeName();
      doReturn(null).when(componentInstance).getDeployComponentInstance();

      // Act
      boolean added = EdgeFactory.addNode(componentInstance, builder);

      // Assert
      assertThat(added).isFalse();
      assertThat(builder.nodesSize()).isZero();
    }

    @Test
    void acceptsExistingNodeWithoutReinsertingIt() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      ComponentInstance componentInstance =
          validComponentInstance("node-id", "node-name", "ProviderX");
      builder.putNode(componentInstance.id(), componentInstance);

      // Act
      boolean added = EdgeFactory.addNode(componentInstance, builder);

      // Assert
      assertThat(added).isTrue();
      assertThat(builder.nodesSize()).isOne();
    }

    @Test
    void acceptsTechnicalZoneContainerWithoutDeployment() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      ComponentInstance componentInstance =
          validComponentInstance("tech-id", "tech-node", "ProviderX");
      Component component = componentInstance.getComponent();
      doReturn(true).when(component).isContainer();
      doReturn(mock(ZoneType.class)).when(componentInstance).getZoneType();
      doReturn("TechnicalZone").when(componentInstance).getZoneTypeName();
      doReturn(null).when(componentInstance).getDeployComponentInstance();

      // Act
      boolean added = EdgeFactory.addNode(componentInstance, builder);

      // Assert
      assertThat(added).isTrue();
      assertThat(builder.nodesSize()).isOne();
    }

    @Test
    void rejectsContainerNodeWhenZoneTypeIsNull() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      ComponentInstance componentInstance =
          validComponentInstance("container-id", "container-node", "ProviderX");
      Component component = componentInstance.getComponent();
      doReturn(true).when(component).isContainer();
      doReturn(null).when(componentInstance).getZoneType();

      // Act
      boolean added = EdgeFactory.addNode(componentInstance, builder);

      // Assert
      assertThat(added).isFalse();
      assertThat(builder.nodesSize()).isZero();
    }

    @Test
    void rejectsContainerNodeWhenServiceTagIsMissingAndEndpointsAreEmpty() {
      // Arrange
      GraphBuilder builder = new GraphBuilder("v1");
      ComponentInstance componentInstance =
          validComponentInstance("empty-endpoint-id", "empty-endpoint-node", "ProviderX");
      Component component = componentInstance.getComponent();
      doReturn(true).when(component).isContainer();
      doReturn(mock(ZoneType.class)).when(componentInstance).getZoneType();
      doReturn("ApplicationZone").when(componentInstance).getZoneTypeName();
      doReturn(mock(ComponentInstance.class)).when(componentInstance).getDeployComponentInstance();
      doReturn(null).when(componentInstance).getServiceTag();
      doReturn(Set.of()).when(componentInstance).getInternalEndpoints(Direction.OUT);
      doReturn(Set.of()).when(componentInstance).getInternalEndpoints(Direction.IN);

      // Act
      boolean added = EdgeFactory.addNode(componentInstance, builder);

      // Assert
      assertThat(added).isFalse();
      assertThat(builder.nodesSize()).isZero();
    }
  }

  private static void configureSelectorMocks(
      DependencyInstance dependencyInstance,
      ComponentInstance source,
      ComponentInstance destination) {
    Set<java.util.Map<String, String>> sourceSelectors = Set.of(java.util.Map.of("app", "source"));
    Set<java.util.Map<String, String>> destinationSelectors =
        Set.of(java.util.Map.of("app", "dest"));
    Set<java.util.Map<String, String>> destinationSourceSelectors =
        Set.of(java.util.Map.of("team", "alpha"));
    doReturn(true).when(dependencyInstance).isSourceLimitedToSelector();
    doReturn(sourceSelectors).when(dependencyInstance).getSourcesSelectors();
    doReturn(destinationSelectors).when(dependencyInstance).getTargetsSelectors();
    doReturn(destinationSourceSelectors).when(destination).getSourcesSelectors();
    doReturn(true).when(source).areLabelsMatching(destinationSourceSelectors);
    doReturn(true).when(source).areLabelsMatching(destinationSelectors);
    doReturn(true).when(source).areLabelsMatching(sourceSelectors);
    doReturn(true).when(destination).areLabelsMatching(destinationSelectors);
  }

  private static DependencyInstance baseDependencyInstance(
      boolean sourceAsComponent, boolean targetAsComponent) {
    DependencyInstance dependencyInstance = mock(DependencyInstance.class);
    Dependency dependency = mock(Dependency.class);
    Component sourceComponent = mock(Component.class);
    Component destinationComponent = mock(Component.class);
    ComponentInstance sourceInstance = validComponentInstance("src-id", "src-name", "ProviderX");
    ComponentInstance destinationInstance =
        validComponentInstance("dst-id", "dst-name", "ProviderY");
    ComponentInstance genericSourceA =
        validComponentInstance("generic-src-a", "generic-src-a", "ProviderX");
    ComponentInstance genericSourceB =
        validComponentInstance("generic-src-b", "generic-src-b", "ProviderX");
    ComponentInstance genericDestA =
        validComponentInstance("generic-dst-a", "generic-dst-a", "ProviderY");
    ComponentInstance genericDestB =
        validComponentInstance("generic-dst-b", "generic-dst-b", "ProviderY");
    Service service = mock(Service.class);

    // Batch all mock configurations to minimize method call overhead
    doReturn(sourceAsComponent).when(dependencyInstance).isSourceAsComponent();
    doReturn(targetAsComponent).when(dependencyInstance).isTargetAsComponent();
    doReturn(dependency).when(dependencyInstance).getDependency();
    doReturn("dependency-name").when(dependencyInstance).getName();
    doReturn("dep-id").when(dependencyInstance).id();
    doReturn(sourceInstance).when(dependencyInstance).getComponentInstance();
    doReturn(destinationInstance).when(dependencyInstance).getTargetComponentInstance();
    doReturn(Set.of(service)).when(dependencyInstance).getServices();
    doReturn(false).when(dependencyInstance).isSourceLimitedToSelector();
    doReturn(componentLandingZone("dep-lz", "dep-target", "ProviderX"))
        .when(dependencyInstance)
        .getLandingZone();

    doReturn(sourceComponent).when(dependency).getSourceComponent();
    doReturn(destinationComponent).when(dependency).getDestinationComponent();

    doReturn(false).when(sourceComponent).isTechnical();
    doReturn(false).when(sourceComponent).isContainer();
    doReturn(false).when(sourceComponent).isHosting();
    doReturn("source-component").when(sourceComponent).getName();
    doReturn(sourceAsComponent ? Set.of(genericSourceA, genericSourceB) : Set.of())
        .when(sourceComponent)
        .getAllComponentInstances();

    doReturn(false).when(destinationComponent).isTechnical();
    doReturn(false).when(destinationComponent).isContainer();
    doReturn(false).when(destinationComponent).isHosting();
    doReturn("destination-component").when(destinationComponent).getName();
    doReturn(targetAsComponent ? Set.of(genericDestA, genericDestB) : Set.of())
        .when(destinationComponent)
        .getAllComponentInstances();

    doReturn("service-id").when(service).id();
    doReturn("service-name").when(service).getName();
    doReturn("8080").when(service).getPorts();
    doReturn(V1ServiceSpec.ProtocolEnum.TCP).when(service).getProtocol();

    return dependencyInstance;
  }

  private static ComponentInstance validComponentInstance(
      String id, String name, String providerName) {
    ComponentInstance componentInstance = mock(ComponentInstance.class);
    Component component = mock(Component.class);
    LandingZone landingZone = componentLandingZone("lz-" + name, "target-" + name, providerName);
    ComponentInstance deployComponentInstance = mock(ComponentInstance.class);
    Scope scope = mock(Scope.class);
    ApplicationPhase applicationPhase = mock(ApplicationPhase.class);

    String deployName = "deploy-" + name;
    String scopeName = "scope-" + name;
    String zoneName = "zone-" + name;

    doReturn(id).when(componentInstance).id();
    doReturn(name).when(componentInstance).getName();
    doReturn(component).when(componentInstance).getComponent();
    doReturn(landingZone).when(componentInstance).getLandingZone();
    doReturn(deployComponentInstance).when(componentInstance).getDeployComponentInstance();
    doReturn(scope).when(componentInstance).getScope();
    doReturn(applicationPhase).when(componentInstance).getApplicationPhase();
    doReturn(scopeName).when(scope).getName();
    doReturn("namespace-" + name).when(componentInstance).getNamespaceName();
    doReturn(zoneName).when(componentInstance).getInternalZoneTypeNames(Direction.IN);
    doReturn(zoneName).when(componentInstance).getZoneTypeName();
    doReturn(Set.of(endpoint("443"))).when(componentInstance).getInternalEndpoints(Direction.OUT);
    doReturn(Set.of(endpoint("8443"))).when(componentInstance).getInternalEndpoints(Direction.IN);
    doReturn(mock(ServiceTag.class)).when(componentInstance).getServiceTag();
    doReturn(deployName).when(deployComponentInstance).getName();
    doReturn(false).when(component).isTechnical();
    doReturn(false).when(component).isContainer();
    doReturn(false).when(component).isHosting();
    doReturn("component-" + name).when(component).getName();

    return componentInstance;
  }

  private static LandingZone componentLandingZone(
      String name, String targetName, String providerName) {
    LandingZone landingZone = mock(LandingZone.class);
    Target target = mock(Target.class);
    Region region = mock(Region.class);
    com.amadeus.aud.libraries.models.core.db.Continent continent =
        mock(com.amadeus.aud.libraries.models.core.db.Continent.class);
    com.amadeus.aud.libraries.models.core.db.MacroPhase macroPhase =
        mock(com.amadeus.aud.libraries.models.core.db.MacroPhase.class);

    String continentName = "continent-" + name;
    String macroName = "macro-" + name;

    doReturn(name).when(landingZone).getName();
    doReturn(target).when(landingZone).getTarget();
    doReturn(region).when(landingZone).getRegion();
    doReturn(macroPhase).when(landingZone).getMacroPhase();
    doReturn(targetName).when(target).getName();
    doReturn(providerName).when(target).getProviderName();
    doReturn(continent).when(region).getContinent();
    doReturn(continentName).when(continent).getName();
    doReturn(macroName).when(macroPhase).getName();
    return landingZone;
  }

  private static Endpoint endpoint(String value) {
    Endpoint endpoint = mock(Endpoint.class);
    doReturn(value).when(endpoint).getValue();
    return endpoint;
  }
}

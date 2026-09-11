package com.amadeus.aud.services.apis.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.ReferenceEntry;
import com.amadeus.aud.libraries.backend.models.v2.ReferencesResponse;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ApplicationPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependenciesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependencyInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopeReleaseNotesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ServicesRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.amadeus.aud.services.apis.services.endpoints.v2.ReferencesService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReferencesServiceTest {

  private final Version version = new Version();
  private final ReferencesService referencesService =
      new ReferencesService(new TestAudService(version));
  private final V1Component commonData = componentData(false, false);

  @Nested
  class GetReferences {

    @Test
    void unknownObjectTypeThrowsBadRequestException() {
      assertThatThrownBy(
              () ->
                  referencesService
                      .getReferences("latest", "unknown-type", "id")
                      .await()
                      .indefinitely())
          .isInstanceOf(BadRequestException.class)
          .hasMessage(
              """
              Unknown object type: unknown-type. Known types: applicationid
              applicationphase
              automatictemplateinstance
              blacklist
              blacklistedcidr
              blacklistedurl
              blockedport
              certification
              component
              componentinstance
              componentplacement
              connection
              connectionapproval
              connectionapprovalcomponentskip
              connectionapprovalscopeskip
              connectionphase
              continent
              dependency
              dependencyinstance
              firewallgroup
              firewallvendor
              ipaddressinstance
              landingzone
              landingzonefunctionaltype
              landingzonetype
              macrophase
              namespace
              outboundsnat
              provider
              region
              role
              scope
              scopereleasenote
              securityarea
              securityareamapping
              service
              serviceapproval
              servicetag
              subnetinstance
              subnettype
              target
              targettype
              zoneinstance
              zonetype""");
    }
  }

  @Nested
  class ComputeReferences {

    @Test
    void entityNotFoundThrowsNotFoundException() {
      version.addRepository(new ComponentsRepository());

      assertThatThrownBy(
              () ->
                  referencesService.computeReferences(
                      version, ObjectTypeListNamesEnum.COMPONENTS, "non-existent"))
          .isInstanceOf(NotFoundException.class);
    }

    @Test
    void returnsDirectReferences() {
      String targetId = "comp-target";
      Component target = component(targetId, "Target Component", Set.of());
      ComponentsRepository components = new ComponentsRepository();
      components.put(targetId, target);
      version.addRepository(components);

      Service referringService = new Service();
      referringService.setName("Referring Service");
      referringService.setComponent(target);
      ServicesRepository services = new ServicesRepository();
      services.put(referringService.id(), referringService);
      version.addRepository(services);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTS, targetId);

      assertThat(response.direct()).containsKey(ObjectTypeListNamesEnum.SERVICES.getUri());
      List<ReferenceEntry> entries =
          response.direct().get(ObjectTypeListNamesEnum.SERVICES.getUri());
      assertThat(entries).hasSize(1);
      assertThat(entries.get(0).id()).isEqualTo("Target Component***Referring Service");
    }

    @Test
    void inheritedServicesForComponent() {
      String childId = "comp-child";
      Component parent = component("comp-parent", "Parent Component", Set.of());
      Component child = component(childId, "Child Component", Set.of(parent));
      ComponentsRepository components = new ComponentsRepository();
      components.put(parent.id(), parent);
      components.put(child.id(), child);
      version.addRepository(components);

      Service inheritedService = new Service();
      inheritedService.setName("Inherited Service");
      inheritedService.setComponent(parent);
      ServicesRepository services = new ServicesRepository();
      services.put(inheritedService.id(), inheritedService);
      version.addRepository(services);

      ReferencesResponse response =
          referencesService.computeReferences(version, ObjectTypeListNamesEnum.COMPONENTS, childId);

      assertThat(response.inherited()).containsKey(ObjectTypeListNamesEnum.SERVICES.getUri());
      List<ReferenceEntry> inheritedEntries =
          response.inherited().get(ObjectTypeListNamesEnum.SERVICES.getUri());
      assertThat(inheritedEntries).hasSize(1);
      assertThat(inheritedEntries.get(0).fromParent()).isEqualTo("Parent Component");
      assertThat(inheritedEntries.get(0).id()).isEqualTo("Parent Component***Inherited Service");
    }

    @Test
    void inheritedDependencyInstancesForComponentInstance() {
      String ciId = "ci-target";
      Component parentComponent = component("parent", "Ancestor Component", Set.of());
      Component childComponent = component("child", "Child Component", Set.of(parentComponent));
      ComponentInstance componentInstance = componentInstance(ciId, childComponent);
      ComponentInstancesRepository componentInstances = new ComponentInstancesRepository();
      componentInstances.put(componentInstance.id(), componentInstance);
      version.addRepository(componentInstances);

      Dependency dependency = dependency("dependency", parentComponent, null);
      DependencyInstance dependencyInstance =
          dependencyInstance("di-inherited", dependency, true, false);
      DependencyInstancesRepository dependencyInstances = new DependencyInstancesRepository();
      dependencyInstances.put(dependencyInstance.id(), dependencyInstance);
      version.addRepository(dependencyInstances);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTINSTANCES, ciId);

      assertThat(response.inherited())
          .containsKey(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri());
      assertThat(response.inherited().get(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri()))
          .hasSize(1);
    }

    @Test
    void returnsCombinedDirectAndInheritedReferences() {
      String targetId = "target-comp";
      Component parent = component("parent-id", "Parent Component", Set.of());
      Component target = component(targetId, "Target Component", Set.of(parent));
      Component referringComponent =
          component("direct-ref-id", "Direct Referring Comp", Set.of(target));
      ComponentsRepository components = new ComponentsRepository();
      components.put(target.id(), target);
      components.put(parent.id(), parent);
      components.put(referringComponent.id(), referringComponent);
      version.addRepository(components);

      Service inheritedService = new Service();
      inheritedService.setName("Service From Parent");
      inheritedService.setComponent(parent);
      ServicesRepository services = new ServicesRepository();
      services.put(inheritedService.id(), inheritedService);
      version.addRepository(services);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTS, targetId);

      assertThat(response.direct()).containsKey(ObjectTypeListNamesEnum.COMPONENTS.getUri());
      assertThat(response.direct().get(ObjectTypeListNamesEnum.COMPONENTS.getUri()).get(0).id())
          .isEqualTo("direct-ref-id");

      assertThat(response.inherited()).containsKey(ObjectTypeListNamesEnum.SERVICES.getUri());
      assertThat(response.inherited().get(ObjectTypeListNamesEnum.SERVICES.getUri()).get(0).id())
          .isEqualTo("Parent Component***Service From Parent");
      assertThat(
              response
                  .inherited()
                  .get(ObjectTypeListNamesEnum.SERVICES.getUri())
                  .get(0)
                  .fromParent())
          .isEqualTo("Parent Component");
    }

    @Test
    void scopeIsIgnoredWhenItIsDeclaringScope() {
      String targetId = "comp-target";
      Scope declaringScope = scope("declaring-scope", "Declaring Scope");
      Component target = component(targetId, "Target Component", Set.of());
      target.setScope(declaringScope);
      declaringScope.setComponents(Set.of(target));
      ComponentsRepository components = new ComponentsRepository();
      components.put(target.id(), target);
      version.addRepository(components);
      ScopesRepository scopes = new ScopesRepository();
      scopes.put(declaringScope.id(), declaringScope);
      version.addRepository(scopes);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTS, targetId);

      assertThat(response.direct()).doesNotContainKey(ObjectTypeListNamesEnum.SCOPES.getUri());
    }

    @Test
    void scopeIsIncludedWhenItIsExternalReference() {
      String targetId = "target-entity";
      Scope declaringScope = scope("declaring-scope", "Declaring Scope");
      DummyEntity target = new DummyEntity();
      target.setUniqueIdFrom(targetId);
      target.setName("Target Entity");
      target.setData(new V1Scope());
      target.setScope(declaringScope);
      SecurityAreasRepository<DummyEntity> securityAreas = new SecurityAreasRepository<>();
      securityAreas.put(target.id(), target);
      version.addRepository(securityAreas);
      TestReferringScope referringScope = new TestReferringScope();
      referringScope.setUniqueIdFrom("referring-scope");
      referringScope.setName("Referring Scope");
      referringScope.targetRef = target;
      referringScope.setData(new V1Scope());
      ScopesRepository scopes = new ScopesRepository();
      scopes.put(declaringScope.id(), declaringScope);
      scopes.put(referringScope.id(), referringScope);
      version.addRepository(scopes);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.SECURITYAREAS, targetId);

      assertThat(response.direct()).containsKey(ObjectTypeListNamesEnum.SCOPES.getUri());
      List<ReferenceEntry> entries = response.direct().get(ObjectTypeListNamesEnum.SCOPES.getUri());
      assertThat(entries)
          .singleElement()
          .extracting(ReferenceEntry::id)
          .isEqualTo("referring-scope");
    }

    @Test
    void returnsEmptyResponseForNonAbstractTargetAndMissingReferenceRepositories() {
      BomElement target = () -> "plain-id";
      SecurityAreasRepository<BomElement> securityAreas = new SecurityAreasRepository<>();
      securityAreas.put("plain-key", target);
      version.addRepository(securityAreas);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.SECURITYAREAS, "plain-id");

      assertThat(response.direct()).isEmpty();
      assertThat(response.inherited()).isEmpty();
    }

    @Test
    void missingTargetRepositoryAndDataThrowNotFoundException() {
      Version versionWithoutRepository = new Version();
      Version versionWithoutData = new Version();
      ComponentsRepository repositoryWithoutData = new ComponentsRepository();
      repositoryWithoutData.setData(null);
      versionWithoutData.addRepository(repositoryWithoutData);

      assertThatThrownBy(
              () ->
                  referencesService.computeReferences(
                      versionWithoutRepository, ObjectTypeListNamesEnum.COMPONENTS, "missing"))
          .isInstanceOf(NotFoundException.class);
      assertThatThrownBy(
              () ->
                  referencesService.computeReferences(
                      versionWithoutData, ObjectTypeListNamesEnum.COMPONENTS, "missing"))
          .isInstanceOf(NotFoundException.class);
    }

    @Test
    void componentInheritanceCoversDependencyEndsAndSuppressesDirectDuplicates() {
      Component sourceParent = component("source-parent", "Source Parent", Set.of());
      Component destinationParent = component("destination-parent", "Destination Parent", Set.of());
      Component target = component("target", "Target", Set.of(sourceParent, destinationParent));
      ComponentsRepository components = new ComponentsRepository();
      components.put(target.id(), target);
      version.addRepository(components);

      Dependency sourceDependency = dependency("source-dependency", sourceParent, null);
      Dependency destinationDependency =
          dependency("destination-dependency", null, destinationParent);
      Dependency bothDependency = dependency("both-dependency", sourceParent, destinationParent);
      Dependency duplicate = dependency("duplicate", sourceParent, target);
      DependenciesRepository dependencies = new DependenciesRepository();
      dependencies.put(sourceDependency.id(), sourceDependency);
      dependencies.put(destinationDependency.id(), destinationDependency);
      dependencies.put(bothDependency.id(), bothDependency);
      dependencies.put(duplicate.id(), duplicate);
      version.addRepository(dependencies);

      Service serviceWithoutComponent = new Service();
      serviceWithoutComponent.setName("Service Without Component");
      ServicesRepository services = new ServicesRepository();
      services.put(serviceWithoutComponent.id(), serviceWithoutComponent);
      version.addRepository(services);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTS, target.id());

      assertThat(response.direct().get(ObjectTypeListNamesEnum.DEPENDENCIES.getUri()))
          .extracting(ReferenceEntry::id)
          .containsExactly("duplicate");
      assertThat(response.inherited().get(ObjectTypeListNamesEnum.DEPENDENCIES.getUri()))
          .extracting(ReferenceEntry::id)
          .containsExactlyInAnyOrder(
              "source-dependency", "destination-dependency", "both-dependency");
      assertThat(response.inherited().get(ObjectTypeListNamesEnum.DEPENDENCIES.getUri()))
          .filteredOn(entry -> "both-dependency".equals(entry.id()))
          .singleElement()
          .extracting(ReferenceEntry::fromParent)
          .isEqualTo("Destination Parent,Source Parent");
    }

    @Test
    void componentInstanceInheritanceHandlesDestinationNullDependencyAndDuplicate() {
      Component parent = component("parent", "Parent", Set.of());
      Component child = component("child", "Child", Set.of(parent));
      ComponentInstance targetInstance = componentInstance("instance", child);
      ComponentInstancesRepository instances = new ComponentInstancesRepository();
      instances.put(targetInstance.id(), targetInstance);
      version.addRepository(instances);

      Dependency destinationDependency = dependency("destination", null, parent);
      DependencyInstance destinationInstance =
          dependencyInstance("destination-instance", destinationDependency, false, true);
      DependencyInstance nullDependency = dependencyInstance("null-dependency", null, true, true);
      DependencyInstance duplicate =
          dependencyInstance("duplicate-instance", destinationDependency, false, true);
      duplicate.setComponentInstance(targetInstance);
      DependencyInstancesRepository dependencyInstances = new DependencyInstancesRepository();
      dependencyInstances.put(destinationInstance.id(), destinationInstance);
      dependencyInstances.put(nullDependency.id(), nullDependency);
      dependencyInstances.put(duplicate.id(), duplicate);
      version.addRepository(dependencyInstances);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTINSTANCES, targetInstance.id());

      assertThat(response.inherited().get(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri()))
          .singleElement()
          .satisfies(
              (ReferenceEntry entry) -> {
                assertThat(entry.id()).isEqualTo("destination-instance");
                assertThat(entry.fromParent()).isEqualTo("Parent");
              });
      assertThat(response.direct().get(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri()))
          .extracting(ReferenceEntry::id)
          .containsExactly("duplicate-instance");
    }

    @Test
    void directReferenceScanSkipsReleaseNotesAndNonAbstractElements() {
      Component target = component("target", "Target", Set.of());
      ComponentsRepository components = new ComponentsRepository();
      components.put(target.id(), target);
      version.addRepository(components);

      ScopeReleaseNote releaseNote = new ScopeReleaseNote();
      releaseNote.setUniqueIdFrom("note");
      ScopeReleaseNotesRepository releaseNotes = new ScopeReleaseNotesRepository();
      releaseNotes.put(releaseNote.id(), releaseNote);
      version.addRepository(releaseNotes);
      BomElement plainElement = () -> "plain";
      SecurityAreasRepository<BomElement> securityAreas = new SecurityAreasRepository<>();
      securityAreas.put(plainElement.id(), plainElement);
      version.addRepository(securityAreas);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTS, target.id());

      assertThat(response.direct()).isEmpty();
      assertThat(response.inherited()).isEmpty();
    }

    @Test
    void componentReferencesCoverInterfaceFlagsAndNonMatchingAncestors() {
      Component sharedAncestor = component("shared", "Shared", Set.of());
      Component sourceAncestor = component("source", null, Set.of(sharedAncestor));
      Component destinationAncestor = component("destination", null, Set.of(sharedAncestor));
      Component unrelated = component("unrelated", "Unrelated", Set.of());
      Component target =
          component(
              "target",
              "Target",
              new LinkedHashSet<>(List.of(sourceAncestor, destinationAncestor)));
      Component publicReference =
          componentWithFlags(
              "public-reference",
              "Public Reference",
              new LinkedHashSet<>(List.of(unrelated, target)),
              true,
              false);
      Component interfaceReference =
          componentWithFlags(
              "interface-reference", "Interface Reference", Set.of(target), true, true);
      ComponentsRepository components = new ComponentsRepository();
      components.put(target.id(), target);
      components.put(publicReference.id(), publicReference);
      components.put(interfaceReference.id(), interfaceReference);
      version.addRepository(components);

      Scope unrelatedScope = scope("unrelated-scope", "Unrelated Scope");
      ScopesRepository scopes = new ScopesRepository();
      scopes.put(unrelatedScope.id(), unrelatedScope);
      version.addRepository(scopes);

      Dependency sourceDependency = dependency("source-dependency", sourceAncestor, null);
      Dependency destinationDependency =
          dependency("destination-dependency", null, destinationAncestor);
      Dependency unrelatedDependency = dependency("unrelated-dependency", unrelated, unrelated);
      DependenciesRepository dependencies = new DependenciesRepository();
      dependencies.put(sourceDependency.id(), sourceDependency);
      dependencies.put(destinationDependency.id(), destinationDependency);
      dependencies.put(unrelatedDependency.id(), unrelatedDependency);
      version.addRepository(dependencies);

      Service unrelatedService = new Service();
      unrelatedService.setName("Unrelated Service");
      unrelatedService.setComponent(unrelated);
      ServicesRepository services = new ServicesRepository();
      services.put(unrelatedService.id(), unrelatedService);
      version.addRepository(services);
      ApplicationPhasesRepository applicationPhases = new ApplicationPhasesRepository();
      applicationPhases.setData(null);
      version.addRepository(applicationPhases);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTS, target.id());

      assertThat(response.direct().get(ObjectTypeListNamesEnum.COMPONENTS.getUri()))
          .extracting(ReferenceEntry::id, ReferenceEntry::isInterface)
          .containsExactlyInAnyOrder(
              org.assertj.core.groups.Tuple.tuple("public-reference", null),
              org.assertj.core.groups.Tuple.tuple("interface-reference", true));
      assertThat(response.inherited().get(ObjectTypeListNamesEnum.DEPENDENCIES.getUri()))
          .extracting(ReferenceEntry::id, ReferenceEntry::fromParent)
          .containsExactlyInAnyOrder(
              org.assertj.core.groups.Tuple.tuple("source-dependency", null),
              org.assertj.core.groups.Tuple.tuple("destination-dependency", null));
      assertThat(response.inherited()).doesNotContainKey(ObjectTypeListNamesEnum.SERVICES.getUri());
    }

    @Test
    void componentInstanceReferencesRejectUnrelatedEndsAndHandleUnnamedParent() {
      Component parent = component("parent", null, Set.of());
      Component child = component("child", "Child", Set.of(parent));
      Component unrelated = component("unrelated", "Unrelated", Set.of());
      ComponentInstance target = componentInstance("target-instance", child);
      ComponentInstancesRepository componentInstances = new ComponentInstancesRepository();
      componentInstances.put(target.id(), target);
      version.addRepository(componentInstances);

      Dependency matchingDependency = dependency("matching-dependency", parent, parent);
      Dependency unrelatedDependency = dependency("unrelated-dependency", unrelated, unrelated);
      DependencyInstance matchingInstance =
          dependencyInstance("matching-instance", matchingDependency, true, true);
      DependencyInstance unrelatedInstance =
          dependencyInstance("unrelated-instance", unrelatedDependency, true, true);
      DependencyInstancesRepository dependencyInstances = new DependencyInstancesRepository();
      dependencyInstances.put(matchingInstance.id(), matchingInstance);
      dependencyInstances.put(unrelatedInstance.id(), unrelatedInstance);
      version.addRepository(dependencyInstances);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTINSTANCES, target.id());

      assertThat(response.inherited().get(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri()))
          .singleElement()
          .satisfies(
              (ReferenceEntry entry) -> {
                assertThat(entry.id()).isEqualTo("matching-instance");
                assertThat(entry.fromParent()).isNull();
              });
    }

    @Test
    void componentInstanceWithoutComponentHasNoInheritedReferences() {
      ComponentInstance target = componentInstance("target-instance", null);
      ComponentInstancesRepository componentInstances = new ComponentInstancesRepository();
      componentInstances.put(target.id(), target);
      version.addRepository(componentInstances);

      ReferencesResponse response =
          referencesService.computeReferences(
              version, ObjectTypeListNamesEnum.COMPONENTINSTANCES, target.id());

      assertThat(response.direct()).isEmpty();
      assertThat(response.inherited()).isEmpty();
    }
  }

  private static final class DummyEntity extends AbstractBase<V1Scope> {}

  private static final class TestReferringScope extends Scope {
    private DummyEntity targetRef;
  }

  private Component component(String id, String name, Set<Component> parents) {
    Component component = new Component();
    component.setUniqueIdFrom(id);
    component.setName(name);
    component.setParents(parents);
    component.setData(commonData);
    return component;
  }

  private Component componentWithFlags(
      String id,
      String name,
      Set<Component> parents,
      boolean publicInheritance,
      boolean technical) {
    Component component = component(id, name, parents);
    component.setData(componentData(publicInheritance, technical));
    return component;
  }

  private ComponentInstance componentInstance(String id, Component component) {
    V1ComponentInstanceSpec spec = new V1ComponentInstanceSpec();
    V1ComponentInstance data = new V1ComponentInstance();
    data.setSpec(spec);
    ComponentInstance componentInstance = new ComponentInstance();
    componentInstance.setUniqueIdFrom(id);
    componentInstance.setName(id);
    componentInstance.setData(data);
    componentInstance.setComponent(component);
    return componentInstance;
  }

  private static Scope scope(String id, String name) {
    Scope scope = new Scope();
    scope.setUniqueIdFrom(id);
    scope.setName(name);
    scope.setData(new V1Scope());
    return scope;
  }

  private static V1Component componentData(boolean publicInheritance, boolean technical) {
    V1ComponentSpec spec = new V1ComponentSpec();
    spec.setIsPublicInheritance(publicInheritance);
    spec.setIsTechnical(technical);
    V1Component data = new V1Component();
    data.setSpec(spec);
    return data;
  }

  private Dependency dependency(String id, Component source, Component destination) {
    Dependency dependency = new Dependency();
    dependency.setUniqueIdFrom(id);
    dependency.setName(id);
    dependency.setSourceComponent(source);
    dependency.setDestinationComponent(destination);
    dependency.setData(new V1Dependency());
    return dependency;
  }

  private DependencyInstance dependencyInstance(
      String id, Dependency dependency, boolean sourceAsComponent, boolean targetAsComponent) {
    DependencyInstance instance = new DependencyInstance();
    instance.setUniqueIdFrom(id);
    instance.setName(id);
    instance.setDependency(dependency);
    instance.setSourceAsComponent(sourceAsComponent);
    V1DependencyInstanceSpec spec = new V1DependencyInstanceSpec();
    spec.setIsSourceAsComponent(sourceAsComponent);
    spec.setIsTargetAsComponent(targetAsComponent);
    V1DependencyInstance data = new V1DependencyInstance();
    data.setSpec(spec);
    instance.setData(data);
    return instance;
  }

  private static final class TestAudService extends AudService {
    private final Version version;

    private TestAudService(Version version) {
      super(null, null, null);
      this.version = version;
    }

    @Override
    public Uni<Version> getVersion(String versionId) {
      return Uni.createFrom().item(version);
    }
  }
}

final class SecurityAreasRepository<T extends BomElement> extends ConcurrentRepository<T> {}

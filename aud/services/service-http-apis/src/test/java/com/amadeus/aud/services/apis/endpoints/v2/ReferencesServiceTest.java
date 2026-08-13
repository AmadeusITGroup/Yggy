package com.amadeus.aud.services.apis.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.ReferenceEntry;
import com.amadeus.aud.libraries.backend.models.v2.ReferencesResponse;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstanceSpec;
import com.amadeus.aud.services.apis.services.endpoints.v2.ReferencesService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReferencesServiceTest {

  @Mock private AudService audService;
  @Mock private Version version;

  @InjectMocks private ReferencesService referencesService;

  private V1Component commonData;

  @BeforeEach
  void setUp() {
    V1ComponentSpec spec = new V1ComponentSpec();
    spec.setIsPublicInheritance(false);
    spec.setIsTechnical(false);
    commonData = new V1Component();
    commonData.setSpec(spec);
  }

  @Nested
  class GetReferences {

    @Test
    void unknownObjectTypeThrowsNotFoundException() {
      when(audService.getVersion(anyString())).thenReturn(Uni.createFrom().item(version));
      assertThatThrownBy(
              () ->
                  referencesService
                      .getReferences("latest", "unknown-type", "id")
                      .await()
                      .indefinitely())
          .isInstanceOf(NotFoundException.class);
    }
  }

  @Nested
  class ComputeReferences {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void entityNotFoundThrowsNotFoundException() {
      String objectType = ObjectTypeListNamesEnum.COMPONENTS.getUri();
      ConcurrentRepository repo = mock(ConcurrentRepository.class);
      when(version.getRepository(ObjectTypeListNamesEnum.COMPONENTS.getRepositoryKey()))
          .thenReturn(repo);
      when(repo.getData()).thenReturn(new ConcurrentHashMap<>());

      assertThatThrownBy(
              () -> referencesService.computeReferences(version, objectType, "non-existent"))
          .isInstanceOf(NotFoundException.class);
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void returnsDirectReferences() throws Exception {
      String objectType = ObjectTypeListNamesEnum.COMPONENTS.getUri();
      String targetId = "comp-target";

      Component target = new Component();
      setPrivateField(target, "uniqueId", targetId);
      setPrivateField(target, "name", "Target Component");
      setPrivateField(target, "data", commonData);

      Map<ObjectTypeListNamesEnum, ConcurrentRepository<? extends BomElement>> repoMap =
          stubAllRepositories(version);

      ConcurrentMap<String, Component> componentsData = new ConcurrentHashMap<>();
      componentsData.put(targetId, target);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.COMPONENTS).getData())
          .thenReturn(componentsData);

      Service referringService = new Service();
      referringService.setName("Referring Service");
      referringService.setComponent(target);
      setPrivateField(referringService, "data", commonData);

      ConcurrentMap<String, Service> servicesData = new ConcurrentHashMap<>();
      servicesData.put(referringService.id(), referringService);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.SERVICES).getData())
          .thenReturn(servicesData);

      ReferencesResponse response =
          referencesService.computeReferences(version, objectType, targetId);

      assertThat(response.direct()).containsKey(ObjectTypeListNamesEnum.SERVICES.getUri());
      List<ReferenceEntry> entries =
          response.direct().get(ObjectTypeListNamesEnum.SERVICES.getUri());
      assertThat(entries).hasSize(1);
      assertThat(entries.get(0).id()).isEqualTo("Target Component***Referring Service");
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void inheritedServicesForComponent() throws Exception {
      String objectType = ObjectTypeListNamesEnum.COMPONENTS.getUri();
      String childId = "comp-child";

      Component parent = new Component();
      setPrivateField(parent, "uniqueId", "comp-parent");
      setPrivateField(parent, "name", "Parent Component");
      setPrivateField(parent, "data", commonData);

      Component child = new Component();
      setPrivateField(child, "uniqueId", childId);
      setPrivateField(child, "name", "Child Component");
      setPrivateField(child, "parents", Set.of(parent));
      setPrivateField(child, "data", commonData);

      Map<ObjectTypeListNamesEnum, ConcurrentRepository<? extends BomElement>> repoMap =
          stubAllRepositories(version);

      ConcurrentMap<String, Component> componentsData = new ConcurrentHashMap<>();
      componentsData.put("comp-parent", parent);
      componentsData.put(childId, child);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.COMPONENTS).getData())
          .thenReturn(componentsData);

      Service inheritedService = new Service();
      inheritedService.setName("Inherited Service");
      inheritedService.setComponent(parent);
      setPrivateField(inheritedService, "data", commonData);

      ConcurrentMap<String, Service> servicesData = new ConcurrentHashMap<>();
      servicesData.put(inheritedService.id(), inheritedService);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.SERVICES).getData())
          .thenReturn(servicesData);

      ReferencesResponse response =
          referencesService.computeReferences(version, objectType, childId);

      assertThat(response.inherited()).containsKey(ObjectTypeListNamesEnum.SERVICES.getUri());
      List<ReferenceEntry> inheritedEntries =
          response.inherited().get(ObjectTypeListNamesEnum.SERVICES.getUri());
      assertThat(inheritedEntries).hasSize(1);
      assertThat(inheritedEntries.get(0).fromParent()).isEqualTo("Parent Component");
      assertThat(inheritedEntries.get(0).id()).isEqualTo("Parent Component***Inherited Service");
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void inheritedDependencyInstancesForComponentInstance() throws Exception {
      String objectType = ObjectTypeListNamesEnum.COMPONENTINSTANCES.getUri();
      String ciId = "ci-target";

      Component parentComp = new Component();
      setPrivateField(parentComp, "name", "Ancestor Component");
      setPrivateField(parentComp, "data", commonData);

      Component childComp = new Component();
      setPrivateField(childComp, "parents", Set.of(parentComp));
      setPrivateField(childComp, "data", commonData);

      ComponentInstance ci = new ComponentInstance();
      setPrivateField(ci, "uniqueId", ciId);
      setPrivateField(ci, "component", childComp);
      setPrivateField(ci, "data", commonData);

      Map<ObjectTypeListNamesEnum, ConcurrentRepository<? extends BomElement>> repoMap =
          stubAllRepositories(version);

      ConcurrentMap<String, ComponentInstance> instanceData = new ConcurrentHashMap<>();
      instanceData.put(ciId, ci);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.COMPONENTINSTANCES).getData())
          .thenReturn(instanceData);

      Dependency dep = new Dependency();
      setPrivateField(dep, "name", "The Dependency");
      setPrivateField(dep, "sourceComponent", parentComp);
      setPrivateField(dep, "data", new com.amadeus.aud.libraries.yaml.models.V1Dependency());

      V1DependencyInstanceSpec diSpec = new V1DependencyInstanceSpec();
      diSpec.setIsSourceAsComponent(true);
      diSpec.setIsTargetAsComponent(false);
      V1DependencyInstance diYamlData = new V1DependencyInstance();
      diYamlData.setSpec(diSpec);

      DependencyInstance di = new DependencyInstance();
      setPrivateField(di, "uniqueId", "di-inherited");
      setPrivateField(di, "data", diYamlData);
      setPrivateField(di, "dependency", dep);
      setPrivateField(di, "isSourceAsComponent", true);

      ConcurrentMap<String, DependencyInstance> diData = new ConcurrentHashMap<>();
      diData.put("di-inherited", di);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES).getData())
          .thenReturn(diData);

      ReferencesResponse response = referencesService.computeReferences(version, objectType, ciId);

      assertThat(response.inherited())
          .containsKey(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri());
      assertThat(response.inherited().get(ObjectTypeListNamesEnum.DEPENDENCYINSTANCES.getUri()))
          .hasSize(1);
    }

    @Test
    @SuppressWarnings({"rawtypes"})
    void returnsCombinedDirectAndInheritedReferences() throws Exception {
      String objectType = ObjectTypeListNamesEnum.COMPONENTS.getUri();
      String targetId = "target-comp";

      Component parent = new Component();
      setPrivateField(parent, "name", "Parent Component");

      Component target = new Component();
      setPrivateField(target, "uniqueId", targetId);
      setPrivateField(target, "name", "Target Component");
      setPrivateField(target, "parents", Set.of(parent));

      Map<ObjectTypeListNamesEnum, ConcurrentRepository<? extends BomElement>> repoMap =
          stubAllRepositories(version);

      ConcurrentMap<String, Component> compData = new ConcurrentHashMap<>();
      compData.put(targetId, target);
      compData.put("parent-id", parent);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.COMPONENTS).getData())
          .thenReturn(compData);

      setPrivateField(target, "data", commonData);
      setPrivateField(parent, "data", commonData);

      Component referringComp = new Component();
      setPrivateField(referringComp, "uniqueId", "direct-ref-id");
      setPrivateField(referringComp, "name", "Direct Referring Comp");
      setPrivateField(referringComp, "parents", Set.of(target));
      setPrivateField(referringComp, "data", commonData);

      compData.put("direct-ref-id", referringComp);

      Service inheritedService = new Service();
      inheritedService.setName("Service From Parent");
      inheritedService.setComponent(parent);

      ConcurrentMap<String, Service> svcData = new ConcurrentHashMap<>();
      svcData.put(inheritedService.id(), inheritedService);
      when((ConcurrentMap) repoMap.get(ObjectTypeListNamesEnum.SERVICES).getData())
          .thenReturn(svcData);

      ReferencesResponse response =
          referencesService.computeReferences(version, objectType, targetId);

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
  }

  // --- Helpers ---

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<ObjectTypeListNamesEnum, ConcurrentRepository<? extends BomElement>>
      stubAllRepositories(Version version) {
    Map<ObjectTypeListNamesEnum, ConcurrentRepository<? extends BomElement>> repoMap =
        new TreeMap<>();
    for (ObjectTypeListNamesEnum type : ObjectTypeListNamesEnum.values()) {
      ConcurrentRepository repo = mock(ConcurrentRepository.class);
      when(version.getRepository(type.getRepositoryKey())).thenReturn(repo);
      when(repo.getData()).thenReturn(new ConcurrentHashMap<>());
      repoMap.put(type, repo);
    }
    return repoMap;
  }

  private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
    Field field = findField(object.getClass(), fieldName);
    field.setAccessible(true);
    field.set(object, value);
  }

  private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    Class<?> current = clazz;
    while (current != null) {
      try {
        return current.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }
}

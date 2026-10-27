package com.amadeus.aud.libraries.v2.objects.service;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.ReferenceEntry;
import com.amadeus.aud.libraries.backend.models.v2.ReferencesResponse;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.producers.VersionCacheKeyGenerator;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.DependencyInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.models.core.db.Service;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@ApplicationScoped
public class ReferencesService {

  private static final Map<Class<?>, List<Field>> FIELDS_CACHE = new ConcurrentHashMap<>();
  private static final Comparator<ReferenceEntry> REFERENCE_NAME_ORDER =
      Comparator.comparing(
              ReferenceEntry::name, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
          .thenComparing(ReferenceEntry::name, Comparator.nullsLast(String::compareTo));

  @Inject
  public ReferencesService() {}

  public Uni<ReferencesResponse> getReferences(
      Version version, String objectType, String entityId) {
    ObjectTypeListNamesEnum objectTypeEnum =
        ObjectTypeListNamesEnum.byUri(objectType)
            .orElseThrow(
                () ->
                    new BadRequestException(
                        "Unknown object type: "
                            + objectType
                            + ". Known types: "
                            + ObjectTypeListNamesEnum.knownRepositoryKeys()));

    return Uni.createFrom()
        .item(() -> computeReferences(version, objectTypeEnum, entityId))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }

  @CacheResult(cacheName = "getReferencesResponse", keyGenerator = VersionCacheKeyGenerator.class)
  public ReferencesResponse computeReferences(
      Version version, ObjectTypeListNamesEnum objectTypeEnum, String entityId) {
    BomElement targetEntity = findTargetEntityById(version, objectTypeEnum, entityId);
    if (targetEntity == null) {
      throw new NotFoundException("Entity not found: " + entityId);
    }
    if (!(targetEntity instanceof AbstractBase<?> targetBase)) {
      return emptyResponse();
    }

    Map<String, List<ReferenceEntry>> directReferences =
        findDirectReferences(version, targetBase, targetEntity.getClass(), targetBase.id());

    Map<String, List<ReferenceEntry>> inheritedReferences =
        targetEntity instanceof Component component
            ? findInheritedComponentReferences(version, component, directReferences)
            : targetEntity instanceof ComponentInstance componentInstance
                ? findInheritedDependencyInstances(version, componentInstance, directReferences)
                : Map.of();

    return new ReferencesResponse(directReferences, inheritedReferences);
  }

  // ========================= Direct references =========================

  private static Map<String, List<ReferenceEntry>> findDirectReferences(
      Version version, AbstractBase<?> targetBase, Class<?> targetClass, String id) {
    Map<String, List<ReferenceEntry>> directReferencesByType = new TreeMap<>();
    for (ObjectTypeListNamesEnum objectType : ObjectTypeListNamesEnum.values()) {
      ConcurrentRepository<? extends BomElement> repository = getRepository(version, objectType);
      if (repository == null || repository.getData() == null) {
        continue;
      }
      for (BomElement element : repository.getData().values()) {
        appendDirectReferenceIfMatch(
            element, targetBase, targetClass, id, objectType, directReferencesByType);
      }
    }
    directReferencesByType.values().forEach(list -> list.sort(REFERENCE_NAME_ORDER));
    return directReferencesByType;
  }

  private static void appendDirectReferenceIfMatch(
      BomElement element,
      AbstractBase<?> targetBase,
      Class<?> targetClass,
      String id,
      ObjectTypeListNamesEnum objectType,
      Map<String, List<ReferenceEntry>> directReferencesByType) {
    if (element instanceof ScopeReleaseNote) {
      return;
    }
    if (element instanceof Scope scope
        && targetBase.getScope() != null
        && scope.id().equals(targetBase.getScope().id())) {
      return;
    }
    if (!(element instanceof AbstractBase<?> base)) {
      return;
    }
    if (base == targetBase) {
      return;
    }
    if (!hasDirectReference(element, targetClass, id)) {
      return;
    }
    Boolean isInterface = resolveIsInterface(element);
    directReferencesByType
        .computeIfAbsent(objectType.getUri(), k -> new ArrayList<>())
        .add(
            new ReferenceEntry(
                base.id(),
                base.getName(),
                base.getScopeName(),
                base.getComment(),
                isInterface,
                null));
  }

  // ========================= Inherited references =========================

  private static Map<String, List<ReferenceEntry>> findInheritedComponentReferences(
      Version version, Component component, Map<String, List<ReferenceEntry>> directReferences) {
    Map<String, List<ReferenceEntry>> inherited = new TreeMap<>();
    inherited.putAll(findInheritedDependencies(version, component, directReferences));
    inherited.putAll(findInheritedServices(version, component, directReferences));
    return inherited.isEmpty() ? Map.of() : inherited;
  }

  private static Map<String, List<ReferenceEntry>> findInheritedDependencyInstances(
      Version version,
      ComponentInstance componentInstance,
      Map<String, List<ReferenceEntry>> directReferences) {
    Set<Component> ancestorComponents = getAncestors(componentInstance.getComponent());
    return ancestorComponents.isEmpty()
        ? Map.of()
        : collectInheritedEntries(
            version,
            ObjectTypeListNamesEnum.DEPENDENCYINSTANCES,
            directReferences,
            element ->
                element instanceof DependencyInstance dependencyInstance
                    && matchesAncestors(dependencyInstance, ancestorComponents),
            (BomElement element) -> {
              DependencyInstance dependencyInstance = (DependencyInstance) element;
              return new ReferenceEntry(
                  dependencyInstance.id(),
                  dependencyInstance.getName(),
                  dependencyInstance.getScopeName(),
                  dependencyInstance.getComment(),
                  null,
                  findDependencyInstanceFromParent(dependencyInstance, ancestorComponents));
            });
  }

  private static Map<String, List<ReferenceEntry>> findInheritedDependencies(
      Version version, Component component, Map<String, List<ReferenceEntry>> directReferences) {
    Set<Component> ancestorComponents = new HashSet<>();
    collectAncestors(component, ancestorComponents);
    ancestorComponents.remove(component);
    return ancestorComponents.isEmpty()
        ? Map.of()
        : collectInheritedEntries(
            version,
            ObjectTypeListNamesEnum.DEPENDENCIES,
            directReferences,
            element ->
                element instanceof Dependency dependency
                    && matchesAncestorComponent(dependency, ancestorComponents),
            (BomElement element) -> {
              Dependency dependency = (Dependency) element;
              return new ReferenceEntry(
                  dependency.id(),
                  dependency.getName(),
                  dependency.getScopeName(),
                  dependency.getComment(),
                  null,
                  findDependencyFromParent(dependency, ancestorComponents));
            });
  }

  private static Map<String, List<ReferenceEntry>> findInheritedServices(
      Version version, Component component, Map<String, List<ReferenceEntry>> directReferences) {
    Set<Component> ancestorComponents = new HashSet<>();
    collectAncestors(component, ancestorComponents);
    ancestorComponents.remove(component);
    return ancestorComponents.isEmpty()
        ? Map.of()
        : collectInheritedEntries(
            version,
            ObjectTypeListNamesEnum.SERVICES,
            directReferences,
            element ->
                element instanceof Service service
                    && service.getComponent() != null
                    && ancestorComponents.contains(service.getComponent()),
            (BomElement element) -> {
              Service service = (Service) element;
              return new ReferenceEntry(
                  service.id(),
                  service.getName(),
                  service.getScopeName(),
                  service.getComment(),
                  null,
                  service.getComponentName());
            });
  }

  private static Set<Component> getAncestors(Component component) {
    if (component == null) {
      return Collections.emptySet();
    }
    Set<Component> ancestors = new HashSet<>();
    collectAncestors(component, ancestors);
    return ancestors;
  }

  private static void collectAncestors(Component component, Set<Component> result) {
    if (component.getParents() == null) {
      return;
    }
    for (Component parent : component.getParents()) {
      if (result.add(parent)) {
        collectAncestors(parent, result);
      }
    }
  }

  private static boolean matchesAncestors(
      DependencyInstance dependencyInstance, Set<Component> ancestorComponents) {
    Dependency dependency = dependencyInstance.getDependency();
    if (dependency == null) {
      return false;
    }
    boolean sourceMatch =
        dependencyInstance.isSourceAsComponent()
            && ancestorComponents.contains(dependency.getSourceComponent());
    boolean targetMatch =
        dependencyInstance.isTargetAsComponent()
            && ancestorComponents.contains(dependency.getDestinationComponent());
    return sourceMatch || targetMatch;
  }

  private static Set<String> collectDirectKeys(Map<String, List<ReferenceEntry>> directReferences) {
    Set<String> directKeys = new HashSet<>();
    for (List<ReferenceEntry> references : directReferences.values()) {
      for (ReferenceEntry reference : references) {
        directKeys.add(reference.id());
      }
    }
    return directKeys;
  }

  private static Map<String, List<ReferenceEntry>> collectInheritedEntries(
      Version version,
      ObjectTypeListNamesEnum type,
      Map<String, List<ReferenceEntry>> direct,
      Predicate<BomElement> matches,
      Function<BomElement, ReferenceEntry> entryMapper) {
    ConcurrentRepository<? extends BomElement> repository = getRepository(version, type);
    if (repository == null || repository.getData() == null) {
      return Map.of();
    }

    Set<String> directKeys = collectDirectKeys(direct);
    List<ReferenceEntry> entries = new ArrayList<>();
    for (BomElement element : repository.getData().values()) {
      if (!matches.test(element)) {
        continue;
      }
      ReferenceEntry entry = entryMapper.apply(element);
      if (!directKeys.contains(entry.id())) {
        entries.add(entry);
      }
    }
    entries.sort(REFERENCE_NAME_ORDER);
    return entries.isEmpty() ? Map.of() : Map.of(type.getUri(), entries);
  }

  private static boolean matchesAncestorComponent(Dependency dependency, Set<Component> ancestors) {
    return ancestors.contains(dependency.getSourceComponent())
        || ancestors.contains(dependency.getDestinationComponent());
  }

  private static ConcurrentRepository<? extends BomElement> getRepository(
      Version version, ObjectTypeListNamesEnum objectType) {
    return version.getRepository(objectType.getRepositoryKey());
  }

  private static BomElement findTargetEntityById(
      Version version, ObjectTypeListNamesEnum targetObjectType, String entityId) {
    ConcurrentRepository<? extends BomElement> targetRepository =
        getRepository(version, targetObjectType);
    if (targetRepository == null || targetRepository.getData() == null) {
      return null;
    }
    for (BomElement element : targetRepository.getData().values()) {
      if (element != null && entityId.equals(element.id())) {
        return element;
      }
    }
    return null;
  }

  private static ReferencesResponse emptyResponse() {
    return new ReferencesResponse(Map.of(), Map.of());
  }

  private static Boolean resolveIsInterface(BomElement element) {
    if (element instanceof Component comp) {
      return comp.isPublicInheritance() && comp.isTechnical() ? true : null;
    }
    return null;
  }

  private static boolean hasDirectReference(Object element, Class<?> targetClass, String id) {
    for (Field field : getCachedFields(element.getClass())) {
      try {
        if (fieldValueMatchesTarget(field.get(element), targetClass, id)) {
          return true;
        }
      } catch (IllegalAccessException e) {
        Log.warnf(e, "Unexpected inaccessible field on %s", element.getClass().getSimpleName());
      }
    }
    return false;
  }

  private static boolean fieldValueMatchesTarget(Object value, Class<?> targetClass, String id) {
    if (value == null) {
      return false;
    }
    if (targetClass.isInstance(value)) {
      return id.equals(((AbstractBase<?>) value).id());
    }
    if (value instanceof Collection<?> coll) {
      for (Object item : coll) {
        if (targetClass.isInstance(item) && id.equals(((AbstractBase<?>) item).id())) {
          return true;
        }
      }
    }
    return false;
  }

  private static @Nullable String findDependencyInstanceFromParent(
      DependencyInstance dependencyInstance, Set<Component> ancestorComponents) {
    Dependency dependency = dependencyInstance.getDependency();
    if (dependency == null) {
      return null;
    }
    Set<String> parentNames = new java.util.TreeSet<>();
    if (dependencyInstance.isSourceAsComponent()
        && ancestorComponents.contains(dependency.getSourceComponent())) {
      String name = dependency.getSourceComponentName();
      if (name != null) {
        parentNames.add(name);
      }
    }
    if (dependencyInstance.isTargetAsComponent()
        && ancestorComponents.contains(dependency.getDestinationComponent())) {
      String name = dependency.getDestinationComponentName();
      if (name != null) {
        parentNames.add(name);
      }
    }
    return parentNames.isEmpty() ? null : String.join("|", parentNames);
  }

  private static String findDependencyFromParent(
      Dependency dependency, Set<Component> ancestorComponents) {
    Set<String> parentNames = new java.util.TreeSet<>();
    if (ancestorComponents.contains(dependency.getSourceComponent())) {
      String name = dependency.getSourceComponentName();
      if (name != null) {
        parentNames.add(name);
      }
    }
    if (ancestorComponents.contains(dependency.getDestinationComponent())) {
      String name = dependency.getDestinationComponentName();
      if (name != null) {
        parentNames.add(name);
      }
    }
    return parentNames.isEmpty() ? null : String.join(",", parentNames);
  }

  private static List<Field> getCachedFields(Class<?> clazz) {
    return FIELDS_CACHE.computeIfAbsent(clazz, ReferencesService::collectDeclaredFields);
  }

  /**
   * Walks the class hierarchy and collects all non-static, non-synthetic fields, except the
   * Component ancestor cache.
   *
   * <p><b>JEP 500 warning:</b> this method uses {@link Field#setAccessible(boolean)}, which may be
   * restricted in future JDK versions (see <a href="https://openjdk.org/jeps/500">JEP 500</a>). If
   * that becomes an issue, the alternative is to replace reflection with explicit per-type
   * field-extraction logic (i.e. switch on the object type and extract references manually).
   */
  @SuppressWarnings("java:S3011") // setAccessible is intentional
  private static List<Field> collectDeclaredFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    Class<?> current = clazz;
    while (current != null && current != Object.class) {
      for (Field field : current.getDeclaredFields()) {
        boolean ancestorCache =
            current == Component.class && "internalAncestors".equals(field.getName());
        if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic() && !ancestorCache) {
          field.setAccessible(true);
          fields.add(field);
        }
      }
      current = current.getSuperclass();
    }
    return fields;
  }
}

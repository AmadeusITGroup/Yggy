package com.amadeus.aud.libraries.checker.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.VersionBuilder;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AccessControlEntitiesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AccessRightsEntryEntitiesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.AccessControlEntity;
import com.amadeus.aud.libraries.models.core.db.AccessRightsEntryEntity;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Scope;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BomFromAudRepositoryTest {

  private Version version;
  private VersionBuilder versionBuilder;
  private AudDataRepository audDataRepository;
  private BomFromAudRepository bomFromAudRepository;

  /** A Version whose repositories are all empty and registered. */
  private static Version emptyVersion() throws ReflectiveOperationException {
    Version newVersion = new Version();
    for (Field field : newVersion.getClass().getDeclaredFields()) {
      if (field.getName().endsWith("Repository")) {
        field.setAccessible(true);
        field.set(newVersion, field.getType().getDeclaredConstructor().newInstance());
      }
    }
    newVersion.setScopesRepository(new ScopesRepository());
    newVersion.setComponentsRepository(new ComponentsRepository());
    newVersion.setAccessControlEntitiesRepository(new AccessControlEntitiesRepository());
    newVersion.setAccessRightsEntryEntitiesRepository(new AccessRightsEntryEntitiesRepository());
    newVersion.getRepositoryNames();
    return newVersion;
  }

  @BeforeEach
  void setup() {
    try {
      version = emptyVersion();
      versionBuilder = mock(VersionBuilder.class);
      when(versionBuilder.buildVersion("999.999.999", audDataRepository)).thenReturn(version);
      audDataRepository = new AudDataRepository();
      bomFromAudRepository = new BomFromAudRepository(versionBuilder);
      bomFromAudRepository.initWithAudDataRepository(audDataRepository);
      Field versionField = bomFromAudRepository.getClass().getDeclaredField("version");
      versionField.setAccessible(true);
      versionField.set(bomFromAudRepository, version);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void listComponentsForDomain() {

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn("AUD");
    when(scope.id()).thenReturn("AUD");
    Component component1 = mock(Component.class);
    when(component1.getScope()).thenReturn(scope);
    when(component1.getScopeName()).thenReturn("AUD");
    when(component1.getName()).thenReturn("Component 1");
    when(component1.id()).thenReturn("Component 1");
    Component component2 = mock(Component.class);
    when(component2.getScope()).thenReturn(scope);
    when(component2.getScopeName()).thenReturn("AUD");
    when(component2.getName()).thenReturn("Component 2");
    when(component2.id()).thenReturn("Component 2");

    ScopesRepository scopesRepository = version.getScopesRepository();
    ConcurrentMap<String, Scope> scopesMap = scopesRepository.getData();
    scopesMap.put(scope.id(), scope);

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    ConcurrentMap<String, Component> componentMap = componentsRepository.getData();
    componentMap.put(component1.id(), component1);
    componentMap.put(component2.id(), component2);

    Component[] components = new Component[3];
    Collection<Component> componentCollection = bomFromAudRepository.list(Component.class, "AUD");
    componentCollection.toArray(components);
    assertThat(components[0]).isNotNull();
    assertThat(components[1]).isNotNull();
    assertThat(components[2]).isNull();

    assertThat(components[0].getScopeName()).isEqualTo(components[1].getScopeName());
    assertThat(components[0].getScopeName()).isEqualTo(scope.getName());

    Component c1 =
        componentCollection.stream()
            .filter(c -> component1.getName().equals(c.getName()))
            .findFirst()
            .orElse(null);
    assertThat(c1).isNotNull();

    Component c2 =
        componentCollection.stream()
            .filter(c -> component2.getName().equals(c.getName()))
            .findFirst()
            .orElse(null);
    assertThat(c2).isNotNull();
  }

  @Test
  void listComponentsForDifferentDomain() {

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn("AUD");
    when(scope.id()).thenReturn("AUD");
    Component component1 = mock(Component.class);
    when(component1.getScope()).thenReturn(scope);
    when(component1.getScopeName()).thenReturn("AUD");
    when(component1.getName()).thenReturn("Component 1");
    when(component1.id()).thenReturn("Component 1");

    ScopesRepository scopesRepository = version.getScopesRepository();
    ConcurrentMap<String, Scope> scopesMap = scopesRepository.getData();
    scopesMap.put(scope.id(), scope);

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    ConcurrentMap<String, Component> componentMap = componentsRepository.getData();
    componentMap.put(component1.id(), component1);

    Component[] components = new Component[3];
    Collection<Component> componentCollection = bomFromAudRepository.list(Component.class, "ACS");
    assertThat(componentCollection).isEmpty();
    componentCollection.toArray(components);
    assertThat(components[0]).isNull();
    assertThat(components[1]).isNull();
    assertThat(components[2]).isNull();
  }

  @Test
  void listAccessControlEntitiesForDifferentDomain() {

    AccessControlEntity accessControlEntity1 = mock(AccessControlEntity.class);

    AccessControlEntity accessControlEntity2 = mock(AccessControlEntity.class);

    when(accessControlEntity1.id()).thenReturn("accessControlEntity1 Unique ID");
    when(accessControlEntity2.id()).thenReturn("accessControlEntity2 Unique ID");

    AccessControlEntitiesRepository aclRepository = version.getAccessControlEntitiesRepository();
    ConcurrentMap<String, AccessControlEntity> aclMap = aclRepository.getData();
    aclMap.put(accessControlEntity1.id(), accessControlEntity1);
    aclMap.put(accessControlEntity2.id(), accessControlEntity2);

    AccessControlEntity[] acls = new AccessControlEntity[3];
    Collection<AccessControlEntity> aclCollection = aclRepository.getData().values();

    assertThat(aclCollection).hasSize(2);
    aclCollection.toArray(acls);
    assertThat(acls[0]).isNotNull();
    assertThat(acls[1]).isNotNull();
    assertThat(acls[2]).isNull();

    assertThat(acls[0].id()).isNotEqualTo(acls[1].id());

    AccessControlEntity c1 =
        aclCollection.stream()
            .filter(c -> accessControlEntity1.id().equals(c.id()))
            .findFirst()
            .orElse(null);
    assertThat(c1).isNotNull();

    AccessControlEntity c2 =
        aclCollection.stream()
            .filter(c -> accessControlEntity2.id().equals(c.id()))
            .findFirst()
            .orElse(null);
    assertThat(c2).isNotNull();
  }

  @Test
  void listAccessRightsEntriesForDifferentDomain() {

    AccessRightsEntryEntity accessControlEntity1 = mock(AccessRightsEntryEntity.class);

    AccessRightsEntryEntity accessControlEntity2 = mock(AccessRightsEntryEntity.class);

    when(accessControlEntity1.id()).thenReturn("Entry1 Unique ID");
    when(accessControlEntity2.id()).thenReturn("Entry2 Unique ID");

    AccessRightsEntryEntitiesRepository aclRepository =
        version.getAccessRightsEntryEntitiesRepository();
    ConcurrentMap<String, AccessRightsEntryEntity> aclMap = aclRepository.getData();
    aclMap.put(accessControlEntity1.id(), accessControlEntity1);
    aclMap.put(accessControlEntity2.id(), accessControlEntity2);

    AccessRightsEntryEntity[] acls = new AccessRightsEntryEntity[3];
    Collection<AccessRightsEntryEntity> aclCollection = aclRepository.getData().values();

    assertThat(aclCollection).hasSize(2);
    aclCollection.toArray(acls);
    assertThat(acls[0]).isNotNull();
    assertThat(acls[1]).isNotNull();
    assertThat(acls[2]).isNull();

    assertThat(acls[0].id()).isNotEqualTo(acls[1].id());

    AccessRightsEntryEntity c1 =
        aclCollection.stream()
            .filter(c -> accessControlEntity1.id().equals(c.id()))
            .findFirst()
            .orElse(null);
    assertThat(c1).isNotNull();

    AccessRightsEntryEntity c2 =
        aclCollection.stream()
            .filter(c -> accessControlEntity2.id().equals(c.id()))
            .findFirst()
            .orElse(null);
    assertThat(c2).isNotNull();
  }

  @Test
  void listEverythingForNullDomain() {

    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn("AUD");
    when(scope.id()).thenReturn("AUD");
    Component component1 = mock(Component.class);
    when(component1.getScope()).thenReturn(scope);
    when(component1.getScopeName()).thenReturn("AUD");
    when(component1.getName()).thenReturn("Component 1");
    when(component1.id()).thenReturn("Component 1");
    Component component2 = mock(Component.class);
    when(component2.getScope()).thenReturn(scope);
    when(component2.getScopeName()).thenReturn("AUD");
    when(component2.getName()).thenReturn("Component 2");
    when(component2.id()).thenReturn("Component 2");

    ScopesRepository scopesRepository = version.getScopesRepository();
    ConcurrentMap<String, Scope> scopesMap = scopesRepository.getData();
    scopesMap.put(scope.id(), scope);

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    ConcurrentMap<String, Component> componentMap = componentsRepository.getData();
    componentMap.put(component1.id(), component1);
    componentMap.put(component2.id(), component2);

    AccessControlEntity accessControlEntity1 = mock(AccessControlEntity.class);
    AccessControlEntity accessControlEntity2 = mock(AccessControlEntity.class);
    AccessControlEntity accessControlEntity3 = mock(AccessControlEntity.class);
    when(accessControlEntity1.id()).thenReturn("accessControlEntity1 Unique ID");
    when(accessControlEntity2.id()).thenReturn("accessControlEntity2 Unique ID");
    when(accessControlEntity3.id()).thenReturn("accessControlEntity3 Unique ID");

    AccessControlEntitiesRepository aclRepository = version.getAccessControlEntitiesRepository();
    ConcurrentMap<String, AccessControlEntity> aclMap = aclRepository.getData();
    aclMap.put(accessControlEntity1.id(), accessControlEntity1);
    aclMap.put(accessControlEntity2.id(), accessControlEntity2);
    aclMap.put(accessControlEntity3.id(), accessControlEntity3);

    AccessRightsEntryEntity accessRightsEntity1 = mock(AccessRightsEntryEntity.class);
    when(accessRightsEntity1.id()).thenReturn("Entry1 Unique ID");

    AccessRightsEntryEntitiesRepository rightsRepository =
        version.getAccessRightsEntryEntitiesRepository();
    ConcurrentMap<String, AccessRightsEntryEntity> rightsMap = rightsRepository.getData();
    rightsMap.put(accessRightsEntity1.id(), accessRightsEntity1);

    Component[] components = new Component[3];
    Collection<Component> componentCollection = bomFromAudRepository.list(Component.class, "AUD");
    componentCollection.toArray(components);
    assertThat(components[0]).isNotNull();
    assertThat(components[1]).isNotNull();
    assertThat(components[2]).isNull();

    assertThat(components[0].getScopeName()).isEqualTo(components[1].getScopeName());
    assertThat(components[0].getScopeName()).isEqualTo(scope.getName());

    Component c1 =
        componentCollection.stream()
            .filter(c -> component1.getName().equals(c.getName()))
            .findFirst()
            .orElse(null);
    assertThat(c1).isNotNull();

    Component c2 =
        componentCollection.stream()
            .filter(c -> component2.getName().equals(c.getName()))
            .findFirst()
            .orElse(null);
    assertThat(c2).isNotNull();

    AccessControlEntity[] acls = new AccessControlEntity[3];
    Collection<AccessControlEntity> aclCollection =
        version.getAccessControlEntitiesRepository().getData().values();

    assertThat(aclCollection).hasSize(3);
    aclCollection.toArray(acls);
    assertThat(acls[0]).isNotNull();
    assertThat(acls[1]).isNotNull();
    assertThat(acls[2]).isNotNull();

    assertThat(acls[1].id()).isNotEqualTo(acls[2].id());

    AccessControlEntity acl1 =
        aclCollection.stream()
            .filter(c -> accessControlEntity1.id().equals(c.id()))
            .findFirst()
            .orElse(null);
    assertThat(acl1).isNotNull();

    AccessControlEntity acl2 =
        aclCollection.stream()
            .filter(c -> accessControlEntity2.id().equals(c.id()))
            .findFirst()
            .orElse(null);
    assertThat(acl2).isNotNull();

    AccessControlEntity acl3 =
        aclCollection.stream()
            .filter(c -> accessControlEntity3.id().equals(c.id()))
            .findFirst()
            .orElse(null);
    assertThat(acl3).isNotNull();

    AccessRightsEntryEntity[] rights = new AccessRightsEntryEntity[3];
    Collection<AccessRightsEntryEntity> rightsCollection =
        version.getAccessRightsEntryEntitiesRepository().getData().values();

    assertThat(rightsCollection).hasSize(1);
    rightsCollection.toArray(rights);
    assertThat(rights[0]).isNotNull();
    assertThat(rights[1]).isNull();
    assertThat(rights[2]).isNull();

    assertThat(rights[0].id()).isEqualTo(accessRightsEntity1.id());
  }

  private static Component componentInScope(String name, String scopeName) {
    Scope scope = mock(Scope.class);
    when(scope.getName()).thenReturn(scopeName);
    Component component = mock(Component.class);
    when(component.getScope()).thenReturn(scope);
    when(component.getName()).thenReturn(name);
    when(component.id()).thenReturn(name);
    return component;
  }

  @Test
  void listSameTypeForSeveralDomainsReturnsEachDomainElements() {
    ConcurrentMap<String, Component> componentMap = version.getComponentsRepository().getData();
    Component aud1 = componentInScope("aud-1", "AUD");
    Component aud2 = componentInScope("aud-2", "AUD");
    Component acs1 = componentInScope("acs-1", "ACS");
    Component noScope = mock(Component.class);
    componentMap.put("aud-1", aud1);
    componentMap.put("aud-2", aud2);
    componentMap.put("acs-1", acs1);
    componentMap.put("no-scope", noScope);

    assertThat(bomFromAudRepository.list(Component.class, "AUD"))
        .containsExactlyInAnyOrder(aud1, aud2);
    assertThat(bomFromAudRepository.list(Component.class, "ACS")).containsExactly(acs1);
    assertThat(bomFromAudRepository.list(Component.class, "OTHER")).isEmpty();
    assertThat(bomFromAudRepository.list(Component.class, null))
        .containsExactlyInAnyOrder(aud1, aud2, acs1, noScope);
  }

  @Test
  void listReturnsAModifiableCopy() {
    version.getComponentsRepository().getData().put("aud-1", componentInScope("aud-1", "AUD"));

    Collection<Component> first = bomFromAudRepository.list(Component.class, "AUD");
    first.clear();

    assertThat(bomFromAudRepository.list(Component.class, "AUD")).hasSize(1);
  }

  /** A BOM initialised from {@link #version} the normal way, so the scope index is used. */
  private BomFromAudRepository initializedBom(Version builtVersion) {
    VersionBuilder builder = mock(VersionBuilder.class);
    when(builder.buildVersion(any(), any())).thenReturn(builtVersion);
    BomFromAudRepository bom = new BomFromAudRepository(builder);
    bom.initWithAudDataRepository(new AudDataRepository());
    return bom;
  }

  @Test
  void indexedListReturnsElementsPresentWhenTheVersionWasBuilt() {
    Component aud1 = componentInScope("aud-1", "AUD");
    Component acs1 = componentInScope("acs-1", "ACS");
    version.getComponentsRepository().getData().put("aud-1", aud1);
    version.getComponentsRepository().getData().put("acs-1", acs1);

    BomFromAudRepository bom = initializedBom(version);

    assertThat(bom.list(Component.class, "AUD")).containsExactly(aud1);
    assertThat(bom.list(Component.class, "ACS")).containsExactly(acs1);
    assertThat(bom.list(Component.class, null)).containsExactlyInAnyOrder(aud1, acs1);
  }

  /**
   * Pins the documented contract: a built Version is a snapshot, so changes made to it after
   * initialisation are not seen by scoped lists. Changing this is a deliberate decision.
   */
  @Test
  void indexedListIgnoresChangesMadeToTheVersionAfterInitialisation() {
    Component before = componentInScope("before", "AUD");
    version.getComponentsRepository().getData().put("before", before);
    BomFromAudRepository bom = initializedBom(version);

    version.getComponentsRepository().put("after", componentInScope("after", "AUD"));
    version.getComponentsRepository().getData().remove("before");

    assertThat(bom.list(Component.class, "AUD")).containsExactly(before);
  }

  @Test
  void reinitialisingWithANewVersionUsesTheNewVersion() throws ReflectiveOperationException {
    Component old = componentInScope("old", "AUD");
    version.getComponentsRepository().getData().put("old", old);
    VersionBuilder builder = mock(VersionBuilder.class);
    BomFromAudRepository bom = new BomFromAudRepository(builder);
    when(builder.buildVersion(any(), any())).thenReturn(version);
    bom.initWithAudDataRepository(new AudDataRepository());
    assertThat(bom.list(Component.class, "AUD")).containsExactly(old);

    Version newVersion = emptyVersion();
    Component fresh = componentInScope("fresh", "AUD");
    newVersion.getComponentsRepository().getData().put("fresh", fresh);
    when(builder.buildVersion(any(), any())).thenReturn(newVersion);
    bom.initWithAudDataRepository(new AudDataRepository());

    assertThat(bom.list(Component.class, "AUD")).containsExactly(fresh);
  }

  @Test
  void listScansWhenTheVersionWasNotBuiltByInitialisation() {
    // setup() swaps the version by reflection after init, so there is no index for it.
    Component before = componentInScope("before", "AUD");
    version.getComponentsRepository().getData().put("before", before);
    assertThat(bomFromAudRepository.list(Component.class, "AUD")).containsExactly(before);

    Component after = componentInScope("after", "AUD");
    version.getComponentsRepository().put("after", after);

    assertThat(bomFromAudRepository.list(Component.class, "AUD"))
        .containsExactlyInAnyOrder(before, after);
  }
}

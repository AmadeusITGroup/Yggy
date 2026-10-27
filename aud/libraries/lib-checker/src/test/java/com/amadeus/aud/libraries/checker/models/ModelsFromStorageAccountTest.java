package com.amadeus.aud.libraries.checker.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.AudDataLoader;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import io.smallrye.mutiny.Uni;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModelsFromStorageAccountTest {
  private AudDataRepository repository;
  private AudDataLoader loader;

  @BeforeEach
  void setup() {
    repository = new AudDataRepository();
    repository.setData(new ConcurrentHashMap<>());
    loader = mock(AudDataLoader.class);
    when(loader.initObjects("latest", false)).thenReturn(Uni.createFrom().item(repository));
  }

  private static V1Metadata metadata(String name, String scope) {
    V1Metadata metadata = new V1Metadata();
    metadata.setName(name);
    metadata.setScope(scope);
    return metadata;
  }

  private static V1Component component(String name, String scope) {
    V1Component component = new V1Component();
    component.setMetadata(metadata(name, scope));
    return component;
  }

  private static V1Dependency dependency(String name, String scope) {
    V1Dependency dependency = new V1Dependency();
    dependency.setMetadata(metadata(name, scope));
    return dependency;
  }

  private static V1Scope scope(String name) {
    V1Scope scope = new V1Scope();
    scope.setMetadata(metadata(name, name));
    return scope;
  }

  private static ModelsExtension extension(
      Map<Class<? extends ModelElement>, List<? extends ModelElement>> data,
      Class<? extends ModelElement> domainType) {
    return new ModelsExtension() {
      @Override
      public Map<Class<? extends ModelElement>, List<? extends ModelElement>> load() {
        return data;
      }

      @Override
      public Class<? extends ModelElement> domainType() {
        return domainType;
      }
    };
  }

  private ModelsFromStorageAccount loaded(List<ModelsExtension> extensions) {
    ModelsFromStorageAccount models = new ModelsFromStorageAccount(loader, extensions);
    models.loadLatestWithDomains(Set.of(), Map.of());
    return models;
  }

  @Test
  void allForDomainReturnsOnlyThatDomainAcrossTypes() {
    V1Component audComponent = component("c1", "AUD");
    V1Component acsComponent = component("c2", "ACS");
    V1Dependency audDependency = dependency("d1", "AUD");
    repository.getData().put(V1Component.class, List.of(audComponent, acsComponent));
    repository.getData().put(V1Dependency.class, List.of(audDependency));

    ModelsFromStorageAccount models = loaded(List.of());

    assertThat(models.all("AUD")).containsExactlyInAnyOrder(audComponent, audDependency);
    assertThat(models.all("ACS")).containsExactly(acsComponent);
    assertThat(models.all("OTHER")).isEmpty();
    assertThat(models.all(null))
        .containsExactlyInAnyOrder(audComponent, acsComponent, audDependency);
  }

  @Test
  void allForDomainReturnsAModifiableCopy() {
    repository.getData().put(V1Component.class, List.of(component("c1", "AUD")));
    ModelsFromStorageAccount models = loaded(List.of());

    Collection<ModelElement> first = models.all("AUD");
    first.clear();

    assertThat(models.all("AUD")).hasSize(1);
  }

  @Test
  void withoutOverriddenDomainsStoredDataIsKept() {
    V1Component stored = component("stored", "AUD");
    repository.getData().put(V1Component.class, new ArrayList<>(List.of(stored)));

    ModelsFromStorageAccount models = loaded(List.of());

    assertThat(models.all("AUD")).containsExactly(stored);
    List<? extends ModelElement> integrated = models.latestIntegrated("AUD").get(V1Component.class);
    assertThat(new ArrayList<ModelElement>(integrated)).containsExactly(stored);
  }

  @Test
  void overriddenDomainWithoutLocalDataIsEmptied() {
    V1Component stored = component("stored", "AUD");
    V1Component other = component("other", "ACS");
    repository.getData().put(V1Component.class, new ArrayList<>(List.of(stored, other)));
    ModelsFromStorageAccount models = new ModelsFromStorageAccount(loader, List.of());

    models.loadLatestWithDomains(Set.of("AUD"), Map.of());

    assertThat(models.all("AUD")).isEmpty();
    assertThat(models.all("ACS")).containsExactly(other);
  }

  @Test
  void checkedDomainsAreReplacedByLocalData() {
    V1Component remoteAud = component("remote", "AUD");
    V1Component remoteAcs = component("acs", "ACS");
    repository.getData().put(V1Component.class, new ArrayList<>(List.of(remoteAud, remoteAcs)));
    V1Component localAud = component("local", "AUD");
    ModelsFromStorageAccount models = new ModelsFromStorageAccount(loader, List.of());

    models.loadLatestWithDomains(Set.of("AUD"), Map.of(V1Component.class, List.of(localAud)));

    assertThat(models.list(V1Component.class, null)).containsExactlyInAnyOrder(remoteAcs, localAud);
    assertThat(models.all("AUD")).containsExactly(localAud);
    List<? extends ModelElement> integrated = models.latestIntegrated("AUD").get(V1Component.class);
    assertThat(new ArrayList<ModelElement>(integrated)).containsExactly(remoteAud);
  }

  @Test
  void domainNamesDefaultToV1Scope() {
    repository.getData().put(V1Scope.class, List.of(scope("AUD"), scope("ACS")));

    assertThat(loaded(List.of()).allDomainNames()).containsExactlyInAnyOrder("AUD", "ACS");
  }

  @Test
  void extensionDataIsAddedAndVisibleInScopedQueries() {
    V1Dependency fromExtension = dependency("extra", "AUD");
    ModelsFromStorageAccount models =
        loaded(List.of(extension(Map.of(V1Dependency.class, List.of(fromExtension)), null)));

    assertThat(models.list(V1Dependency.class, null)).containsExactly(fromExtension);
    assertThat(models.all("AUD")).containsExactly(fromExtension);
  }

  @Test
  void extensionCanChangeTheDomainType() {
    repository.getData().put(V1Scope.class, List.of(scope("ignored")));
    ModelsFromStorageAccount models =
        loaded(
            List.of(
                extension(
                    Map.of(V1Component.class, List.of(component("AUD", "Governance"))),
                    V1Component.class)));

    assertThat(models.allDomainNames()).containsExactly("AUD");
  }

  @Test
  void twoExtensionsDefiningTheDomainTypeAreRejected() {
    List<ModelsExtension> extensions =
        List.of(extension(Map.of(), V1Component.class), extension(Map.of(), V1Dependency.class));

    assertThatThrownBy(() -> new ModelsFromStorageAccount(loader, extensions))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("domain type");
  }

  @Test
  void twoExtensionsProvidingTheSameTypeAreRejected() {
    Map<Class<? extends ModelElement>, List<? extends ModelElement>> data =
        Map.of(V1Component.class, List.of());
    ModelsFromStorageAccount models =
        new ModelsFromStorageAccount(loader, List.of(extension(data, null), extension(data, null)));

    assertThatThrownBy(() -> models.loadLatestWithDomains(Set.of(), Map.of()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(V1Component.class.getName());
  }

  @Test
  void scopedQueriesAcceptANonStringDomain() {
    V1Component aud = component("c1", "AUD");
    V1Component acs = component("c2", "ACS");
    repository.getData().put(V1Component.class, List.of(aud, acs));
    ModelsFromStorageAccount models = loaded(List.of());
    CharSequence domain = new StringBuilder("AUD");

    assertThat(models.list(V1Component.class, domain)).containsExactly(aud);
    List<? extends ModelElement> fromMap = models.allAsMap(domain).get(V1Component.class);
    assertThat(new ArrayList<ModelElement>(fromMap)).containsExactly(aud);
    assertThat(models.all(domain)).containsExactly(aud);
  }

  @Test
  void listOfAMissingTypeIsEmpty() {
    ModelsFromStorageAccount models = loaded(List.of());

    assertThat(models.list(V1Component.class, null)).isEmpty();
    assertThat(models.single(V1Component.class, "any")).isEmpty();
  }
}

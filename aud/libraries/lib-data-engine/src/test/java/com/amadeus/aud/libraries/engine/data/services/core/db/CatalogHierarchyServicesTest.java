package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.instanceMetadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.named;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Namespace;
import com.amadeus.aud.libraries.models.core.db.Role;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1Namespace;
import com.amadeus.aud.libraries.yaml.models.V1NamespaceSpec;
import com.amadeus.aud.libraries.yaml.models.V1Role;
import com.amadeus.aud.libraries.yaml.models.V1ScopeSpec;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CatalogHierarchyServicesTest {
  @Nested
  class ComponentTests {
    @Test
    void initWithoutDataOrScopesRepositoryDoesNotCreateComponents() {
      // Arrange
      ComponentService sut = configure(new ComponentService());
      Version missingDataVersion = new Version();
      Version missingScopesVersion = new Version();
      missingScopesVersion.setAudDataRepository(new AudDataRepository());

      // Act
      sut.init(missingDataVersion);
      sut.init(missingScopesVersion);

      // Assert
      assertThat(missingDataVersion.getComponentsRepository()).isNull();
      assertThat(missingScopesVersion.getComponentsRepository()).isNull();
    }

    @Test
    void initRequiresTopLevelAndTopLevelContainerComponents() {
      // Arrange
      CoreDbServiceTestFixture missingTopLevel = new CoreDbServiceTestFixture();
      missingTopLevel.addData(
          V1Component.class, List.of(component("TopLevelContainer", true, true, List.of())));
      CoreDbServiceTestFixture missingTopLevelContainer = new CoreDbServiceTestFixture();
      missingTopLevelContainer.addData(
          V1Component.class, List.of(component("TopLevel", true, false, List.of())));
      ComponentService sut = configure(new ComponentService());

      // Act and assert
      assertThatThrownBy(() -> sut.init(missingTopLevel.version))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("TopLevel component not found");
      assertThatThrownBy(() -> sut.init(missingTopLevelContainer.version))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("TopLevelContainer component not found");
    }

    @Test
    void initBuildsAutomaticAndExplicitHierarchyAndKeepsFirstConflictingDefinition() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      V1Component duplicate = component("duplicate", false, true, List.of());
      fixture.addData(
          V1Component.class,
          List.of(
              component("TopLevel", true, false, List.of()),
              component("TopLevelContainer", true, true, List.of()),
              component("TopLevelIaaS", true, false, List.of()),
              component("container", false, true, List.of()),
              component("iaas", false, false, List.of()),
              component("unspecified", false, null, List.of()),
              component("explicit", true, false, List.of("TopLevel", "explicit", "missing-parent")),
              duplicate,
              duplicate,
              component("duplicate", false, false, List.of()),
              component("orphan", false, true, List.of(), "missing-scope")));
      ComponentService sut = configure(new ComponentService());

      // Act
      sut.init(fixture.version);

      // Assert
      Component topLevel = fixture.version.getComponentsRepository().get("TopLevel");
      Component topLevelContainer =
          fixture.version.getComponentsRepository().get("TopLevelContainer");
      Component topLevelIaas = fixture.version.getComponentsRepository().get("TopLevelIaaS");
      Component container = fixture.version.getComponentsRepository().get("container");
      Component iaas = fixture.version.getComponentsRepository().get("iaas");
      Component unspecified = fixture.version.getComponentsRepository().get("unspecified");
      Component explicit = fixture.version.getComponentsRepository().get("explicit");
      Component keptDuplicate = fixture.version.getComponentsRepository().get("duplicate");
      assertThat(container.getParents()).containsExactlyInAnyOrder(topLevel, topLevelContainer);
      assertThat(topLevel.getChildren()).contains(container, iaas, unspecified, keptDuplicate);
      assertThat(iaas.getParents()).containsExactlyInAnyOrder(topLevel, topLevelIaas);
      assertThat(unspecified.getParents()).containsExactly(topLevel);
      assertThat(explicit.getParents()).containsExactly(topLevel);
      assertThat(topLevel.getChildren()).contains(explicit);
      assertThat(keptDuplicate.isContainer()).isTrue();
      assertThat(fixture.version.getComponentsRepository().getData()).hasSize(9);
      assertThat(fixture.version.getComponentsRepository().get("orphan").getScope()).isNull();
    }

    @Test
    void initAllowsMissingTopLevelIaaSButRejectsNullExplicitParentList() {
      // Arrange
      CoreDbServiceTestFixture withoutIaas = new CoreDbServiceTestFixture();
      withoutIaas.addData(
          V1Component.class,
          List.of(
              component("TopLevel", true, false, List.of()),
              component("TopLevelContainer", true, true, List.of()),
              component("workload", false, false, List.of())));
      ComponentService sut = configure(new ComponentService());
      sut.init(withoutIaas.version);
      CoreDbServiceTestFixture nullParents = new CoreDbServiceTestFixture();
      nullParents.addData(
          V1Component.class,
          List.of(
              component("TopLevel", true, false, List.of()),
              component("TopLevelContainer", true, true, List.of()),
              component("invalid", false, false, null)));

      // Act and assert
      assertThat(withoutIaas.version.getComponentsRepository().get("workload").getParents())
          .containsExactly(withoutIaas.version.getComponentsRepository().get("TopLevel"));
      assertThatThrownBy(() -> sut.init(nullParents.version))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  class RoleTests {
    @Test
    void initStopsWithoutScopesRepository() {
      // Arrange
      Version version = new Version();
      RoleService sut = configure(new RoleService());

      // Act
      sut.init(version);

      // Assert
      assertThat(version.getRolesRepository()).isNull();
    }

    @Test
    void initAssociatesDefinedAndUsedRolesWhileIgnoringMissingOrAbsentLists() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      Scope consumer =
          fixture.addScope("consumer", new V1ScopeSpec().roles(List.of("reader", "missing-role")));
      fixture.addScope("empty-roles", new V1ScopeSpec().roles(List.of()));
      fixture.addScope("null-roles", new V1ScopeSpec().roles(null));
      fixture.addScope("null-spec", null);
      fixture.addData(
          V1Role.class,
          List.of(
              new V1Role().metadata(metadata("reader", CoreDbServiceTestFixture.SCOPE_NAME)),
              new V1Role().metadata(metadata("orphan", "missing-scope"))));
      RoleService sut = configure(new RoleService());

      // Act
      sut.init(fixture.version);

      // Assert
      Role reader = fixture.version.getRolesRepository().get("reader");
      assertThat(reader.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getDefinedRoles()).contains(reader);
      assertThat(reader.getScopes()).containsExactly(consumer);
      assertThat(consumer.getRoles()).containsExactly(reader);
      assertThat(fixture.version.getRolesRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class NamespaceTests {
    @Test
    void initStopsWithoutScopesRepository() {
      // Arrange
      Version version = new Version();
      NamespaceService sut = configure(new NamespaceService());

      // Act
      sut.init(version);

      // Assert
      assertThat(version.getNamespacesRepository()).isNull();
    }

    @Test
    void initAssociatesAuthorizedScopesAndHandlesNullEmptyAndMissingReferences() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      Scope authorized = fixture.addScope("authorized");
      fixture.addData(
          V1Namespace.class,
          List.of(
              namespace("linked", CoreDbServiceTestFixture.SCOPE_NAME, List.of("authorized")),
              namespace("missing", CoreDbServiceTestFixture.SCOPE_NAME, List.of("missing-scope")),
              namespace("empty", CoreDbServiceTestFixture.SCOPE_NAME, List.of()),
              namespace("null-list", CoreDbServiceTestFixture.SCOPE_NAME, null),
              namespaceWithoutSpec("null-spec", CoreDbServiceTestFixture.SCOPE_NAME),
              namespace("orphan", "missing-scope", List.of("authorized"))));
      NamespaceService sut = configure(new NamespaceService());

      // Act
      sut.init(fixture.version);

      // Assert
      Namespace linked = fixture.version.getNamespacesRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getNamespaces()).contains(linked);
      assertThat(linked.getAuthorizedScopes()).containsExactly(authorized);
      assertThat(fixture.version.getNamespacesRepository().get("missing").getAuthorizedScopes())
          .containsExactly((Scope) null);
      assertThat(fixture.version.getNamespacesRepository().get("empty").getAuthorizedScopes())
          .isNull();
      assertThat(fixture.version.getNamespacesRepository().get("null-list").getAuthorizedScopes())
          .isNull();
      assertThat(fixture.version.getNamespacesRepository().get("null-spec").getAuthorizedScopes())
          .isNull();
      assertThat(fixture.version.getNamespacesRepository().get("orphan").getScope()).isNull();
    }
  }

  @Nested
  class AutomaticTemplateInstanceTests {
    @Test
    void initStopsAtMissingLandingZoneAndComponentRepositories() {
      // Arrange
      AutomaticTemplateInstanceService sut = configure(new AutomaticTemplateInstanceService());
      Version version = new Version();

      // Act and assert
      sut.initAutomaticTemplateInstances(version, List.of(), null);
      assertThat(version.getAutomaticTemplateInstancesRepository()).isNull();
      version.setLandingZonesRepository(new LandingZonesRepository());
      sut.initAutomaticTemplateInstances(version, List.of(), null);
      assertThat(version.getAutomaticTemplateInstancesRepository()).isNull();
    }

    @Test
    void initLinksReferencesInvokesCallbackAndHandlesMissingEmptyAndNullInputs() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      LandingZone landingZone = named(new LandingZone(), "landing-zone");
      Component component =
          CoreDbServiceTestFixture.component(
              "component", CoreDbServiceTestFixture.SCOPE_NAME, false, false, false, List.of());
      LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
      ComponentsRepository componentsRepository = new ComponentsRepository();
      landingZonesRepository.put(landingZone.id(), landingZone);
      componentsRepository.put(component.id(), component);
      fixture.version.setLandingZonesRepository(landingZonesRepository);
      fixture.version.setComponentsRepository(componentsRepository);
      List<ModelElement> templates =
          List.of(
              template("linked", CoreDbServiceTestFixture.SCOPE_NAME, "landing-zone", "component"),
              template(
                  "missing",
                  CoreDbServiceTestFixture.SCOPE_NAME,
                  "missing-zone",
                  "missing-component"),
              template("empty", CoreDbServiceTestFixture.SCOPE_NAME, "", ""),
              template("null", CoreDbServiceTestFixture.SCOPE_NAME, null, null),
              template("orphan", "missing-scope", "", ""));
      AtomicInteger callbackCount = new AtomicInteger();
      AutomaticTemplateInstanceService sut = configure(new AutomaticTemplateInstanceService());

      // Act
      sut.initAutomaticTemplateInstances(
          fixture.version, templates, ignored -> callbackCount.incrementAndGet());

      // Assert
      AutomaticTemplateInstance linked =
          fixture.version.getAutomaticTemplateInstancesRepository().get("linked");
      assertThat(linked.getScope()).isSameAs(fixture.scope);
      assertThat(fixture.scope.getAutomaticTemplateInstances()).contains(linked);
      assertThat(linked.getLandingZone()).isSameAs(landingZone);
      assertThat(landingZone.getAutomaticTemplateInstances()).contains(linked);
      assertThat(linked.getComponent()).isSameAs(component);
      assertThat(component.getAutomaticTemplateInstances()).contains(linked);
      assertThat(
              fixture
                  .version
                  .getAutomaticTemplateInstancesRepository()
                  .get("missing")
                  .getLandingZone())
          .isNull();
      assertThat(
              fixture.version.getAutomaticTemplateInstancesRepository().get("empty").getComponent())
          .isNull();
      assertThat(
              fixture
                  .version
                  .getAutomaticTemplateInstancesRepository()
                  .get("null")
                  .getLandingZone())
          .isNull();
      assertThat(fixture.version.getAutomaticTemplateInstancesRepository().get("orphan").getScope())
          .isNull();
      assertThat(callbackCount).hasValue(4);
    }

    @Test
    void initAcceptsNullTemplateListAndNullCallback() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.version.setLandingZonesRepository(new LandingZonesRepository());
      fixture.version.setComponentsRepository(new ComponentsRepository());
      AutomaticTemplateInstanceService sut = configure(new AutomaticTemplateInstanceService());

      // Act
      sut.initAutomaticTemplateInstances(fixture.version, null, null);

      // Assert
      assertThat(fixture.version.getAutomaticTemplateInstancesRepository()).isNotNull();
      assertThat(fixture.version.getAutomaticTemplateInstancesRepository().getData()).isEmpty();
    }

    @Test
    void initAcceptsNullCallbackForPopulatedTemplateList() {
      // Arrange
      CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
      fixture.version.setLandingZonesRepository(new LandingZonesRepository());
      fixture.version.setComponentsRepository(new ComponentsRepository());
      AutomaticTemplateInstanceService sut = configure(new AutomaticTemplateInstanceService());

      // Act
      sut.initAutomaticTemplateInstances(
          fixture.version,
          List.of(template("without-callback", CoreDbServiceTestFixture.SCOPE_NAME, "", "")),
          null);

      // Assert
      assertThat(fixture.version.getAutomaticTemplateInstancesRepository().get("without-callback"))
          .isNotNull();
    }
  }

  private static V1Component component(
      String name, boolean technical, Boolean container, List<String> parents) {
    return component(name, technical, container, parents, CoreDbServiceTestFixture.SCOPE_NAME);
  }

  private static V1Component component(
      String name, boolean technical, Boolean container, List<String> parents, String scopeName) {
    return new V1Component()
        .metadata(metadata(name, scopeName))
        .spec(new V1ComponentSpec().isTechnical(technical).isContainer(container).parents(parents));
  }

  private static V1Namespace namespace(
      String name, String scopeName, List<String> authorizedScopes) {
    return new V1Namespace()
        .metadata(metadata(name, scopeName))
        .spec(new V1NamespaceSpec().authorizedScopes(authorizedScopes));
  }

  private static V1Namespace namespaceWithoutSpec(String name, String scopeName) {
    return new V1Namespace().metadata(metadata(name, scopeName)).spec(null);
  }

  private static V1ComponentInstance template(
      String name, String scopeName, String landingZone, String component) {
    V1ComponentInstance data =
        new V1ComponentInstance().spec(new V1ComponentInstanceSpec().component(component));
    data.setMetadata(instanceMetadata(name, scopeName, landingZone));
    return data;
  }
}

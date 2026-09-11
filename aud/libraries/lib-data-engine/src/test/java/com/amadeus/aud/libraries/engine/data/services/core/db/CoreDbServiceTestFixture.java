package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1ComponentSpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.amadeus.aud.libraries.yaml.models.V1ScopeSpec;
import java.util.List;

final class CoreDbServiceTestFixture {
  static final String SCOPE_NAME = "test-scope";
  static final String VERSION_NUMBER = "1.2.3";

  final AudDataRepository dataRepository = new AudDataRepository();
  final ScopesRepository scopesRepository = new ScopesRepository();
  final Version version = new Version();
  final Scope scope;

  CoreDbServiceTestFixture() {
    version.setVersionNumber(VERSION_NUMBER);
    version.setAudDataRepository(dataRepository);
    version.setScopesRepository(scopesRepository);
    scope = addScope(SCOPE_NAME, new V1ScopeSpec());
    addScope("Governance");
    addScope("GlobalDefinitions");
  }

  Scope addScope(String name) {
    return addScope(name, new V1ScopeSpec());
  }

  Scope addScope(String name, V1ScopeSpec spec) {
    V1Scope data = new V1Scope().metadata(metadata(name, name)).spec(spec);
    Scope createdScope = named(new Scope(), name);
    createdScope.setData(data);
    scopesRepository.put(createdScope.id(), createdScope);
    return createdScope;
  }

  <T extends ModelElement> void addData(Class<T> modelClass, List<T> data) {
    dataRepository.getData().put(modelClass, data);
  }

  Component addComponent(String name) {
    Component component = component(name, SCOPE_NAME, false, false, false, List.of());
    return component;
  }

  static Component component(
      String name,
      String scopeName,
      boolean technical,
      boolean container,
      boolean hosting,
      List<String> parents) {
    V1ComponentSpec spec =
        new V1ComponentSpec()
            .isTechnical(technical)
            .isContainer(container)
            .isHosting(hosting)
            .parents(parents);
    return new Component(new V1Component().metadata(metadata(name, scopeName)).spec(spec));
  }

  static ComponentInstance componentInstance(
      String name, String scopeName, String landingZoneName, Component component) {
    V1ComponentInstance data =
        new V1ComponentInstance()
            .spec(new V1ComponentInstanceSpec().component(component.getName()));
    data.setMetadata(instanceMetadata(name, scopeName, landingZoneName));
    ComponentInstance componentInstance = new ComponentInstance(data);
    componentInstance.setComponent(component);
    return componentInstance;
  }

  static V1Metadata metadata(String name, String scopeName) {
    return new V1Metadata().name(name).scope(scopeName);
  }

  static V1MetadataInstance instanceMetadata(
      String name, String scopeName, String landingZoneName) {
    return new V1MetadataInstance().name(name).scope(scopeName).landingzone(landingZoneName);
  }

  static <T extends AbstractBase<?>> T named(T entity, String name) {
    entity.setUniqueIdFrom(name);
    entity.setName(name);
    return entity;
  }
}

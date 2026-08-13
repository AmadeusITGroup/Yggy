package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.DependenciesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.Dependency;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.amadeus.aud.libraries.yaml.models.V1DependencySpec;
import io.quarkus.logging.Log;
import jakarta.inject.Singleton;

@Singleton
public class DependencyService extends BaseService<Dependency, V1Dependency> {

  @Override
  protected DependenciesRepository getRepository(Version version) {
    DependenciesRepository dependenciesRepository = new DependenciesRepository();
    version.setDependenciesRepository(dependenciesRepository);
    return dependenciesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    ComponentsRepository componentsRepository = version.getComponentsRepository();
    if (componentsRepository == null) {
      Log.errorf("Component Repository not found for version %s", version);
      return;
    }

    init(
        version,
        Dependency.class,
        V1Dependency.class,
        (Dependency dependency, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addDependency(dependency);
          linkComponents(dependency, componentsRepository);
        });
  }

  private void linkComponents(Dependency dependency, ComponentsRepository componentsRepository) {
    V1DependencySpec spec = dependency.getData().getSpec();

    String sourceComponentName = spec.getComponent();
    if (sourceComponentName != null && !sourceComponentName.isBlank()) {
      Component sourceComponent = componentsRepository.get(sourceComponentName);
      if (sourceComponent == null) {
        logModelWarning(
            "Dependency %s references a non-existing Source Component %s",
            dependency.getName(), sourceComponentName);
      } else {
        dependency.setSourceComponent(sourceComponent);
        sourceComponent.addDependency(dependency);
      }
    }

    String destinationComponentName = spec.getTargetComponent();
    if (destinationComponentName != null && !destinationComponentName.isBlank()) {
      Component destinationComponent = componentsRepository.get(destinationComponentName);
      if (destinationComponent == null) {
        logModelWarning(
            "Dependency %s references a non-existing Destination Component %s",
            dependency.getName(), destinationComponentName);
      } else {
        dependency.setDestinationComponent(destinationComponent);
        destinationComponent.addDependency(dependency);
      }
    }

    dependency.setSecurityJustification(spec.getSecurityJustification());
  }
}

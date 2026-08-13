package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.AutomaticTemplateInstancesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ComponentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class AutomaticTemplateInstanceService
    extends BaseService<AutomaticTemplateInstance, V1ComponentInstance> {

  @Override
  protected AutomaticTemplateInstancesRepository getRepository(Version version) {
    AutomaticTemplateInstancesRepository automaticTemplateInstancesRepository =
        new AutomaticTemplateInstancesRepository();
    version.setAutomaticTemplateInstancesRepository(automaticTemplateInstancesRepository);
    return automaticTemplateInstancesRepository;
  }

  @LogExecutionTime
  public void initAutomaticTemplateInstances(
      Version version,
      List<ModelElement> automaticTemplateInstances,
      @Nullable Consumer<AutomaticTemplateInstance> callback) {

    LandingZonesRepository landingZonesRepository = version.getLandingZonesRepository();
    if (landingZonesRepository == null) {
      Log.errorf("LandingZone Repository not found for version %s", version);
      return;
    }

    ComponentsRepository componentsRepository = version.getComponentsRepository();
    if (componentsRepository == null) {
      Log.errorf("Component Repository not found for version %s", version);
      return;
    }

    init(
        version,
        AutomaticTemplateInstance.class,
        V1ComponentInstance.class,
        automaticTemplateInstances,
        (AutomaticTemplateInstance automaticTemplateInstance, Scope scope) -> {
          if (scope == null) {
            return;
          }

          scope.addAutomaticTemplateInstance(automaticTemplateInstance);

          V1ComponentInstance data = automaticTemplateInstance.getData();

          linkLandingZone(automaticTemplateInstance, data, landingZonesRepository);
          linkComponent(automaticTemplateInstance, data, componentsRepository);

          if (callback != null) {
            callback.accept(automaticTemplateInstance);
          }
        });
  }

  private void linkLandingZone(
      AutomaticTemplateInstance automaticTemplateInstance,
      V1ComponentInstance data,
      LandingZonesRepository landingZonesRepository) {

    String landingZoneId = data.getMetadata().getLandingzone();
    if (landingZoneId == null || landingZoneId.isEmpty()) {
      return;
    }

    LandingZone landingZone = landingZonesRepository.get(landingZoneId);
    if (landingZone == null) {
      logModelWarning(
          "AutomaticTemplateInstance %s references a non-existing LandingZone %s",
          automaticTemplateInstance.id(), landingZoneId);
      return;
    }

    automaticTemplateInstance.setLandingZone(landingZone);
    landingZone.addAutomaticTemplateInstance(automaticTemplateInstance);
  }

  private void linkComponent(
      AutomaticTemplateInstance automaticTemplateInstance,
      V1ComponentInstance data,
      ComponentsRepository componentsRepository) {

    String componentId = data.getSpec().getComponent();
    if (componentId == null || componentId.isEmpty()) {
      return;
    }

    Component component = componentsRepository.get(componentId);
    if (component == null) {
      logModelWarning(
          "AutomaticTemplateInstance %s references a non-existing Component %s",
          automaticTemplateInstance.id(), componentId);
      return;
    }

    automaticTemplateInstance.setComponent(component);
    component.addAutomaticTemplateInstance(automaticTemplateInstance);
  }
}

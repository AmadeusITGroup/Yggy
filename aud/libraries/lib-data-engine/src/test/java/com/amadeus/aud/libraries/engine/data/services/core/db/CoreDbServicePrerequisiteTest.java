package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import org.junit.jupiter.api.Test;

class CoreDbServicePrerequisiteTest {
  @Test
  void componentInstanceInitializationStopsWhenDataIsMissing() {
    Version version = new Version();
    AutomaticTemplateInstanceService automaticTemplateInstanceService =
        configure(new AutomaticTemplateInstanceService());
    ComponentInstanceService sut =
        configure(
            new ComponentInstanceService(
                automaticTemplateInstanceService, new EmptyComponentInstanceLogic()));

    assertThatCode(() -> sut.init(version)).doesNotThrowAnyException();

    assertThat(version.getComponentInstancesRepository()).isNull();
  }

  @Test
  void dependencyInstanceInitializationStopsWhenComponentsAreMissing() {
    Version version = new Version();
    DependencyInstanceService sut = configure(new DependencyInstanceService());

    assertThatCode(() -> sut.init(version)).doesNotThrowAnyException();

    assertThat(version.getDependencyInstancesRepository()).isNull();
  }

  @Test
  void landingZoneInitializationStopsWhenTargetsAreMissing() {
    Version version = new Version();
    LandingZoneService sut = configure(new LandingZoneService());

    assertThatCode(() -> sut.init(version)).doesNotThrowAnyException();

    assertThat(version.getLandingZonesRepository()).isNull();
  }

  @Test
  void subnetInitializationStopsWhenLandingZonesAreMissing() {
    Version version = new Version();
    SubnetInstanceService sut = configure(new SubnetInstanceService());

    assertThatCode(() -> sut.init(version)).doesNotThrowAnyException();

    assertThat(version.getSubnetInstancesRepository()).isNull();
  }

  @Test
  void targetInitializationStopsWhenScopesAreMissing() {
    Version version = new Version();
    TargetService sut = configure(new TargetService());

    assertThatCode(() -> sut.init(version)).doesNotThrowAnyException();

    assertThat(version.getTargetsRepository()).isNull();
  }

  @Test
  void zoneInitializationStopsWhenLandingZonesAreMissing() {
    Version version = new Version();
    ZoneInstanceService sut = configure(new ZoneInstanceService());

    assertThatCode(() -> sut.init(version)).doesNotThrowAnyException();

    assertThat(version.getZoneInstancesRepository()).isNull();
  }

  @Test
  void internalInitializationStopsWhenRepositoriesAreMissing() {
    Version version = new Version();
    ComponentInstanceService componentInstanceService =
        configure(
            new ComponentInstanceService(
                configure(new AutomaticTemplateInstanceService()),
                new EmptyComponentInstanceLogic()));
    LandingZoneService landingZoneService = configure(new LandingZoneService());
    TargetService targetService = configure(new TargetService());
    ZoneInstanceService zoneInstanceService = configure(new ZoneInstanceService());

    assertThatCode(
            () -> {
              componentInstanceService.initInternals(version);
              landingZoneService.initInternals(version);
              targetService.initInternals(version);
              zoneInstanceService.initInternals(version);
            })
        .doesNotThrowAnyException();

    assertThat(version.getRepositoryNames()).isEmpty();
  }
}

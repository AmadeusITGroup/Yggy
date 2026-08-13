package com.amadeus.aud.services.apis.services.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.BreakdownCount;
import com.amadeus.aud.libraries.backend.models.v2.InstanceRepartition;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Component;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class InstanceRepartitionServiceTest {

  @Mock private AudService audService;
  private InstanceRepartitionService service;

  @BeforeEach
  void setUp() {
    service = new InstanceRepartitionService(audService);
  }

  @Test
  void throwsNotFoundExceptionWhenEntityNotFound() {
    Version version = mock(Version.class);
    ConcurrentRepository<Component> componentRepo = mock(ConcurrentRepository.class);
    when(componentRepo.getData()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());
    when(version.getRepository(ObjectTypeListNamesEnum.COMPONENTS.getRepositoryKey()))
        .thenReturn((ConcurrentRepository) componentRepo);

    Throwable thrown =
        catchThrowable(
            () ->
                service.computeInstanceRepartition(
                    version, ObjectTypeListNamesEnum.COMPONENTS, "non-existent", "both"));

    assertThat(thrown).isInstanceOf(NotFoundException.class);
    assertThat(thrown.getMessage()).contains("Entity not found: non-existent");
  }

  @Test
  void throwsBadRequestExceptionWhenUnsupportedObjectType() {
    Version version = mock(Version.class);
    ConcurrentRepository<Target> targetRepo = mock(ConcurrentRepository.class);
    Target target = mock(Target.class);
    java.util.concurrent.ConcurrentMap<String, Target> data =
        new java.util.concurrent.ConcurrentHashMap<>();
    data.put("target-1", target);
    when(targetRepo.getData()).thenReturn(data);
    when(targetRepo.get("target-1")).thenReturn(target);
    when(version.getRepository(ObjectTypeListNamesEnum.TARGETS.getRepositoryKey()))
        .thenReturn((ConcurrentRepository) targetRepo);

    Throwable thrown =
        catchThrowable(
            () ->
                service.computeInstanceRepartition(
                    version, ObjectTypeListNamesEnum.TARGETS, "target-1", "both"));

    assertThat(thrown).isInstanceOf(BadRequestException.class);
    assertThat(thrown.getMessage()).contains("Instance repartition not supported for object type");
  }

  @Test
  void returnsCorrectAggregationsForComponents() {
    Version version = mock(Version.class);

    // Mock Component
    Component component = mock(Component.class);

    ConcurrentRepository<Component> componentRepo = mock(ConcurrentRepository.class);
    java.util.concurrent.ConcurrentMap<String, Component> data =
        new java.util.concurrent.ConcurrentHashMap<>();
    data.put("comp-1", component);
    when(componentRepo.getData()).thenReturn(data);
    when(componentRepo.get("comp-1")).thenReturn(component);
    when(version.getRepository(ObjectTypeListNamesEnum.COMPONENTS.getRepositoryKey()))
        .thenReturn((ConcurrentRepository) componentRepo);

    // Mock ComponentInstances
    ComponentInstance ci1 = mock(ComponentInstance.class);
    when(ci1.getNamespaceName()).thenReturn("ns-1");
    when(ci1.getLandingZoneName()).thenReturn("lz-1");
    when(ci1.getDeployComponentInstanceName()).thenReturn("paas-1");
    when(ci1.getInternalZoneTypeNames(Direction.OUT)).thenReturn("sz-1");

    LandingZone lz1 = mock(LandingZone.class);
    when(lz1.getMacroPhaseName()).thenReturn("phase-1");
    when(lz1.getTargetName()).thenReturn("target-1");
    Target target1 = mock(Target.class);
    when(target1.getContinentName()).thenReturn("europe");
    when(lz1.getTarget()).thenReturn(target1);
    when(ci1.getLandingZone()).thenReturn(lz1);

    ComponentInstance ci2 = mock(ComponentInstance.class);
    when(ci2.getNamespaceName()).thenReturn("ns-2");
    when(ci2.getLandingZoneName()).thenReturn("lz-2");
    when(ci2.getDeployComponentInstanceName()).thenReturn("paas-2");
    when(ci2.getInternalZoneTypeNames(Direction.OUT)).thenReturn("sz-2");

    LandingZone lz2 = mock(LandingZone.class);
    when(lz2.getMacroPhaseName()).thenReturn("phase-2");
    when(lz2.getTargetName()).thenReturn("target-2");
    Target target2 = mock(Target.class);
    when(target2.getContinentName()).thenReturn("americas");
    when(lz2.getTarget()).thenReturn(target2);
    when(ci2.getLandingZone()).thenReturn(lz2);

    when(component.getComponentInstances()).thenReturn(Set.of(ci1));
    when(component.getAllComponentInstances()).thenReturn(Set.of(ci1, ci2));

    // both
    InstanceRepartition responseBoth =
        service.computeInstanceRepartition(
            version, ObjectTypeListNamesEnum.COMPONENTS, "comp-1", "both");

    assertThat(responseBoth.total()).isEqualTo(2);
    assertThat(responseBoth.breakdowns().continent())
        .containsExactlyInAnyOrder(
            new BreakdownCount("europe", 1), new BreakdownCount("americas", 1));
    assertThat(responseBoth.breakdowns().macrophase())
        .containsExactlyInAnyOrder(
            new BreakdownCount("phase-1", 1), new BreakdownCount("phase-2", 1));

    // direct
    InstanceRepartition responseDirect =
        service.computeInstanceRepartition(
            version, ObjectTypeListNamesEnum.COMPONENTS, "comp-1", "direct");
    assertThat(responseDirect.total()).isEqualTo(1);
    assertThat(responseDirect.breakdowns().namespace())
        .containsExactly(new BreakdownCount("ns-1", 1));

    // indirect
    InstanceRepartition responseIndirect =
        service.computeInstanceRepartition(
            version, ObjectTypeListNamesEnum.COMPONENTS, "comp-1", "indirect");
    assertThat(responseIndirect.total()).isEqualTo(1);
    assertThat(responseIndirect.breakdowns().namespace())
        .containsExactly(new BreakdownCount("ns-2", 1));
  }

  @Test
  void throwsBadRequestExceptionWhenSelectorIsUnsupported() {
    Version version = mock(Version.class);
    Component component = mock(Component.class);

    java.util.concurrent.ConcurrentMap<String, Component> data =
        new java.util.concurrent.ConcurrentHashMap<>();
    data.put("comp-1", component);

    Throwable thrown =
        catchThrowable(
            () ->
                service.computeInstanceRepartition(
                    version, ObjectTypeListNamesEnum.COMPONENTS, "comp-1", "sideways"));

    assertThat(thrown).isInstanceOf(BadRequestException.class);
    assertThat(thrown.getMessage()).contains("Unsupported instance repartition selector");
  }

  @Test
  void getInstanceRepartition_returnsUni() {
    Version version = mock(Version.class);
    when(audService.getVersion("latest")).thenReturn(Uni.createFrom().item(version));

    ConcurrentRepository<Component> componentRepo = mock(ConcurrentRepository.class);
    when(componentRepo.getData()).thenReturn(new java.util.concurrent.ConcurrentHashMap<>());
    when(version.getRepository(ObjectTypeListNamesEnum.COMPONENTS.getRepositoryKey()))
        .thenReturn((ConcurrentRepository) componentRepo);

    Uni<InstanceRepartition> uni =
        service.getInstanceRepartition(
            "latest", ObjectTypeListNamesEnum.COMPONENTS, "comp-1", "both");

    Throwable thrown = catchThrowable(() -> uni.await().indefinitely());
    assertThat(thrown).isInstanceOf(NotFoundException.class);
  }
}

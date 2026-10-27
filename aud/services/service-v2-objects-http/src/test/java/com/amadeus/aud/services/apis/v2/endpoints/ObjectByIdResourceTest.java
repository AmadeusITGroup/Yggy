package com.amadeus.aud.services.apis.v2.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.ConnectivityDetailItem;
import com.amadeus.aud.libraries.backend.models.v2.InstanceBreakdowns;
import com.amadeus.aud.libraries.backend.models.v2.InstanceRepartition;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.v2.objects.service.ConnectivityDetailService;
import com.amadeus.aud.libraries.v2.objects.service.InstanceRepartitionService;
import com.amadeus.aud.libraries.v2.objects.service.ObjectByIdService;
import com.amadeus.aud.libraries.v2.objects.service.ReferencesService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ObjectByIdResourceTest {

  @Test
  void getConnectivityDetail_requiresSupportedObjectType() {
    AudService audService = mock(AudService.class);
    Version version = mock(Version.class);
    ConnectivityDetailService connectivityDetailService = mock(ConnectivityDetailService.class);
    ObjectByIdResource resource =
        new ObjectByIdResource(
            audService,
            connectivityDetailService,
            mock(ObjectByIdService.class),
            mock(ReferencesService.class),
            mock(InstanceRepartitionService.class));
    when(audService.getVersion("v1")).thenReturn(Uni.createFrom().item(version));
    when(connectivityDetailService.getConnectivityDetailResponse(
            eq(version), eq("unknowns"), eq("value"), eq("both"), eq(null), eq(1), eq(10)))
        .thenThrow(new BadRequestException("Unsupported connectivity object type: unknowns"));

    Throwable thrown =
        catchThrowable(
            () ->
                resource
                    .getConnectivityDetail("v1", "unknowns", "value", "both", null, 1, 10)
                    .await()
                    .indefinitely());

    assertThat(thrown)
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Unsupported connectivity object type");
  }

  @Test
  void getConnectivityDetail_usesObjectTypeDirectionAndPciParameters() {
    AudService audService = mock(AudService.class);
    Version version = mock(Version.class);
    ConnectivityDetailService connectivityDetailService = mock(ConnectivityDetailService.class);
    ObjectByIdResource resource =
        new ObjectByIdResource(
            audService,
            connectivityDetailService,
            mock(ObjectByIdService.class),
            mock(ReferencesService.class),
            mock(InstanceRepartitionService.class));
    when(audService.getVersion("v1")).thenReturn(Uni.createFrom().item(version));
    when(connectivityDetailService.getConnectivityDetailResponse(
            eq(version), eq("targets"), eq("target-a"), eq("out"), eq(true), eq(1), eq(10)))
        .thenReturn(
            Uni.createFrom()
                .item(new Response<List<ConnectivityDetailItem>>(List.of(), new Metadata())));

    resource
        .getConnectivityDetail("v1", "targets", "target-a", "out", true, 1, 10)
        .await()
        .indefinitely();

    verify(connectivityDetailService)
        .getConnectivityDetailResponse(version, "targets", "target-a", "out", true, 1, 10);
  }

  @Test
  void getInstanceRepartition_delegatesToService() {
    AudService audService = mock(AudService.class);
    Version version = mock(Version.class);
    InstanceRepartitionService instanceRepartitionService = mock(InstanceRepartitionService.class);
    ObjectByIdResource resource =
        new ObjectByIdResource(
            audService,
            mock(ConnectivityDetailService.class),
            mock(ObjectByIdService.class),
            mock(ReferencesService.class),
            instanceRepartitionService);
    InstanceRepartition responseMock =
        new InstanceRepartition(
            0,
            new InstanceBreakdowns(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()));
    when(audService.getVersion("v1")).thenReturn(Uni.createFrom().item(version));
    when(instanceRepartitionService.getInstanceRepartition(
            eq(version), eq(ObjectTypeListNamesEnum.COMPONENTS), eq("comp-1"), eq("both")))
        .thenReturn(Uni.createFrom().item(responseMock));

    resource.getInstanceRepartition("v1", "comp-1", "both").await().indefinitely();

    verify(instanceRepartitionService)
        .getInstanceRepartition(version, ObjectTypeListNamesEnum.COMPONENTS, "comp-1", "both");
  }
}

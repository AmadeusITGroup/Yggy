package com.amadeus.aud.services.apis.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.ConnectivityDetailItem;
import com.amadeus.aud.libraries.backend.models.v2.InstanceBreakdowns;
import com.amadeus.aud.libraries.backend.models.v2.InstanceRepartition;
import com.amadeus.aud.libraries.backend.models.v2.Metadata;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.services.apis.services.endpoints.v2.ConnectivityDetailService;
import com.amadeus.aud.services.apis.services.endpoints.v2.InstanceRepartitionService;
import com.amadeus.aud.services.apis.services.endpoints.v2.ObjectByIdService;
import com.amadeus.aud.services.apis.services.endpoints.v2.ReferencesService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.SAME_THREAD)
class ObjectByIdResourceTest {

  @Test
  void getConnectivityDetail_requiresSupportedObjectType() {
    ConnectivityDetailService connectivityDetailService = mock(ConnectivityDetailService.class);
    ObjectByIdResource resource =
        new ObjectByIdResource(
            connectivityDetailService,
            mock(ObjectByIdService.class),
            mock(ReferencesService.class),
            mock(InstanceRepartitionService.class));
    when(connectivityDetailService.getConnectivityDetailResponse(
            eq("v1"), eq("unknowns"), eq("value"), eq("both"), eq(null), eq(1), eq(10)))
        .thenThrow(new BadRequestException("Unsupported connectivity object type: unknowns"));

    Throwable thrown =
        catchThrowable(
            () -> resource.getConnectivityDetail("v1", "unknowns", "value", "both", null, 1, 10));

    assertThat(thrown).isNotNull();
    if (thrown == null) {
      return;
    }
    Throwable actualThrown = Objects.requireNonNull(thrown);

    assertThat(actualThrown).isInstanceOf(BadRequestException.class);
    assertThat(Objects.requireNonNull(actualThrown.getMessage()))
        .contains("Unsupported connectivity object type");
  }

  @Test
  void getConnectivityDetail_usesObjectTypeDirectionAndPciParameters() {
    ConnectivityDetailService connectivityDetailService = mock(ConnectivityDetailService.class);
    ObjectByIdResource resource =
        new ObjectByIdResource(
            connectivityDetailService,
            mock(ObjectByIdService.class),
            mock(ReferencesService.class),
            mock(InstanceRepartitionService.class));
    when(connectivityDetailService.getConnectivityDetailResponse(
            eq("v1"), eq("targets"), eq("target-a"), eq("out"), eq(true), eq(1), eq(10)))
        .thenReturn(
            Uni.createFrom()
                .item(new Response<List<ConnectivityDetailItem>>(List.of(), new Metadata())));

    resource.getConnectivityDetail("v1", "targets", "target-a", "out", true, 1, 10);

    verify(connectivityDetailService)
        .getConnectivityDetailResponse("v1", "targets", "target-a", "out", true, 1, 10);
  }

  @Test
  void getInstanceRepartition_delegatesToService() {
    InstanceRepartitionService instanceRepartitionService = mock(InstanceRepartitionService.class);
    ObjectByIdResource resource =
        new ObjectByIdResource(
            mock(ConnectivityDetailService.class),
            mock(ObjectByIdService.class),
            mock(ReferencesService.class),
            instanceRepartitionService);
    InstanceRepartition responseMock =
        new InstanceRepartition(
            0,
            new InstanceBreakdowns(
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of()));
    when(instanceRepartitionService.getInstanceRepartition(
            eq("v1"), eq(ObjectTypeListNamesEnum.COMPONENTS), eq("comp-1"), eq("both")))
        .thenReturn(Uni.createFrom().item(responseMock));

    resource.getInstanceRepartition("v1", "comp-1", "both");

    verify(instanceRepartitionService)
        .getInstanceRepartition("v1", ObjectTypeListNamesEnum.COMPONENTS, "comp-1", "both");
  }
}

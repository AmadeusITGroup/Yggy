package com.amadeus.aud.libraries.v2.objects.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.engine.data.AudVersionsService;
import com.amadeus.aud.libraries.engine.data.models.core.internals.SingleVersion;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("VersionService Tests")
class VersionServiceTest {
  @Mock private AudVersionsService audVersionsService;
  private VersionService sut;

  @BeforeEach
  void setUp() {
    sut = new VersionService(audVersionsService);
  }

  @Test
  @DisplayName("should get all available versions from external service")
  void getAllAvailableVersionsResponse() {
    SingleVersion version1 = new SingleVersion("1.0.0", null, 0);
    SingleVersion version2 = new SingleVersion("1.5.0", null, 0);
    SingleVersion version3 = new SingleVersion("2.0.0", null, 0);

    when(audVersionsService.listAllVersions())
        .thenReturn(Uni.createFrom().item(List.of(version1, version2, version3)));

    Response<List<SingleVersion>> body =
        sut.getAllAvailableVersionsResponse().await().indefinitely();

    assertThat(body).isNotNull();
    assertThat(body.data()).containsExactly(version1, version2, version3);
    assertThat(body.metadata()).isNull();
  }
}

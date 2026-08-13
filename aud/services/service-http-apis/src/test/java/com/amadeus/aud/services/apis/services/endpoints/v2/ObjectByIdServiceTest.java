package com.amadeus.aud.services.apis.services.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.aud.AudService;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import jakarta.ws.rs.NotFoundException;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@MockitoSettings(strictness = Strictness.LENIENT)
class ObjectByIdServiceTest {

  @Mock private AudService audService;
  @Mock private ObjectUtils objectUtils;

  private ObjectByIdService service;

  @BeforeEach
  void setUp() {
    service = new ObjectByIdService(audService, objectUtils);
  }

  @Test
  @DisplayName("should throw NotFoundException when object id not found")
  void testGetObjectByIdResponseWithInvalidId() {
    Version version = mock(Version.class);

    @SuppressWarnings("unchecked")
    ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
    when(repository.getData()).thenReturn(new ConcurrentHashMap<>());
    when(version.getRepository("service")).thenReturn(repository);

    Throwable thrown =
        catchThrowable(
            () ->
                service
                    .getObjectByIdResponse(version, "services", "invalid-id", false)
                    .await()
                    .indefinitely());
    assertThat(thrown).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("should return response when object id is found")
  void testGetObjectByIdResponseWithValidId() {
    Version version = mock(Version.class);

    BomElement element = mock(BomElement.class);
    when(element.id()).thenReturn("test-id");

    ConcurrentHashMap<String, BomElement> data = new ConcurrentHashMap<>();
    data.put("test-id", element);

    @SuppressWarnings("unchecked")
    ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
    when(repository.getData()).thenReturn(data);
    when(version.getRepository("service")).thenReturn(repository);

    Response<Object> response =
        service.getObjectByIdResponse(version, "services", "test-id", false).await().indefinitely();

    assertThat(response).isNotNull();
  }
}

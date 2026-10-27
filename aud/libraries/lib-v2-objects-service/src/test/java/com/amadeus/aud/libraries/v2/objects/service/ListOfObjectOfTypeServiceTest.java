package com.amadeus.aud.libraries.v2.objects.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.backend.models.v2.Pagination;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("ListOfObjectOfTypeService Tests")
class ListOfObjectOfTypeServiceTest {

  private ListOfObjectOfTypeService service;

  @BeforeEach
  void setUp() {
    service = new ListOfObjectOfTypeService();
  }

  @Nested
  @DisplayName("Object Type Validation")
  class ObjectTypeValidationTests {

    @Test
    @DisplayName("should throw NotFoundException when object type is invalid")
    void getObjectTypeResponseWithInvalidObjectType() {
      Version version = mock(Version.class);

      Throwable thrown =
          catchThrowable(
              () ->
                  service
                      .getObjectTypeResponse(version, "invalid", null, null)
                      .await()
                      .indefinitely());
      assertThat(thrown).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("should accept valid object types")
    void getObjectTypeResponseWithValidObjectType() {
      Version version = mock(Version.class);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
      when(repository.getData()).thenReturn(new ConcurrentHashMap<>());
      when(version.getRepository("component")).thenReturn(repository);

      Response<?> response =
          service.getObjectTypeResponse(version, "components", null, null).await().indefinitely();

      assertThat(response).isNotNull();
    }
  }

  @Nested
  @DisplayName("Pagination Validation")
  class PaginationValidationTests {

    @Test
    @DisplayName("should reject pageSize < 1")
    void getObjectTypeResponseWithPageSizeZero() {
      Version version = createVersionWithUnidentifiedElements(10);

      Throwable thrown =
          catchThrowable(
              () ->
                  service
                      .getObjectTypeResponse(version, "components", 1, 0)
                      .await()
                      .indefinitely());

      assertThat(thrown).isInstanceOf(BadRequestException.class);
      assertThat(thrown.getMessage()).contains("pageSize must be >= 1");
    }

    @Test
    @DisplayName("should reject pageSize > MAX_PAGE_SIZE")
    void getObjectTypeResponseWithPageSizeExceeded() {
      Version version = createVersionWithUnidentifiedElements(10);

      Throwable thrown =
          catchThrowable(
              () ->
                  service
                      .getObjectTypeResponse(version, "components", 1, 10001)
                      .await()
                      .indefinitely());

      assertThat(thrown).isInstanceOf(BadRequestException.class);
      assertThat(thrown.getMessage()).contains("pageSize must be <=");
    }

    @Test
    @DisplayName("should reject pageNum < 1")
    void getObjectTypeResponseWithPageNumZero() {
      Version version = createVersionWithUnidentifiedElements(10);

      Throwable thrown =
          catchThrowable(
              () ->
                  service
                      .getObjectTypeResponse(version, "components", 0, 10)
                      .await()
                      .indefinitely());

      assertThat(thrown).isInstanceOf(BadRequestException.class);
      assertThat(thrown.getMessage()).contains("pageNum must be >= 1");
    }

    @Test
    @DisplayName("should reject pageNum exceeding lastPage")
    void getObjectTypeResponseWithPageNumExceeded() {
      Version version = createVersionWithElements(5);

      Throwable thrown =
          catchThrowable(
              () ->
                  service
                      .getObjectTypeResponse(version, "components", 2, 10)
                      .await()
                      .indefinitely());

      assertThat(thrown).isInstanceOf(BadRequestException.class);
      assertThat(thrown.getMessage()).contains("pageNum");
      assertThat(thrown.getMessage()).contains("exceeds last page");
    }
  }

  @Nested
  @DisplayName("Pagination Functionality")
  class PaginationFunctionalityTests {

    @Test
    @DisplayName("should apply default pageSize when null")
    void getObjectTypeResponseWithDefaultPageSize() {
      Version version = createVersionWithElements(5);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", null, null).await().indefinitely();

      assertThat(response).isNotNull();
      assertThat(response.metadata()).isNotNull();
      Pagination pagination = response.metadata().pagination();
      assertThat(pagination).isNotNull();
      assertThat(pagination.pageSize()).isPositive();
    }

    @Test
    @DisplayName("should apply default pageNum when null")
    void getObjectTypeResponseWithDefaultPageNum() {
      Version version = createVersionWithElements(50);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", null, 10).await().indefinitely();

      assertThat(response).isNotNull();
      assertThat(response.metadata().pagination().pageNum()).isOne();
    }

    @Test
    @DisplayName("should return correct items for page 1")
    void getObjectTypeResponsePage1() {
      Version version = createVersionWithElements(25);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", 1, 10).await().indefinitely();

      assertThat(response.data()).hasSize(10);
      assertThat(response.metadata().pagination().count()).isEqualTo(25);
      assertThat(response.metadata().pagination().pageNum()).isOne();
      assertThat(response.metadata().pagination().pageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("should return correct items for page 2")
    void getObjectTypeResponsePage2() {
      Version version = createVersionWithElements(25);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", 2, 10).await().indefinitely();

      assertThat(response.data()).hasSize(10);
      assertThat(response.metadata().pagination().pageNum()).isEqualTo(2);
    }

    @Test
    @DisplayName("should return remaining items on last page")
    void getObjectTypeResponseLastPage() {
      Version version = createVersionWithElements(25);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", 3, 10).await().indefinitely();

      assertThat(response.data()).hasSize(5);
      assertThat(response.metadata().pagination().pageNum()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("Edge Cases")
  class EdgeCasesTests {

    @Test
    void getObjectTypeResponseThrowsWhenRepositoryIsMissing() {
      Version version = mock(Version.class);

      Throwable thrown =
          catchThrowable(
              () ->
                  service
                      .getObjectTypeResponse(version, "components", 1, 10)
                      .await()
                      .indefinitely());

      assertThat(thrown)
          .isInstanceOf(NotFoundException.class)
          .hasMessage("No data for object type: components");
    }

    @Test
    void getObjectTypeResponseTreatsNullRepositoryDataAsEmpty() {
      Version version = mock(Version.class);
      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
      when(version.<BomElement>getRepository("component")).thenReturn(repository);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", 1, 10).await().indefinitely();

      assertThat(response.data()).isEmpty();
      assertThat(response.metadata().pagination()).isEqualTo(new Pagination(0, 1, 10));
    }

    @Test
    @DisplayName("should handle empty data")
    void getObjectTypeResponseWithEmptyData() {
      Version version = mock(Version.class);

      @SuppressWarnings("unchecked")
      ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);
      when(repository.getData()).thenReturn(new ConcurrentHashMap<>());
      when(version.getRepository("component")).thenReturn(repository);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", 1, 10).await().indefinitely();

      assertThat(response.data()).isEmpty();
      assertThat(response.metadata().pagination().count()).isZero();
    }

    @Test
    @DisplayName("should handle single item")
    void getObjectTypeResponseWithSingleItem() {
      Version version = createVersionWithUnidentifiedElements(1);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", 1, 10).await().indefinitely();

      assertThat(response.data()).hasSize(1);
      assertThat(response.metadata().pagination().count()).isOne();
    }

    @Test
    @DisplayName("should sort items by ID")
    void getObjectTypeResponseSortsByID() {
      Version version = createVersionWithElements(3);

      Response<Collection<BomElement>> response =
          service.getObjectTypeResponse(version, "components", 1, 10).await().indefinitely();

      List<String> ids = new ArrayList<>();
      for (BomElement element : response.data()) {
        ids.add(element.id());
      }

      assertThat(ids).isSorted();
    }
  }

  private Version createVersionWithElements(int count) {
    return createVersionWithElements(count, true);
  }

  private Version createVersionWithUnidentifiedElements(int count) {
    return createVersionWithElements(count, false);
  }

  private Version createVersionWithElements(int count, boolean stubElementIds) {
    Version version = mock(Version.class);

    @SuppressWarnings("unchecked")
    ConcurrentRepository<BomElement> repository = mock(ConcurrentRepository.class);

    ConcurrentHashMap<String, BomElement> data = new ConcurrentHashMap<>();

    for (int i = 0; i < count; i++) {
      String id = String.format("component-%03d", i);
      BomElement element = mock(BomElement.class);
      if (stubElementIds) {
        when(element.id()).thenReturn(id);
      }
      data.put(id, element);
    }

    when(repository.getData()).thenReturn(data);
    when(version.getRepository("component")).thenReturn(repository);

    return version;
  }
}

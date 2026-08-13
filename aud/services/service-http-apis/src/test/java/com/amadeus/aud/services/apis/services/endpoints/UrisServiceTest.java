package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import com.amadeus.aud.libraries.backend.services.v2.UriFromObjectType;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("UrisService Tests")
class UrisServiceTest {

  @Mock private UriFromObjectType uriFromObjectType;

  private UrisService service;

  @BeforeEach
  void setUp() throws Exception {
    service = new UrisService(uriFromObjectType);
  }

  @Test
  @DisplayName("should return URIs with correct structure and categories")
  void testGetUrisReturnsFormattedUriList() throws Exception {
    UriItem uri1 =
        new UriItem(
            "Components",
            "/v2/versions/latest/data/components",
            "05. Application",
            "componentsQuery");
    UriItem uri2 =
        new UriItem(
            "Dependencies",
            "/v2/versions/latest/data/dependencies",
            "05. Application",
            "dependenciesQuery");
    UriItem uri3 =
        new UriItem("Network", "/v2/versions/latest/data/network", "03. Network", "networkQuery");

    List<UriItem> uriItems = List.of(uri1, uri2, uri3);
    Response<List<UriItem>> mockResponse = new Response<>(uriItems, null);

    when(uriFromObjectType.getUris()).thenReturn(Uni.createFrom().item(mockResponse));

    jakarta.ws.rs.core.Response response = service.getUris().await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Response<List<UriItem>> body = (Response<List<UriItem>>) response.getEntity();

    assertThat(body).isNotNull();
    assertThat(body.data()).isNotNull();
    assertThat(body.data()).hasSize(3);

    assertThat(body.data())
        .extracting(UriItem::title)
        .containsExactly("Components", "Dependencies", "Network");

    assertThat(body.data())
        .extracting(UriItem::uri)
        .containsExactly(
            "/v2/versions/latest/data/components",
            "/v2/versions/latest/data/dependencies",
            "/v2/versions/latest/data/network");

    assertThat(body.data())
        .extracting(UriItem::category)
        .containsExactly("05. Application", "05. Application", "03. Network");
  }

  @Test
  @DisplayName("should handle empty URI list gracefully")
  void testGetUrisWithEmptyList() throws Exception {
    Response<List<UriItem>> mockResponse = new Response<>(List.of(), null);

    when(uriFromObjectType.getUris()).thenReturn(Uni.createFrom().item(mockResponse));

    jakarta.ws.rs.core.Response response = service.getUris().await().indefinitely();

    assertThat(response.getStatus()).isEqualTo(200);

    @SuppressWarnings("unchecked")
    Response<List<UriItem>> body = (Response<List<UriItem>>) response.getEntity();

    assertThat(body.data()).isEmpty();
  }
}

package com.amadeus.aud.services.apis.v2.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.v2.objects.service.ConnectivityParquetService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConnectivityParquetResource Unit Tests")
class ConnectivityParquetResourceTest {

  private ConnectivityParquetService parquetService;
  private ConnectivityParquetResource sut;

  @BeforeEach
  void setUp() {
    parquetService = mock(ConnectivityParquetService.class);
    sut = new ConnectivityParquetResource(parquetService);
  }

  @Test
  @DisplayName("Should return 307 Temporary Redirect for file.parquet endpoint")
  void redirectsToFileParquetSasUrl() {
    // Arrange
    String url =
        "https://storage.blob.core.windows.net/aud-version/latest_links.parquet?sv=2020-08-04";
    OffsetDateTime expiry = OffsetDateTime.now().plusMinutes(60);
    ConnectivityParquetService.SasUrlInfo sasUrlInfo =
        new ConnectivityParquetService.SasUrlInfo(url, expiry);
    when(parquetService.getSasUrlInfo()).thenReturn(sasUrlInfo);

    // Act
    Uni<Response> responseUni = sut.getParquetFileRedirect();
    Response response = responseUni.await().indefinitely();

    // Assert
    assertThat(response.getStatus()).isEqualTo(307);
    assertThat(response.getLocation()).isEqualTo(URI.create(url));
    assertThat(response.getHeaderString("Cache-Control")).startsWith("private, max-age=");
    verify(parquetService).getSasUrlInfo();
  }
}

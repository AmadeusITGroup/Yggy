package com.amadeus.aud.libraries.v2.objects.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import jakarta.ws.rs.InternalServerErrorException;
import java.net.URI;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@DisplayName("ConnectivityParquetService Unit Tests")
class ConnectivityParquetServiceTest {

  private AudDataConfig audDataConfig;
  private BlobServiceAsyncClient blobServiceAsyncClient;
  private BlobAsyncClient blobClient;
  private ConnectivityParquetService sut;

  @BeforeEach
  void setUp() {
    audDataConfig = mock(AudDataConfig.class);
    AudDataConfig.CloudStorageConfig cloudStorageConfig =
        mock(AudDataConfig.CloudStorageConfig.class);
    when(audDataConfig.cloudStorage()).thenReturn(cloudStorageConfig);
    when(cloudStorageConfig.versionContainerName()).thenReturn("aud-version");

    blobServiceAsyncClient = mock(BlobServiceAsyncClient.class);
    BlobContainerAsyncClient containerClient = mock(BlobContainerAsyncClient.class);
    blobClient = mock(BlobAsyncClient.class);

    when(blobServiceAsyncClient.getBlobContainerAsyncClient("aud-version"))
        .thenReturn(containerClient);
    when(containerClient.getBlobAsyncClient("latest_links.parquet")).thenReturn(blobClient);
    when(blobClient.getBlobUrl())
        .thenReturn("https://storage.blob.core.windows.net/aud-version/latest_links.parquet");

    UserDelegationKey key = mock(UserDelegationKey.class);
    when(blobServiceAsyncClient.getUserDelegationKey(
            any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(Mono.just(key));
    when(blobClient.generateUserDelegationSas(
            any(BlobServiceSasSignatureValues.class), any(UserDelegationKey.class)))
        .thenReturn("sv=2020-08-04&sig=mock-signature");

    sut = new ConnectivityParquetService(audDataConfig, blobServiceAsyncClient);
  }

  @Test
  @DisplayName("Should generate valid SAS URL info with token and future expiry")
  void generatesValidSasUrlInfo() {
    // Act
    ConnectivityParquetService.SasUrlInfo info = sut.getSasUrlInfo();

    // Assert
    assertThat(info).isNotNull();
    assertThat(info.url())
        .isEqualTo(
            "https://storage.blob.core.windows.net/aud-version/latest_links.parquet?sv=2020-08-04&sig=mock-signature");
    assertThat(info.expiryTime()).isAfter(OffsetDateTime.now());
  }

  @Test
  @DisplayName("Should return URI for SAS URL")
  void returnsSasUri() {
    // Act
    URI uri = sut.getSasUri();

    // Assert
    assertThat(uri).isNotNull();
    assertThat(uri.toString())
        .isEqualTo(
            "https://storage.blob.core.windows.net/aud-version/latest_links.parquet?sv=2020-08-04&sig=mock-signature");
  }

  @Test
  @DisplayName("Should reuse cached SAS URL when called repeatedly within validity window")
  void reusesCachedSasUrl() {
    // Act
    ConnectivityParquetService.SasUrlInfo first = sut.getSasUrlInfo();
    ConnectivityParquetService.SasUrlInfo second = sut.getSasUrlInfo();

    // Assert
    assertThat(second.url()).isEqualTo(first.url());
    assertThat(second.expiryTime()).isEqualTo(first.expiryTime());
    verify(blobServiceAsyncClient, times(1))
        .getUserDelegationKey(any(OffsetDateTime.class), any(OffsetDateTime.class));
  }

  @Test
  @DisplayName("Should throw InternalServerErrorException when generated SAS token is blank")
  void throwsInternalServerErrorWhenSasTokenIsBlank() {
    // Arrange
    when(blobClient.generateUserDelegationSas(
            any(BlobServiceSasSignatureValues.class), any(UserDelegationKey.class)))
        .thenReturn("");

    // Act & Assert
    InternalServerErrorException exception =
        assertThrows(InternalServerErrorException.class, () -> sut.getSasUrlInfo());
    assertThat(exception.getMessage()).contains("Failed to generate Azure SAS URL");
    Throwable cause = exception.getCause();
    assertThat(cause).isInstanceOf(IllegalStateException.class);
    assertThat(cause.getMessage()).contains("Generated Azure SAS token is null or blank");
  }

  @Test
  @DisplayName("Should throw InternalServerErrorException when generated SAS token is null")
  void throwsInternalServerErrorWhenSasTokenIsNull() {
    // Arrange
    when(blobClient.generateUserDelegationSas(
            any(BlobServiceSasSignatureValues.class), any(UserDelegationKey.class)))
        .thenReturn(null);

    // Act & Assert
    InternalServerErrorException exception =
        assertThrows(InternalServerErrorException.class, () -> sut.getSasUrlInfo());
    assertThat(exception.getMessage()).contains("Failed to generate Azure SAS URL");
    Throwable cause = exception.getCause();
    assertThat(cause).isInstanceOf(IllegalStateException.class);
    assertThat(cause.getMessage()).contains("Generated Azure SAS token is null or blank");
  }

  @Test
  @DisplayName("Should throw InternalServerErrorException when Azure key generation fails")
  void throwsInternalServerErrorWhenAzureFails() {
    // Arrange
    when(blobServiceAsyncClient.getUserDelegationKey(
            any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(Mono.error(new RuntimeException("Azure network error")));

    // Act & Assert
    InternalServerErrorException exception =
        assertThrows(InternalServerErrorException.class, () -> sut.getSasUrlInfo());
    assertThat(exception.getMessage()).contains("Failed to generate Azure SAS URL");
  }
}

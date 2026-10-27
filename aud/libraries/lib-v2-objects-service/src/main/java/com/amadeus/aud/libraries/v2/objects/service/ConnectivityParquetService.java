package com.amadeus.aud.libraries.v2.objects.service;

import com.amadeus.aud.libraries.engine.data.configs.AudDataConfig;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;

@ApplicationScoped
public class ConnectivityParquetService {

  private static final String PARQUET_BLOB_NAME = "latest_links.parquet";
  private static final Duration SAS_VALIDITY_DURATION = Duration.ofMinutes(60);
  private static final Duration SAS_REFRESH_MARGIN = Duration.ofMinutes(5);

  private final AudDataConfig audDataConfig;
  private final BlobServiceAsyncClient blobServiceAsyncClient;

  private String cachedSasUrl;
  private OffsetDateTime sasExpiryTime;

  @Inject
  public ConnectivityParquetService(
      AudDataConfig audDataConfig, BlobServiceAsyncClient blobServiceAsyncClient) {
    this.audDataConfig = audDataConfig;
    this.blobServiceAsyncClient = blobServiceAsyncClient;
  }

  public record SasUrlInfo(String url, OffsetDateTime expiryTime) {}

  public synchronized SasUrlInfo getSasUrlInfo() {
    OffsetDateTime now = OffsetDateTime.now();
    if (cachedSasUrl != null
        && sasExpiryTime != null
        && now.isBefore(sasExpiryTime.minus(SAS_REFRESH_MARGIN))) {
      return new SasUrlInfo(cachedSasUrl, sasExpiryTime);
    }

    try {
      OffsetDateTime expiry = now.plus(SAS_VALIDITY_DURATION);
      String container = audDataConfig.cloudStorage().versionContainerName();
      BlobAsyncClient blobClient =
          blobServiceAsyncClient
              .getBlobContainerAsyncClient(container)
              .getBlobAsyncClient(PARQUET_BLOB_NAME);

      UserDelegationKey userDelegationKey =
          blobServiceAsyncClient.getUserDelegationKey(now.minusMinutes(5), expiry).block();

      BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
      BlobServiceSasSignatureValues sasValues =
          new BlobServiceSasSignatureValues(expiry, permission).setStartTime(now.minusMinutes(5));

      String sasToken = blobClient.generateUserDelegationSas(sasValues, userDelegationKey);
      if (sasToken == null || sasToken.isBlank()) {
        throw new IllegalStateException("Generated Azure SAS token is null or blank");
      }
      String fullUrl = blobClient.getBlobUrl() + "?" + sasToken;

      this.cachedSasUrl = fullUrl;
      this.sasExpiryTime = expiry;
      return new SasUrlInfo(fullUrl, expiry);
    } catch (Exception e) {
      Log.error("Failed to generate Azure SAS URL for connectivity parquet", e);
      throw new InternalServerErrorException("Failed to generate Azure SAS URL", e);
    }
  }

  public URI getSasUri() {
    return URI.create(getSasUrlInfo().url());
  }
}

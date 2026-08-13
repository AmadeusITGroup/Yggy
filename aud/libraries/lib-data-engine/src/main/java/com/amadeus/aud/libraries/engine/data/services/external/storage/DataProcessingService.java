package com.amadeus.aud.libraries.engine.data.services.external.storage;

import com.amadeus.aud.libraries.engine.data.repositories.base.BaseRepository;
import com.amadeus.aud.libraries.engine.data.utils.HashUtils;
import io.quarkus.logging.Log;
import java.security.NoSuchAlgorithmException;

/** Service for processing data, including hash calculation and size measurement. */
public class DataProcessingService {

  /**
   * Calculate hash values for a set of responses using standard Java libraries.
   *
   * @param repository The repository to store the hash values in
   * @param responses The responses to hash
   */
  public void calculateHashes(BaseRepository<?> repository, String[] responses) {
    if (responses.length > 0) {
      try {
        String responseMD5Hash = HashUtils.calculateCombinedMD5Hash(responses);
        repository.setDataMD5Hash(responseMD5Hash);
        Log.debugf("MD5 of the combined responses is %s", responseMD5Hash);

        String responseSHA256Hash = HashUtils.calculateCombinedSHA256Hash(responses);
        repository.setDataSHA256Hash(responseSHA256Hash);
        Log.debugf("SHA256 of the combined responses is %s", responseSHA256Hash);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Hash algorithm not found", e);
      }
    }
  }
}

package com.amadeus.aud.libraries.engine.data.services.external.storage;

import java.io.IOException;

public interface LocalFileDataProcessor<R> extends DataProcessor<R> {

  /**
   * Process the response and store it in the repository.
   *
   * @param tagDisplayId The version identifier
   * @param responses The response strings to process
   * @return The repository containing the processed data
   * @throws IOException If an error occurs during processing
   */
  R processResponses(String tagDisplayId, String[] responses, boolean jsonPreferred)
      throws IOException;
}

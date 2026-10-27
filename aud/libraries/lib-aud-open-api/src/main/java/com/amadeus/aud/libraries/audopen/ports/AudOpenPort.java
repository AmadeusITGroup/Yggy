package com.amadeus.aud.libraries.audopen.ports;

import com.amadeus.aud.libraries.audopen.models.OpenResponse;
import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import io.smallrye.mutiny.Uni;

public interface AudOpenPort {
  Uni<OpenResponse> get(
      Version version,
      String source,
      String destination,
      String requestUri,
      String versionRequestUri);
}

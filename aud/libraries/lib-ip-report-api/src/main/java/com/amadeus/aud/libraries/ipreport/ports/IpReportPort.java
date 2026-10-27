package com.amadeus.aud.libraries.ipreport.ports;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.ipreport.models.IpResponse;
import io.smallrye.mutiny.Uni;

public interface IpReportPort {
  Uni<IpResponse> get(Version version, String ipStr, String requestUri, String versionRequestUri);
}

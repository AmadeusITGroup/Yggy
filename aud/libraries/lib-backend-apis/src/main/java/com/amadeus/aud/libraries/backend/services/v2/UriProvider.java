package com.amadeus.aud.libraries.backend.services.v2;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface UriProvider {
  Uni<Response<List<UriItem>>> getUris();
}

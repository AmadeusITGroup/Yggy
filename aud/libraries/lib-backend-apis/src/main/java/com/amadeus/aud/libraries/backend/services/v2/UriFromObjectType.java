package com.amadeus.aud.libraries.backend.services.v2;

import com.amadeus.aud.libraries.backend.AmadeusConstants;
import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import com.amadeus.aud.libraries.yaml.models.Constants;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class UriFromObjectType implements UriProvider {

  private final Response<List<UriItem>> cachedResponse;

  @Inject
  public UriFromObjectType() {
    this.cachedResponse = buildUrisFromEnum();
  }

  private static Response<List<UriItem>> buildUrisFromEnum() {
    List<UriItem> uriItems = new ArrayList<>();

    Map<String, Integer> categoryNumbers = new LinkedHashMap<>();
    categoryNumbers.put(AmadeusConstants.GOVERNANCE, 1);
    categoryNumbers.put(AmadeusConstants.SECURITY_OFFICE, 2);
    categoryNumbers.put(Constants.NETWORK, 3);
    categoryNumbers.put(Constants.FIREWALL, 4);
    categoryNumbers.put(Constants.APPLICATION, 5);
    categoryNumbers.put(AmadeusConstants.LINKS, 6);

    for (ObjectTypeListNamesEnum objectType : ObjectTypeListNamesEnum.values()) {
      if (objectType == ObjectTypeListNamesEnum.SCOPERELEASENOTES) {
        continue;
      }
      String title = objectType.getTitle();
      String uri = "/v2/versions/latest/data/" + objectType.getUri();
      String category = objectType.getCategory();
      Integer categoryNumber = categoryNumbers.get(category);
      String categoryWithNumber =
          categoryNumber != null ? String.format("%02d. %s", categoryNumber, category) : category;
      String queryName = objectType.graphQLQueryName();

      uriItems.add(new UriItem(title, uri, categoryWithNumber, queryName));
    }

    return new Response<>(uriItems, null);
  }

  @Override
  public Uni<Response<List<UriItem>>> getUris() {
    return Uni.createFrom()
        .item(() -> cachedResponse)
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }
}

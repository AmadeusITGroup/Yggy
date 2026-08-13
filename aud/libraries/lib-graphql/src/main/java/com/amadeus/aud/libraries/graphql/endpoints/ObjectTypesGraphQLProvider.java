package com.amadeus.aud.libraries.graphql.endpoints;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ObjectTypesGraphQLProvider {

  @Timeout(value = 30, unit = ChronoUnit.SECONDS)
  @Query("objectTypes")
  public List<String> objectTypes() {
    return Arrays.stream(ObjectTypeListNamesEnum.values())
        .map(ObjectTypeListNamesEnum::getUri)
        .toList();
  }
}

package com.amadeus.aud.services.apis.services.endpoints.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Display;
import com.amadeus.aud.libraries.backend.models.v2.Link;
import com.amadeus.aud.libraries.backend.models.v2.UriLink;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import java.util.List;
import org.junit.jupiter.api.Test;

class ObjectUtilsTest {
  private final ObjectUtils sut = new ObjectUtils(new EmptyLinksHandler());

  @Test
  void scopeHasConnectivityLink() {
    Scope obj = new Scope();
    V1Scope data = new V1Scope();
    obj.setUniqueIdFrom("scope1");
    obj.setData(data);
    Link connectivityLink =
        new UriLink(
            "Connectivity Table",
            "/v2/versions/latest/data/scopes/scope1/connectivity",
            Display.TAB);

    List<Link> links = sut.links(obj, ObjectTypeListNamesEnum.SCOPES);

    assertThat(links).hasSizeGreaterThan(1).contains(connectivityLink);
  }
}

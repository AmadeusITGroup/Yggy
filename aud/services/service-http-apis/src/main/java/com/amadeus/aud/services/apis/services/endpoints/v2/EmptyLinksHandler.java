package com.amadeus.aud.services.apis.services.endpoints.v2;

import com.amadeus.aud.libraries.backend.models.v2.Link;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EmptyLinksHandler implements LinksHandler {

  @Override
  public List<Link> getLinks(BomElement obj) {
    return List.of();
  }
}

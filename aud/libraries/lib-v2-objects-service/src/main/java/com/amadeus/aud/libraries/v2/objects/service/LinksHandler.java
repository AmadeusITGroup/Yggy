package com.amadeus.aud.libraries.v2.objects.service;

import com.amadeus.aud.libraries.backend.models.v2.Link;
import com.amadeus.aud.libraries.models.core.db.base.BomElement;
import java.util.List;

public interface LinksHandler {
  List<Link> getLinks(BomElement obj);
}

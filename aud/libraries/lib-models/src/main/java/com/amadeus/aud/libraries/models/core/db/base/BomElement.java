package com.amadeus.aud.libraries.models.core.db.base;

import com.amadeus.aud.libraries.common.WithId;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.yaml.models.ModelElement;

/**
 * Interface representing a BOM (Bill of Materials) element.
 *
 * <p>a BOM element is an element that has links/pointers to other elements such as Scopes,
 * children, components, etc.
 *
 * <p>Compare with {@link ModelElement}
 */
public interface BomElement extends WithId {
  String DEFAULT_DOMAIN = "Governance";

  default Scope getScope() {
    final Scope governance = new Scope();
    governance.setName(DEFAULT_DOMAIN);
    return governance;
  }
}

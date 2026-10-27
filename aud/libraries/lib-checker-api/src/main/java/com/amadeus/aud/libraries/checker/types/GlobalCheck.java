package com.amadeus.aud.libraries.checker.types;

import com.amadeus.aud.libraries.checker.models.Bom;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.Models;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import java.util.Collection;
import java.util.List;

/** Checks that operate once on the loaded models, BOM, and complete graph. */
@FunctionalInterface
public non-sealed interface GlobalCheck extends CheckType {
  Collection<Message> check(Collection<String> domains, Models models, Bom bom, List<Link> links);
}

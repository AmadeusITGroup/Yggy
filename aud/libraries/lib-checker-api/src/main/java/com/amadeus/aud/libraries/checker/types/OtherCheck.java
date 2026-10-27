package com.amadeus.aud.libraries.checker.types;

import com.amadeus.aud.libraries.checker.models.Bom;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.engine.graph.models.Link;
import java.util.Collection;
import java.util.List;

/** Checks that operate on a valid BOM, but with links to validate. */
@FunctionalInterface
public non-sealed interface OtherCheck extends CheckType {
  Collection<Message> check(CharSequence domain, Bom bom, List<Link> links);
}

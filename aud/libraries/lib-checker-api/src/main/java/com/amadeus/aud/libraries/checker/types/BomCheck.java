package com.amadeus.aud.libraries.checker.types;

import com.amadeus.aud.libraries.checker.models.Bom;
import com.amadeus.aud.libraries.checker.models.Message;
import java.util.Collection;

/** Checks that operate on a BOM to validate */
@FunctionalInterface
public non-sealed interface BomCheck extends CheckType {
  Collection<Message> check(CharSequence domain, Bom bom);
}

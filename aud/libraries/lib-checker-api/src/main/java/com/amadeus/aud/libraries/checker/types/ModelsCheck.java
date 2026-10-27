package com.amadeus.aud.libraries.checker.types;

import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.Models;
import java.util.Collection;

/** Checks that operate on only knowing the Models to validate it. */
@FunctionalInterface
public non-sealed interface ModelsCheck extends CheckType {
  Collection<Message> check(CharSequence domain, Models models);
}

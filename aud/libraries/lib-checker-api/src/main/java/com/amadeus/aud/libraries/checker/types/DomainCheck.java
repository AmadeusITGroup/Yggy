package com.amadeus.aud.libraries.checker.types;

import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Checks regarding only the data parsed from the current domain, but not linked to other domains
 * (unlike ModelsCheck)
 */
@FunctionalInterface
public non-sealed interface DomainCheck extends CheckType {
  Collection<Message> check(
      CharSequence domain, Map<Class<? extends ModelElement>, List<ModelElement>> domainsData);
}

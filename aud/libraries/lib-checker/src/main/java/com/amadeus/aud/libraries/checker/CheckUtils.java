package com.amadeus.aud.libraries.checker;

import com.amadeus.aud.libraries.checker.models.Bom;
import com.amadeus.aud.libraries.models.core.db.Scope;
import java.util.Optional;

public final class CheckUtils {
  private CheckUtils() {}

  public static Optional<Scope> findScope(CharSequence domain, Bom bom) {
    for (Scope s : bom.list(Scope.class, null)) {
      if (domain.equals(s.getName())) {
        return Optional.of(s);
      }
    }
    return Optional.empty();
  }
}

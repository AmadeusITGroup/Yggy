package com.amadeus.aud.libraries.engine.data.repositories.core.db;

import com.amadeus.aud.libraries.engine.data.repositories.base.ConcurrentRepository;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import io.quarkus.logging.Log;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LandingZoneFunctionalTypesRepository
    extends ConcurrentRepository<LandingZoneFunctionalType> {
  public LandingZoneFunctionalTypesRepository() {
    super();
  }

  public void addObject(LandingZoneFunctionalType landingZoneFunctionalType) {
    Objects.requireNonNull(landingZoneFunctionalType, "landingZoneFunctionalType cannot be null");

    ConcurrentMap<String, LandingZoneFunctionalType> data = this.getData();
    if (data == null) {
      data = new ConcurrentHashMap<>();
      this.setData(data);
    }
    data.put(landingZoneFunctionalType.id(), landingZoneFunctionalType);
  }

  private <T extends AbstractBase<?>> void addObjectToGovernance(
      ScopesRepository scopesRepository, T entity, String entityName, Class<T> clazz) {
    addObjectToGovernance(scopesRepository, entity, entityName, clazz.getSimpleName());
  }

  private <T extends AbstractBase<?>> void addObjectToGovernance(
      ScopesRepository scopesRepository, T entity, String entityName, String className) {
    Objects.requireNonNull(entity, "entity cannot be null");

    if (scopesRepository == null) {
      Log.warnf("Scopes Repository not found.");
      return;
    }
    Scope governanceScope = scopesRepository.get("Governance");
    if (governanceScope == null) {
      throw new RuntimeException("Governance scope not found");
    }
    LandingZoneFunctionalType landingZoneFunctionalType =
        new LandingZoneFunctionalType(
            governanceScope,
            entityName,
            String.join(" ", "Automatically generated from", className),
            true);
    landingZoneFunctionalType.setScope(governanceScope);
    // TODO: Are we sure that we should not link it back to the Governance Scope?
    // governanceScope.addLandingZoneFunctionalType(landingZoneFunctionalType);

    addObject(landingZoneFunctionalType);
  }

  public void addObjectToGovernance(ScopesRepository scopesRepository, MacroPhase macroPhase) {
    addObjectToGovernance(
        scopesRepository,
        macroPhase,
        macroPhase.getData().getMetadata().getName(),
        MacroPhase.class);
  }

  public void addObjectToGovernance(ScopesRepository scopesRepository, Provider provider) {
    addObjectToGovernance(
        scopesRepository, provider, provider.getData().getMetadata().getName(), Provider.class);
  }

  public void addObjectToGovernance(ScopesRepository scopesRepository, Continent continent) {
    addObjectToGovernance(
        scopesRepository, continent, continent.getData().getMetadata().getName(), Continent.class);
  }

  public void addObjectToGovernance(ScopesRepository scopesRepository, Target target) {
    addObjectToGovernance(
        scopesRepository, target, target.getData().getMetadata().getName(), Target.class);
  }
}

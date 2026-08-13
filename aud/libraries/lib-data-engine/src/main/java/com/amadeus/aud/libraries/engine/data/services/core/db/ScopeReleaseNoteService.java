package com.amadeus.aud.libraries.engine.data.services.core.db;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopeReleaseNotesRepository;
import com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseService;
import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.models.core.db.Scope;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import jakarta.inject.Singleton;

@Singleton
public class ScopeReleaseNoteService extends BaseService<ScopeReleaseNote, V1ScopeReleaseNote> {

  @Override
  protected ScopeReleaseNotesRepository getRepository(Version version) {
    ScopeReleaseNotesRepository scopeReleaseNotesRepository = new ScopeReleaseNotesRepository();
    version.setScopeReleaseNotesRepository(scopeReleaseNotesRepository);
    return scopeReleaseNotesRepository;
  }

  @LogExecutionTime
  public void init(Version version) {
    init(version, ScopeReleaseNote.class, V1ScopeReleaseNote.class, this::callback);
  }

  private void callback(ScopeReleaseNote scopeReleaseNote, Scope scope) {
    if (scope == null) {
      logModelWarning(
          "Scope (%s) not found for ScopeReleaseNote: %s",
          scopeReleaseNote.getScope(), scopeReleaseNote);
    } else {
      scope.addScopeReleaseNote(scopeReleaseNote);
    }
  }
}

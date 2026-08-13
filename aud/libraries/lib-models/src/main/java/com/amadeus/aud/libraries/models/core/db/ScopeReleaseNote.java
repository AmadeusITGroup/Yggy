package com.amadeus.aud.libraries.models.core.db;

import static com.amadeus.aud.libraries.models.core.db.base.BaseInstance.ID_SEPARATOR;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Commit;
import com.amadeus.aud.libraries.yaml.models.V1Device;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1ScopeReleaseNoteSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class ScopeReleaseNote extends AbstractBase<V1ScopeReleaseNote> {
  public ScopeReleaseNote(V1ScopeReleaseNote scopeReleaseNote) {
    super(
        scopeReleaseNote,
        String.join(
            ID_SEPARATOR,
            Objects.requireNonNull(
                    Objects.requireNonNull(scopeReleaseNote, "scopeReleaseNote cannot be null")
                        .getMetadata(),
                    "metadata cannot be null")
                .getName(),
            Objects.requireNonNull(
                    Objects.requireNonNull(scopeReleaseNote, "scopeReleaseNote cannot be null")
                        .getSpec(),
                    "spec cannot be null")
                .getScopeVersion()));
  }

  public ScopeReleaseNote() {
    super();
  }

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  public List<V1Commit> getCommits() {
    V1ScopeReleaseNoteSpec spec = getData().getSpec();
    if (spec == null) {
      return Collections.emptyList();
    }
    if (spec.getCommits() == null) {
      return Collections.emptyList();
    }
    return spec.getCommits();
  }

  public @Nullable String getScopeVersion() {
    V1ScopeReleaseNoteSpec spec = getData().getSpec();
    if (spec == null) {
      return null;
    }
    return spec.getScopeVersion();
  }

  public List<String> getPrs() {
    V1ScopeReleaseNoteSpec spec = getData().getSpec();
    if (spec == null) {
      return Collections.emptyList();
    }
    if (spec.getPrs() == null) {
      return Collections.emptyList();
    }
    return spec.getPrs();
  }

  public @Nullable String getVersionDateTime() {
    V1ScopeReleaseNoteSpec spec = getData().getSpec();
    if (spec == null) {
      return null;
    }
    return spec.getVersionDatetime();
  }

  public @Nullable List<V1Device> getImpactedDevices() {
    V1ScopeReleaseNoteSpec spec = getData().getSpec();
    if (spec == null) {
      return null;
    }
    return spec.getImpactedDevices();
  }
}

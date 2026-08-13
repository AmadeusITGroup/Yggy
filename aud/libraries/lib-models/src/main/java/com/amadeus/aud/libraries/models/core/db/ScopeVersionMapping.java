package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMapping;
import com.amadeus.aud.libraries.yaml.models.V1ScopeVersionMappingSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import java.util.Objects;

@Entity
public class ScopeVersionMapping extends AbstractBase<V1ScopeVersionMapping> {

  public ScopeVersionMapping(V1ScopeVersionMapping scopeVersionMapping) {
    super(
        scopeVersionMapping,
        Objects.requireNonNull(
                Objects.requireNonNull(scopeVersionMapping, "scopeVersionMapping cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ScopeVersionMapping() {
    super();
  }

  public @Nullable String getScopeVersion() {
    V1ScopeVersionMappingSpec spec = getData().getSpec();
    if (spec == null) {
      return null;
    }
    return spec.getScopeVersion();
  }

  public @Nullable String getAudVersion() {
    V1ScopeVersionMappingSpec spec = getData().getSpec();
    if (spec == null) {
      return null;
    }
    return spec.getAudVersion();
  }

  public @Nullable String getVersionDateTime() {
    V1ScopeVersionMappingSpec spec = getData().getSpec();
    if (spec == null) {
      return null;
    }
    return spec.getVersionDatetime();
  }
}

package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ScopeReleaseNote;
import com.amadeus.aud.libraries.yaml.models.V1Device;
import jakarta.annotation.Nullable;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(ScopeReleaseNoteFilter.NAME)
public class ScopeReleaseNoteFilter {
  public static final String NAME = "ScopeReleaseNoteFilter";
  public static final String PROVIDER = "scopesReleaseNotes";
  private final @Nullable StringFilter scopeName;
  private final @Nullable StringFilter scopeVersion;
  private final @Nullable DeviceFilter impactedDevices;

  @JsonbCreator
  public ScopeReleaseNoteFilter(
      @JsonbProperty("scopeName") @Nullable StringFilter scopeName,
      @JsonbProperty("scopeVersion") @Nullable StringFilter scopeVersion,
      @JsonbProperty("impactedDevices") @Nullable DeviceFilter impactedDevices) {
    this.scopeName = scopeName;
    this.scopeVersion = scopeVersion;
    this.impactedDevices = impactedDevices;
  }

  public boolean matches(@Nullable ScopeReleaseNote releaseNote) {
    return releaseNote != null
        && (scopeName == null || scopeName.matches(releaseNote.getScopeName()))
        && (scopeVersion == null || scopeVersion.matches(releaseNote.getScopeVersion()))
        && matchesImpactedDevices(releaseNote.getImpactedDevices());
  }

  private boolean matchesImpactedDevices(@Nullable List<V1Device> devices) {
    if (impactedDevices == null) {
      return true;
    }
    if (devices == null || devices.isEmpty()) {
      return false;
    }

    for (V1Device device : devices) {
      if (impactedDevices.matches(device)) {
        return true;
      }
    }
    return false;
  }
}

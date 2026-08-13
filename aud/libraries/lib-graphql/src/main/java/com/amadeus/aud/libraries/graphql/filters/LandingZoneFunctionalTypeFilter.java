package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(LandingZoneFunctionalTypeFilter.NAME)
public class LandingZoneFunctionalTypeFilter {
  public static final String NAME = "LandingZoneFunctionalTypeFilter";
  public static final String PROVIDER = "landingZoneFunctionalTypes";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private Boolean isGenerated;
  private List<LandingZoneFilter> landingZones;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setIsGenerated(Boolean isGenerated) {
    this.isGenerated = isGenerated;
  }

  public void setLandingZones(List<LandingZoneFilter> landingZones) {
    this.landingZones = landingZones;
  }

  public StringFilter getId() {
    return id;
  }

  public StringFilter getName() {
    return name;
  }

  public ScopeFilter getScope() {
    return scope;
  }

  public Boolean getIsGenerated() {
    return isGenerated;
  }

  public List<LandingZoneFilter> getLandingZones() {
    return landingZones;
  }

  public boolean matches(@Nullable LandingZoneFunctionalType landingZoneFunctionalType) {
    return landingZoneFunctionalType != null
        && (id == null || id.matches(landingZoneFunctionalType.id()))
        && (name == null || name.matches(landingZoneFunctionalType.getName()))
        && (scope == null || scope.matches(landingZoneFunctionalType.getScope()))
        && FilterUtils.matchesBoolean(isGenerated, landingZoneFunctionalType.isGenerated())
        && FilterUtils.allFilterMatch(
            landingZones, landingZoneFunctionalType.getLandingZones(), LandingZoneFilter::matches);
  }
}

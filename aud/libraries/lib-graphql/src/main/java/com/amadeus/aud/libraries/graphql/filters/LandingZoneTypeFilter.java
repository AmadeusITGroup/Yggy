package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(LandingZoneTypeFilter.NAME)
public class LandingZoneTypeFilter {
  public static final String NAME = "LandingZoneTypeFilter";
  public static final String PROVIDER = "landingZoneTypes";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
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

  public List<LandingZoneFilter> getLandingZones() {
    return landingZones;
  }

  public boolean matches(@Nullable LandingZoneType landingZoneType) {
    return landingZoneType != null
        && (id == null || id.matches(landingZoneType.id()))
        && (name == null || name.matches(landingZoneType.getName()))
        && (scope == null || scope.matches(landingZoneType.getScope()))
        && FilterUtils.allFilterMatch(
            landingZones, landingZoneType.getLandingZones(), LandingZoneFilter::matches);
  }
}

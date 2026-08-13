package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Continent;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

@Input(ContinentFilter.NAME)
public class ContinentFilter {
  public static final String NAME = "ContinentFilter";
  public static final String PROVIDER = "continents";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter regionName;
  private StringFilter targetName;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
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

  public StringFilter getRegionName() {
    return regionName;
  }

  public StringFilter getTargetName() {
    return targetName;
  }

  public void setRegionName(StringFilter regionName) {
    this.regionName = regionName;
  }

  public void setTargetName(StringFilter targetName) {
    this.targetName = targetName;
  }

  public boolean matches(@Nullable Continent continent) {
    return continent != null
        && (id == null || id.matches(continent.id()))
        && (name == null || name.matches(continent.getName()))
        && (scope == null || scope.matches(continent.getScope()))
        && FilterUtils.anyStringFilterMatches(regionName, continent.getRegionNames(), r -> r)
        && FilterUtils.anyStringFilterMatches(targetName, continent.getTargetNames(), t -> t);
  }
}

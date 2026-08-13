package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.AutomaticTemplateInstance;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(AutomaticTemplateInstanceFilter.NAME)
public class AutomaticTemplateInstanceFilter {
  public static final String NAME = "AutomaticTemplateInstanceFilter";
  public static final String PROVIDER = "automaticTemplateInstances";
  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter componentName;
  private LandingZoneFilter landingZone;
  private List<ComponentInstanceFilter> componentInstances;

  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setComponentName(StringFilter componentName) {
    this.componentName = componentName;
  }

  public void setLandingZone(LandingZoneFilter landingZone) {
    this.landingZone = landingZone;
  }

  public void setComponentInstances(List<ComponentInstanceFilter> componentInstances) {
    this.componentInstances = componentInstances;
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

  public StringFilter getComponentName() {
    return componentName;
  }

  public LandingZoneFilter getLandingZone() {
    return landingZone;
  }

  public List<ComponentInstanceFilter> getComponentInstances() {
    return componentInstances;
  }

  public boolean matches(@Nullable AutomaticTemplateInstance automaticTemplateInstance) {
    return automaticTemplateInstance != null
        && (id == null || id.matches(automaticTemplateInstance.id()))
        && (name == null || name.matches(automaticTemplateInstance.getName()))
        && (scope == null || scope.matches(automaticTemplateInstance.getScope()))
        && (componentName == null
            || componentName.matches(automaticTemplateInstance.getComponentName()))
        && (landingZone == null || landingZone.matches(automaticTemplateInstance.getLandingZone()))
        && FilterUtils.allFilterMatch(
            componentInstances,
            automaticTemplateInstance.getComponentInstances(),
            ComponentInstanceFilter::matches);
  }
}

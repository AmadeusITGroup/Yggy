package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class AutomaticTemplateInstance extends BaseInstance<V1ComponentInstance> {
  @ManyToOne(targetEntity = Component.class, cascade = CascadeType.ALL)
  private Component component;

  @ManyToMany private Set<ComponentInstance> componentInstances;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private List<Set<String>> associatedFunctionalTypes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private List<Set<String>> excludedFunctionalTypes;

  @SuppressWarnings("this-escape")
  public AutomaticTemplateInstance(V1ComponentInstance componentInstance) {
    super(
        componentInstance,
        Objects.requireNonNull(
                Objects.requireNonNull(componentInstance, "componentInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());

    V1ComponentInstanceSpec spec = componentInstance.getSpec();

    this.associatedFunctionalTypes =
        getFunctionalTypesConditions(spec.getAssociatedFunctionalTypes());
    this.excludedFunctionalTypes = getFunctionalTypesConditions(spec.getExcludedFunctionalTypes());
  }

  public AutomaticTemplateInstance() {
    super();
  }

  private static List<Set<String>> getFunctionalTypesConditions(
      @Nullable List<String> functionalTypes) {
    if (functionalTypes == null) {
      return new ArrayList<>();
    }

    List<Set<String>> functionalTypesConditions = new ArrayList<>();
    for (String andConditions : functionalTypes) {
      functionalTypesConditions.add(getOrConditions(andConditions));
    }
    return functionalTypesConditions;
  }

  private static Set<String> getOrConditions(String andConditions) {
    String[] orConditions = andConditions.split(",");
    Set<String> orConditionsSet = new HashSet<>();
    for (String orCondition : orConditions) {
      orConditionsSet.add(orCondition.trim());
    }
    return orConditionsSet;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }

  public @Nullable String getComponentName() {
    if (component == null) {
      return null;
    }
    return component.getName();
  }

  public Set<ComponentInstance> getComponentInstances() {
    return componentInstances;
  }

  public void setComponentInstances(Set<ComponentInstance> componentInstances) {
    this.componentInstances = componentInstances;
  }

  public void addComponentInstance(ComponentInstance componentInstance) {
    if (componentInstances == null) {
      componentInstances = new HashSet<>();
    }
    componentInstances.add(componentInstance);
  }

  public void addComponentInstances(Set<ComponentInstance> componentInstances) {
    if (this.componentInstances == null) {
      this.componentInstances = new HashSet<>();
    }
    this.componentInstances.addAll(componentInstances);
  }

  public List<Set<String>> getAssociatedFunctionalTypes() {
    return associatedFunctionalTypes;
  }

  public void setAssociatedFunctionalTypes(List<Set<String>> associatedFunctionalTypes) {
    this.associatedFunctionalTypes = associatedFunctionalTypes;
  }

  public List<Set<String>> getExcludedFunctionalTypes() {
    return excludedFunctionalTypes;
  }

  public void setExcludedFunctionalTypes(List<Set<String>> excludedFunctionalTypes) {
    this.excludedFunctionalTypes = excludedFunctionalTypes;
  }

  /**
   * Method that checks if the landing zone functional types match the inclusion and not the
   * exclusion conditions.
   *
   * <p>Please refer to {@link #matchesLandingZoneFunctionalTypes(List, Set)} for more details on
   * the logic applied to the inclusion adn exclusion conditions.
   *
   * @param landingZoneFunctionalTypes Set of functional types associated with the landing zone
   * @return true if the landing zone functional types match the conditions
   */
  public boolean matchesLandingZoneFunctionalTypes(
      @Nullable Set<LandingZoneFunctionalType> landingZoneFunctionalTypes) {
    if (landingZoneFunctionalTypes == null) {
      return false;
    }

    // Fail-fast if no functional types are associated with the landing zone
    if ((this.associatedFunctionalTypes == null || this.associatedFunctionalTypes.isEmpty())
        && (this.excludedFunctionalTypes == null || this.excludedFunctionalTypes.isEmpty())) {
      return false;
    }

    Set<String> landingZoneFunctionalTypeNames = new HashSet<>();
    for (LandingZoneFunctionalType landingZoneFunctionalType : landingZoneFunctionalTypes) {
      landingZoneFunctionalTypeNames.add(landingZoneFunctionalType.getName());
    }

    return matchesLandingZoneFunctionalTypes(
            this.associatedFunctionalTypes, landingZoneFunctionalTypeNames)
        && !matchesLandingZoneFunctionalTypes(
            this.excludedFunctionalTypes, landingZoneFunctionalTypeNames);
  }

  /**
   * Method that checks if the landing zone functional types match the conditions.
   *
   * <p>The conditions are a list of ANDs and ORs. The data structure is as follows:
   * List<Set<String>> where each Set<String> is a set of OR conditions and the List is list of AND
   * conditions.
   *
   * @param functionalTypes List of conditions as defined above.
   * @param landingZoneFunctionalTypeNames Set of functional type names
   * @return true if the landing zone functional types match the conditions
   */
  private static boolean matchesLandingZoneFunctionalTypes(
      @Nullable List<Set<String>> functionalTypes, Set<String> landingZoneFunctionalTypeNames) {
    if (functionalTypes == null || functionalTypes.isEmpty()) {
      return false;
    }

    boolean conditionMatched = false;
    for (Set<String> orConditions : functionalTypes) {
      conditionMatched = false;
      for (String orCondition : orConditions) {
        if (landingZoneFunctionalTypeNames.contains(orCondition)) {
          conditionMatched = true;
          break;
        }
      }
      if (!conditionMatched) {
        break;
      }
    }
    return conditionMatched;
  }
}

package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstanceSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class DependencyInstance extends BaseInstance<V1DependencyInstance> {

  @ManyToOne(targetEntity = ComponentInstance.class, cascade = CascadeType.ALL)
  private ComponentInstance viaComponentInstance;

  @ManyToOne(targetEntity = Service.class, cascade = CascadeType.ALL)
  private Service viaService;

  @ManyToOne(targetEntity = ComponentInstance.class, cascade = CascadeType.ALL)
  private ComponentInstance componentInstance;

  @ManyToOne(targetEntity = Dependency.class, cascade = CascadeType.ALL)
  private Dependency dependency;

  @ManyToOne(targetEntity = ComponentInstance.class, cascade = CascadeType.ALL)
  private ComponentInstance targetComponentInstance;

  @ManyToMany private Set<Service> services;

  private boolean isSourceAsComponent;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Map<Component, Boolean> internalUseWideSubnets;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Set<Map<String, String>> sourcesSelectors;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Set<Map<String, String>> targetsSelectors;

  public DependencyInstance(V1DependencyInstance dependencyInstance) {
    super(
        dependencyInstance,
        Objects.requireNonNull(
                Objects.requireNonNull(dependencyInstance, "dependencyInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName(),
        Objects.requireNonNull(
                Objects.requireNonNull(dependencyInstance, "dependencyInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getLandingzone());
  }

  public DependencyInstance() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj)
        && (obj instanceof DependencyInstance that)
        //        && Objects.equals(componentInstance, that.componentInstance)
        //        && Objects.equals(dependency, that.dependency)
        //        && Objects.equals(targetComponentInstance, that.targetComponentInstance)
        && isSourceAsComponent == that.isSourceAsComponent;
    //        && Objects.equals(services, that.services);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isSourceAsComponent());
  }

  public @Nullable String getDependencyName() {
    if (dependency == null) {
      return null;
    }
    return dependency.getName();
  }

  public boolean isSourceLimitedToLegacyOnly() {
    return Boolean.TRUE.equals(getData().getSpec().getIsSourceLimitedToLegacyOnly());
  }

  public boolean isSourceLimitedToCloudOnly() {
    return Boolean.TRUE.equals(getData().getSpec().getIsSourceLimitedToCloudOnly());
  }

  public boolean isSourceLimitedToLandingZone() {
    return Boolean.TRUE.equals(getData().getSpec().getIsSourceLimitedToLandingzone());
  }

  public boolean isSourceLimitedToTarget() {
    return Boolean.TRUE.equals(getData().getSpec().getIsSourceLimitedToTarget());
  }

  public boolean isSourceLimitedToContinent() {
    return Boolean.TRUE.equals(getData().getSpec().getIsSourceLimitedToContinent());
  }

  public boolean isSourceLimitedToMacroPhase() {
    return Boolean.TRUE.equals(getData().getSpec().getIsSourceLimitedToMacrophase());
  }

  public boolean isSourceLimitedToSelector() {
    return Boolean.TRUE.equals(getData().getSpec().getIsSourceLimitedToSelector());
  }

  public boolean isTargetAsComponent() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetAsComponent());
  }

  public boolean isTargetLimitedToLegacyOnly() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToLegacyOnly());
  }

  public boolean isTargetLimitedToCloudOnly() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToCloudOnly());
  }

  public boolean isTargetLimitedToSourceNamespace() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourceNamespace());
  }

  public boolean isTargetLimitedToSourcePaas() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourcePaas());
  }

  public boolean isTargetLimitedToSourceSecurityZone() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourceSecurityzone());
  }

  public boolean isTargetLimitedToSourceApplicationPhase() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourceApplicationphase());
  }

  public boolean isTargetLimitedToSourceLandingZone() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourceLandingzone());
  }

  public boolean isTargetLimitedToSourceTarget() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourceTarget());
  }

  public boolean isTargetLimitedToSourceContinent() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourceContinent());
  }

  public boolean isTargetLimitedToSourceMacroPhase() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTargetLimitedToSourceMacrophase());
  }

  public V1DependencyInstanceSpec.DelegateGovernanceToEnum getDelegatedGovernanceTo() {
    return getData().getSpec().getDelegateGovernanceTo();
  }

  public ComponentInstance getViaComponentInstance() {
    return viaComponentInstance;
  }

  public Service getViaService() {
    return viaService;
  }

  public @Nullable String getViaComponentInstanceName() {
    if (viaComponentInstance == null) {
      return null;
    }
    return viaComponentInstance.getName();
  }

  public @Nullable String getViaServiceName() {
    if (viaService == null) {
      return null;
    }
    return viaService.getName();
  }

  public @Nullable String getSourceComponentInstanceName() {
    if (componentInstance == null) {
      return null;
    }
    return componentInstance.getName();
  }

  public @Nullable String getTargetComponentInstanceName() {
    if (targetComponentInstance == null) {
      return null;
    }
    return targetComponentInstance.getName();
  }

  public synchronized Set<Map<String, String>> getSourcesSelectors() {
    if (this.sourcesSelectors == null || this.sourcesSelectors.isEmpty()) {
      initSourcesSelectors();
    }
    return this.sourcesSelectors;
  }

  public synchronized void initSourcesSelectors() {
    List<Map<String, String>> sourcesSelectors1 = getData().getSpec().getSourcesSelectors();
    if (sourcesSelectors1 != null) {
      this.sourcesSelectors = new HashSet<>(sourcesSelectors1);
    }
  }

  public synchronized Set<Map<String, String>> getTargetsSelectors() {
    if (this.targetsSelectors == null || this.targetsSelectors.isEmpty()) {
      initTargetsSelectors();
    }
    return this.targetsSelectors;
  }

  public synchronized void initTargetsSelectors() {
    List<Map<String, String>> targetsSelectors1 = getData().getSpec().getTargetsSelectors();
    if (targetsSelectors1 != null) {
      this.targetsSelectors = new HashSet<>(targetsSelectors1);
    }
  }

  public ComponentInstance getComponentInstance() {
    return componentInstance;
  }

  public void setViaComponentInstance(ComponentInstance componentInstance) {
    this.viaComponentInstance = componentInstance;
  }

  public void setViaService(Service service) {
    this.viaService = service;
  }

  public void setComponentInstance(ComponentInstance componentInstance) {
    this.componentInstance = componentInstance;
  }

  public Dependency getDependency() {
    return dependency;
  }

  public void setDependency(Dependency dependency) {
    this.dependency = dependency;
  }

  public ComponentInstance getTargetComponentInstance() {
    return targetComponentInstance;
  }

  public void setTargetComponentInstance(ComponentInstance targetComponentInstance) {
    this.targetComponentInstance = targetComponentInstance;
  }

  public boolean isSourceAsComponent() {
    return isSourceAsComponent;
  }

  public void setSourceAsComponent(boolean sourceAsComponent) {
    isSourceAsComponent = sourceAsComponent;
  }

  public Set<Service> getServices() {
    return services;
  }

  public void setServices(Set<Service> services) {
    this.services = services;
  }

  public void addService(Service service) {
    if (services == null) {
      services = new HashSet<>();
    }
    services.add(service);
  }

  public void addServices(Set<Service> services) {
    if (this.services == null) {
      this.services = new HashSet<>();
    }
    this.services.addAll(services);
  }

  public Map<Component, Boolean> getInternalUseWideSubnets() {
    return internalUseWideSubnets;
  }

  public void setInternalUseWideSubnets(Map<Component, Boolean> internalUseWideSubnets) {
    this.internalUseWideSubnets = internalUseWideSubnets;
  }

  @JsonIgnore
  public boolean isUseWideSubnet() {
    return isUseWideSubnet(true);
  }

  @JsonIgnore
  public boolean isUseWideSubnet(boolean isSource) {
    if (internalUseWideSubnets == null) {
      return false;
    }
    Component component = null;
    if (isSource) {
      if (isSourceAsComponent()) {
        component = dependency.getSourceComponent();
      }
    } else if (isTargetAsComponent()) {
      component = dependency.getDestinationComponent();
    }

    return component != null && internalUseWideSubnets.getOrDefault(component, false);
  }

  /**
   * Reports if the dependency instance is generic on supplied end and should open the connectivity
   * to "wide" set of subnets - to/from all endpoints of the corresponding Target; This considered
   * to be true if the Component on the generic end is "Linux", "Windows" or one of ancestors of
   * "Linux", "Windows"
   *
   * @param componentsToUseWideSubnet "Linux", "Windows" components plus all the Components
   *     ancestors of "Linux", "Windows"
   */
  public void initInternalUseWideSubnets(Set<Component> componentsToUseWideSubnet) {
    internalUseWideSubnets = new HashMap<>();
    if (dependency == null) {
      return;
    }

    Component component = isSourceAsComponent() ? dependency.getSourceComponent() : null;
    if (component != null) {
      internalUseWideSubnets.put(component, componentsToUseWideSubnet.contains(component));
    }

    component = isTargetAsComponent() ? dependency.getDestinationComponent() : null;
    if (component != null) {
      internalUseWideSubnets.put(component, componentsToUseWideSubnet.contains(component));
    }
  }

  public Set<String> getServiceNames() {
    Set<String> names = new HashSet<>();
    if (services != null) {
      for (Service svc : services) {
        names.add(svc.id());
      }
    }
    return names;
  }
}

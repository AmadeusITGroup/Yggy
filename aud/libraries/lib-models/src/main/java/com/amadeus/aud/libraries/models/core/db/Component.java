package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.AccessControl;
import com.amadeus.aud.libraries.yaml.models.AccessControlDeny;
import com.amadeus.aud.libraries.yaml.models.AccessControlDenyAnyOf;
import com.amadeus.aud.libraries.yaml.models.AccessDenialMessage;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntry;
import com.amadeus.aud.libraries.yaml.models.AccessRightsEntryScopes;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class Component extends AbstractBase<V1Component> implements WithDeprecation {

  public static final String WILD_CARD_ALL = "*";

  @Transient private Set<Component> parents;

  @Transient private Set<Component> children;

  @OneToMany(mappedBy = "component")
  private Set<ZoneType> zoneTypes;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Set<String> serviceNowIds;

  @OneToMany(mappedBy = "component")
  private Set<Service> services;

  @Transient private Set<Dependency> dependencies;

  @OneToMany(mappedBy = "component")
  private Set<ComponentInstance> componentInstances;

  @OneToMany(mappedBy = "component")
  private Set<AutomaticTemplateInstance> automaticTemplateInstances;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Set<String> securityZones;

  @Transient private Set<Component> internalAncestors;

  @Transient private Map<Boolean, Set<ComponentInstance>> allComponentInstances;

  public Component(V1Component component) {
    super(
        component,
        Objects.requireNonNull(
                Objects.requireNonNull(component, "component cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Component() {
    super();
  }

  private void addSecurityZone(String securityZone) {
    if (this.securityZones == null) {
      this.securityZones = new HashSet<>();
    }
    this.securityZones.add(securityZone);
  }

  public Set<String> getSecurityZones() {
    return this.securityZones;
  }

  @Override
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return getData().getSpec().getDeprecatedStatus();
  }

  @Override
  public @Nullable String getDeprecatedMessage() {
    return getData().getSpec().getDeprecatedMessage();
  }

  public @Nullable Map<String, Object> getAccessControl() {
    AccessControl accessControl = getData().getSpec().getAccessControl();
    if (accessControl == null) {
      return null;
    }

    var allow = accessControl.getAllow();
    Map<String, Object> result = new LinkedHashMap<>();
    List<Map<String, Object>> flattenedAllow =
        flattenAccessRightsList(allow != null ? allow.getAccessRights() : null);
    if (flattenedAllow != null) {
      result.put("allow", flattenedAllow);
    }

    Object flattenedDeny = flattenDeny(accessControl.getDeny());
    if (flattenedDeny != null) {
      result.put("deny", flattenedDeny);
    }

    result.put("controlled_components", getControlledComponents(accessControl));
    return result;
  }

  private @Nullable Object flattenDeny(@Nullable AccessControlDeny deny) {
    if (deny == null) {
      return null;
    }

    Object denyInstance = deny.getActualInstance();
    if (denyInstance instanceof AccessControlDenyAnyOf denyBody) {
      return flattenAccessRightsList(denyBody.getAccessRights());
    }
    if (denyInstance instanceof AccessDenialMessage denialMessage) {
      return flattenDenialMessage(denialMessage);
    }
    return null;
  }

  private @Nullable List<Map<String, Object>> flattenAccessRightsList(
      @Nullable List<AccessRightsEntry> accessRights) {
    if (accessRights == null) {
      return null;
    }

    List<Map<String, Object>> flattenedEntries = new ArrayList<>();
    for (AccessRightsEntry accessRight : accessRights) {
      if (accessRight == null) {
        continue;
      }

      Map<String, Object> flattenedEntry = new LinkedHashMap<>();
      var permission = accessRight.getPermission();
      if (permission != null && !permission.getValue().isEmpty()) {
        flattenedEntry.put("permission", permission.getValue());
      }

      Map<String, Object> denialMessage = flattenDenialMessage(accessRight.getReportIfDenied());
      if (denialMessage != null) {
        flattenedEntry.putAll(denialMessage);
      }

      AccessRightsEntryScopes applyTo = accessRight.getApplyTo();
      List<String> appliedToScopes = applyTo != null ? applyTo.getScopes() : null;
      if (appliedToScopes != null) {
        flattenedEntry.put("applied_to_scopes", appliedToScopes);
      }

      flattenedEntries.add(flattenedEntry);
    }
    return flattenedEntries;
  }

  private @Nullable Map<String, Object> flattenDenialMessage(
      @Nullable AccessDenialMessage denialMessage) {
    if (denialMessage == null) {
      return null;
    }

    Map<String, Object> flattenedMessage = new LinkedHashMap<>();
    if (denialMessage.getUserMessage() != null) {
      flattenedMessage.put("user_message", denialMessage.getUserMessage());
    }
    if (denialMessage.getUserMessageSeverity() != null
        && !denialMessage.getUserMessageSeverity().getValue().isEmpty()) {
      flattenedMessage.put(
          "user_message_severity", denialMessage.getUserMessageSeverity().getValue());
    }
    return flattenedMessage.isEmpty() ? null : flattenedMessage;
  }

  private List<String> getControlledComponents(AccessControl accessControl) {
    Scope scope = getScope();
    Set<Component> scopeComponents = scope != null ? scope.getComponents() : null;
    if (scopeComponents == null) {
      return currentComponentName();
    }

    List<String> controlledComponentNames = new ArrayList<>();
    for (Component component : scopeComponents) {
      if (component == null
          || component.getName() == null
          || component.getData() == null
          || component.getData().getSpec() == null) {
        continue;
      }

      if (Objects.equals(component.getData().getSpec().getAccessControl(), accessControl)) {
        controlledComponentNames.add(component.getName());
      }
    }

    if (controlledComponentNames.isEmpty()) {
      return currentComponentName();
    }

    return controlledComponentNames;
  }

  private List<String> currentComponentName() {
    return List.of(getName());
  }

  public boolean isPublic() {
    return Boolean.TRUE.equals(getData().getSpec().getIsPublic());
  }

  public boolean isPublicInheritance() {
    return Boolean.TRUE.equals(getData().getSpec().getIsPublicInheritance());
  }

  public boolean isPublicDependency() {
    return Boolean.TRUE.equals(getData().getSpec().getIsPublicDependency());
  }

  public boolean isPrivate() {
    return Boolean.TRUE.equals(getData().getSpec().getIsPrivate());
  }

  public boolean isHosting() {
    return Boolean.TRUE.equals(getData().getSpec().getIsHosting());
  }

  public boolean isHosted() {
    return Boolean.TRUE.equals(getData().getSpec().getIsContainer());
  }

  public boolean isTechnical() {
    return Boolean.TRUE.equals(getData().getSpec().getIsTechnical());
  }

  public boolean isInterface() {
    return isTechnical();
  }

  public boolean isContainer() {
    return Boolean.TRUE.equals(getData().getSpec().getIsContainer());
  }

  public Set<Component> getChildren() {
    return children;
  }

  public void setChildren(Set<Component> children) {
    this.children = children;
  }

  public void addChild(Component child) {
    if (this.children == null) {
      this.children = new HashSet<>();
    }
    this.children.add(child);
  }

  public void addChildren(Set<Component> children) {
    if (this.children == null) {
      this.children = new HashSet<>();
    }
    this.children.addAll(children);
  }

  @JsonIgnore
  public Set<Component> getParents() {
    return parents;
  }

  @JsonProperty("parents")
  public Set<String> getParentsNames() {
    Set<String> parentsNames = new HashSet<>();
    if (parents != null) {
      for (Component parent : parents) {
        parentsNames.add(parent.id());
      }
    }
    return parentsNames;
  }

  public void setParents(Set<Component> parents) {
    this.parents = parents;
  }

  public void addParent(Component parent) {
    if (this.parents == null) {
      // IMPORTANT: the LinkedHashSet is used to maintain the order of the parents
      this.parents = new LinkedHashSet<>();
    }
    this.parents.add(parent);
  }

  public void addParents(Set<Component> parents) {
    if (this.parents == null) {
      this.parents = new HashSet<>();
    }
    this.parents.addAll(parents);
  }

  public Set<ZoneType> getZoneTypes() {
    return zoneTypes;
  }

  public void setZoneTypes(Set<ZoneType> zoneTypes) {
    this.zoneTypes = zoneTypes;
    for (ZoneType zoneType : zoneTypes) {
      addSecurityZone(zoneType.getName());
    }
  }

  public void addZoneType(ZoneType zoneType) {
    if (this.zoneTypes == null) {
      this.zoneTypes = new HashSet<>();
    }
    this.zoneTypes.add(zoneType);
    addSecurityZone(zoneType.getName());
  }

  public void addZoneTypes(Set<ZoneType> zoneTypes) {
    if (this.zoneTypes == null) {
      this.zoneTypes = new HashSet<>();
    }
    this.zoneTypes.addAll(zoneTypes);
    for (ZoneType zoneType : zoneTypes) {
      addSecurityZone(zoneType.getName());
    }
  }

  public Set<String> getServiceNowIds() {
    return serviceNowIds;
  }

  public void setServiceNowIds(Set<String> serviceNowIds) {
    this.serviceNowIds = serviceNowIds;
  }

  public void addServiceNowId(String serviceNowId) {
    if (this.serviceNowIds == null) {
      this.serviceNowIds = new HashSet<>();
    }
    this.serviceNowIds.add(serviceNowId);
  }

  public void addServiceNowIds(Set<String> serviceNowIds) {
    if (this.serviceNowIds == null) {
      this.serviceNowIds = new HashSet<>();
    }
    this.serviceNowIds.addAll(serviceNowIds);
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

  public Set<Dependency> getDependencies() {
    return dependencies;
  }

  public void setDependencies(Set<Dependency> dependencies) {
    this.dependencies = dependencies;
  }

  public void addDependency(Dependency dependency) {
    if (dependencies == null) {
      dependencies = new HashSet<>();
    }
    dependencies.add(dependency);
  }

  public void addDependencies(Set<Dependency> dependencies) {
    if (this.dependencies == null) {
      this.dependencies = new HashSet<>();
    }
    this.dependencies.addAll(dependencies);
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

  public Set<AutomaticTemplateInstance> getAutomaticTemplateInstances() {
    return automaticTemplateInstances;
  }

  public void setAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    this.automaticTemplateInstances = automaticTemplateInstances;
  }

  public void addAutomaticTemplateInstance(AutomaticTemplateInstance automaticTemplateInstance) {
    if (automaticTemplateInstances == null) {
      automaticTemplateInstances = new HashSet<>();
    }
    automaticTemplateInstances.add(automaticTemplateInstance);
  }

  public void addAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    if (this.automaticTemplateInstances == null) {
      this.automaticTemplateInstances = new HashSet<>();
    }
    this.automaticTemplateInstances.addAll(automaticTemplateInstances);
  }

  public Set<ComponentInstance> getAllComponentInstances() {
    return getAllComponentInstances(true);
  }

  public Set<ComponentInstance> getAllComponentInstances(boolean countTechnical) {
    if (this.allComponentInstances == null) {
      return initAllComponentInstances(countTechnical);
    }

    Set<ComponentInstance> allComponentsInstances = this.allComponentInstances.get(countTechnical);
    if (allComponentsInstances == null) {
      return initAllComponentInstances(countTechnical);
    }

    return allComponentsInstances;
  }

  public synchronized Set<ComponentInstance> initAllComponentInstances(boolean countTechnical) {
    if (this.allComponentInstances == null) {
      this.allComponentInstances = new HashMap<>();
    }

    Set<ComponentInstance> result = new HashSet<>();
    boolean technical = isTechnical();
    if (componentInstances != null
        && !componentInstances.isEmpty()
        && (countTechnical || !technical)) {
      result.addAll(componentInstances);
    }

    if (children != null) {
      for (Component child : children) {
        result.addAll(child.getAllComponentInstances(countTechnical));
      }
    }
    allComponentInstances.put(countTechnical, result);

    return result;
  }

  public synchronized Set<Component> getInternalAncestors() {
    if (internalAncestors != null && !internalAncestors.isEmpty()) {
      return internalAncestors;
    }

    // IMPORTANT: the LinkedHashSet is used to maintain the order of the ancestors
    internalAncestors = new LinkedHashSet<>();
    internalAncestors.add(this);
    if (parents != null) {
      for (Component parent : parents) {
        internalAncestors.addAll(parent.getInternalAncestors());
      }
    }
    return internalAncestors;
  }
}

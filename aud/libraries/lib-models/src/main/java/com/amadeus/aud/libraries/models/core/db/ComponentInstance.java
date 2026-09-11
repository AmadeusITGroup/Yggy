package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.common.Constants;
import com.amadeus.aud.libraries.models.core.db.base.BaseInstance;
import com.amadeus.aud.libraries.models.core.internals.Direction;
import com.amadeus.aud.libraries.models.core.internals.Endpoint;
import com.amadeus.aud.libraries.models.core.utils.EndpointUtils;
import com.amadeus.aud.libraries.models.core.utils.IPv4Utils;
import com.amadeus.aud.libraries.models.core.utils.StringUtils;
import com.amadeus.aud.libraries.models.core.utils.UUIDUtils;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstanceSpec;
import com.amadeus.aud.libraries.yaml.models.V1IPAddressInstanceSpec;
import com.google.common.net.InetAddresses;
import com.google.common.net.UrlEscapers;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
public class ComponentInstance extends BaseInstance<V1ComponentInstance>
    implements WithDeprecation {

  public static final String JSON_PROPERTY_COMPONENT = "component";
  public static final String VALUE = "value";
  public static final String KEY = "key";
  public static final String IS_INHERITED = "is_inherited";
  public static final String IS_GENERATED = "is_generated";
  public static final String INTERNET_DNS_USED_AS_SOURCE = "Internet DNS used as source";
  private static final String AZURE_PROVIDER = "Microsoft - Azure";

  @ManyToOne(targetEntity = Component.class)
  private Component component;

  @ManyToOne(targetEntity = ApplicationPhase.class)
  private ApplicationPhase applicationPhase;

  @ManyToOne(targetEntity = ServiceTag.class)
  @Nullable
  private ServiceTag serviceTag;

  @ManyToOne(targetEntity = ZoneType.class)
  private ZoneType zoneType;

  @ManyToOne(targetEntity = SubnetInstance.class)
  private SubnetInstance inSubnetInstance;

  @ManyToOne(targetEntity = SubnetInstance.class)
  private SubnetInstance outSubnetInstance;

  @ManyToOne(targetEntity = ZoneInstance.class)
  private ZoneInstance inZoneInstance;

  @ManyToOne(targetEntity = ZoneInstance.class)
  private ZoneInstance outZoneInstance;

  @ManyToOne(targetEntity = Namespace.class)
  private Namespace namespace;

  private boolean isAutomaticallyInstantiated = false;

  @ManyToMany(mappedBy = "componentInstances")
  private Set<AutomaticTemplateInstance> automaticTemplateInstances;

  @ManyToOne(targetEntity = ComponentInstance.class)
  private ComponentInstance hostingPlatform;

  @Transient private Set<String> ancestorNames;

  @Transient private Map<String, ObjectGroup> objectGroupDirectionCache;

  @Transient private Map<Boolean, Map<Direction, ObjectGroup>> objectGroupsCache;

  @Transient private Set<ComponentInstance> hostedComponentInstances;

  @Transient
  private final Map<Direction, String> objectGroupNameCache = new EnumMap<>(Direction.class);

  @Transient private Set<ZoneType> zoneTypes;

  @OneToMany(mappedBy = "componentInstance")
  private Set<IPAddressInstance> ipAddressInstances;

  @Transient private Set<DependencyInstance> dependencyInstances;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Map<Integer, String> internalZoneTypeNames;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Map<Direction, String> internalZoneTypeNamesByDirection;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Map<Direction, Set<Endpoint>> internalEndpoints;

  @Transient private Set<SubnetInstance> paasZoneOutSubnetInstances;

  @Transient private Set<SubnetInstance> paasZoneInSubnetInstances;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(length = 8192)
  private Set<Map<String, String>> sourcesSelectors;

  public ComponentInstance(V1ComponentInstance componentInstance) {
    super(
        componentInstance,
        Objects.requireNonNull(
                Objects.requireNonNull(componentInstance, "componentInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public ComponentInstance(V1ComponentInstance componentInstance, String landingZoneName) {
    super(
        componentInstance,
        Objects.requireNonNull(
                Objects.requireNonNull(componentInstance, "componentInstance cannot be null")
                    .getMetadata(),
                "metadata cannot be null")
            .getName(),
        Objects.requireNonNull(landingZoneName));
  }

  public ComponentInstance() {
    super();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ComponentInstance that = (ComponentInstance) obj;

    return super.equals(obj)
        && Objects.equals(isAutomaticallyInstantiated, that.isAutomaticallyInstantiated);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isAutomaticallyInstantiated);
  }

  public @Nullable String getComponentName() {
    if (component == null) {
      return null;
    }
    return component.getName();
  }

  @Override
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return getData().getSpec().getDeprecatedStatus();
  }

  @Override
  public @Nullable String getDeprecatedMessage() {
    return getData().getSpec().getDeprecatedMessage();
  }

  public V1ComponentInstanceSpec.EndpointTypeEnum getEndpointType() {
    return getData().getSpec().getEndpointType();
  }

  public @Nullable String getApplicationPhaseName() {
    if (applicationPhase == null) {
      return null;
    }
    return applicationPhase.getName();
  }

  public String getNamespaceName() {
    if (namespace == null) {
      // TODO: Remove this if when all scopes have fixed all ContainerHasNamespaceReference checks
      // (MOD00003 to MOD00006)
      return getData().getSpec().getNamespace();
    }
    return namespace.getName();
  }

  public @Nullable String getDeployComponentInstanceName() {
    if (hostingPlatform == null) {
      return null;
    }
    return hostingPlatform.getName();
  }

  public @Nullable String getZoneTypeName() {
    if (zoneType == null) {
      return null;
    }
    return zoneType.getName();
  }

  public String getK8sIdentifier() {
    return getData().getSpec().getK8sIdentifier();
  }

  public V1ComponentInstanceSpec.HostingComponentTypeEnum getHostingPlatformType() {
    return getData().getSpec().getHostingComponentType();
  }

  public V1ComponentInstanceSpec.HostingComponentSecurityEnum getHostingPlatformSecurity() {
    return getData().getSpec().getHostingComponentSecurity();
  }

  public @Nullable List<String> getHostingOverlaySubnets() {
    return getData().getSpec().getOverlaySubnets();
  }

  public @Nullable String getInSubnetInstanceName() {
    if (inSubnetInstance == null) {
      return null;
    }
    return inSubnetInstance.getName();
  }

  public @Nullable String getOutSubnetInstanceName() {
    if (outSubnetInstance == null) {
      return null;
    }
    return outSubnetInstance.getName();
  }

  public @Nullable String getInZoneInstanceName() {
    if (inZoneInstance == null) {
      return null;
    }
    return inZoneInstance.getName();
  }

  public @Nullable String getOutZoneInstanceName() {
    if (outZoneInstance == null) {
      return null;
    }
    return outZoneInstance.getName();
  }

  public @Nullable String getServiceTagName() {
    if (serviceTag == null) {
      return null;
    }
    return serviceTag.getName();
  }

  public List<Object> getEndpoints() {
    return getData().getSpec().getEndpoints();
  }

  public Map<String, String> getK8sSelector() {
    return getData().getSpec().getK8sSelector();
  }

  public synchronized Set<Map<String, String>> getSourcesSelectors() {
    if (this.sourcesSelectors == null || this.sourcesSelectors.isEmpty()) {
      initSourceSelectors();
    }

    return sourcesSelectors;
  }

  public synchronized void initSourceSelectors() {
    List<Map<String, String>> sourcesSelectors1 = getData().getSpec().getSourcesSelectors();
    if (sourcesSelectors1 != null) {
      sourcesSelectors = new HashSet<>(sourcesSelectors1);
    }
  }

  public V1ComponentInstanceSpec.HostingComponentSecurityEnum getHostingComponentSecurity() {
    return getData().getSpec().getHostingComponentSecurity();
  }

  public boolean isExternal() {
    LandingZone landingZone = getLandingZone();
    if (landingZone == null) {
      return false;
    }

    Set<ZoneInstance> zoneInstances = landingZone.getZoneInstances();
    if (zoneInstances == null) {
      return false;
    }

    String zoneTypeName = null;
    for (ZoneInstance zoneInstance : zoneInstances) {
      if (Constants.GLOBAL.equals(zoneInstance.getName())) {
        ZoneType globalZoneType = zoneInstance.getZoneType();
        if (globalZoneType != null) {
          zoneTypeName = globalZoneType.getName();
        }
        break;
      }
    }

    return Constants.STR_ZONE_TYPE_INTERNET.equalsIgnoreCase(zoneTypeName)
        || Constants.STR_ZONE_TYPE_EXTRANET.equalsIgnoreCase(zoneTypeName);
  }

  public boolean isChildOfComponent(String parentComponent) {
    initAncestorNames();
    return ancestorNames.contains(parentComponent);
  }

  public ObjectGroup getObjectGroupDirection(Direction direction, boolean cidrInUrlCategoryFlag) {
    if (objectGroupDirectionCache == null) {
      objectGroupDirectionCache = new HashMap<>();
    }
    String cacheKey =
        new StringBuilder()
            .append(direction.name())
            .append('_')
            .append(cidrInUrlCategoryFlag)
            .toString();
    ObjectGroup cached = objectGroupDirectionCache.get(cacheKey);
    if (cached == null) {
      cached = initObjectGroupDirection(direction, cidrInUrlCategoryFlag, cacheKey);
    }
    return cached;
  }

  public ObjectGroup initObjectGroupDirection(
      Direction direction, boolean cidrInUrlCategoryFlag, String cacheKey) {

    Set<Endpoint> endpoints = getInternalEndpoints(direction);

    String assigneeGroup = null;
    Component component = getComponent();
    if (component != null && component.isContainer()) {
      ComponentInstance platform = getDeployComponentInstance();
      if (platform != null) {
        Scope scope = platform.getScope();
        if (scope != null) {
          assigneeGroup = scope.getAssigneeGroup();
        }
      }
    }
    if (assigneeGroup == null) {
      assigneeGroup = getScope().getAssigneeGroup();
    }

    String internalZoneType = getInternalZoneTypeNames(direction, true);
    if (Objects.equals(internalZoneType, "dmz_vip")) {
      internalZoneType = "internet";
    }
    String name = getObjectGroupName(direction);
    if (cidrInUrlCategoryFlag) {
      name = name + "_cidr";
    }

    ObjectGroup.TypeEnum type =
        !cidrInUrlCategoryFlag ? getObjectGroupType() : ObjectGroup.TypeEnum.ADDRESS_GROUP;

    Map<String, String> labels = new HashMap<>();
    LandingZone lz = getLandingZone();
    Target target = lz.getTarget();
    labels.put("landingzone", lz.getName());
    labels.put("landingzone_type", lz.getLandingZoneType().getName());
    labels.put("provider", target.getProvider().getName());
    labels.put("macrophase", lz.getMacroPhaseName());
    labels.put("zone_type", internalZoneType);
    labels.put("generated", "False");

    ObjectGroup objectGroup = new ObjectGroup();
    objectGroup.setName(name);
    objectGroup.setUid(UUIDUtils.generateNameBasedUUID(name));
    objectGroup.setZoneType(internalZoneType);
    objectGroup.setType(ObjectGroup.TypeEnum.valueOf(type.name()));
    objectGroup.setEndpoints(EndpointUtils.translateDataEndpointsToYamlEndpoints(endpoints));
    objectGroup.setOwner(assigneeGroup);
    objectGroup.setLabels(labels);

    objectGroupDirectionCache.put(cacheKey, objectGroup);

    return objectGroup;
  }

  public ObjectGroup.TypeEnum getObjectGroupType() {
    if (this.getEndpointType() == V1ComponentInstanceSpec.EndpointTypeEnum.UR_LLIST) {
      return ObjectGroup.TypeEnum.URL_CATEGORY;
    } else {
      return ObjectGroup.TypeEnum.ADDRESS_GROUP;
    }
  }

  public Map<Direction, ObjectGroup> getObjectGroups(boolean cidrInUrlCategoryFlag) {
    if (objectGroupsCache == null) {
      objectGroupsCache = new HashMap<>();
    }
    Map<Direction, ObjectGroup> cached = objectGroupsCache.get(cidrInUrlCategoryFlag);
    if (cached == null) {
      cached = initObjectGroups(cidrInUrlCategoryFlag);
    }

    return cached;
  }

  public Map<Direction, ObjectGroup> initObjectGroups(boolean cidrInUrlCategoryFlag) {
    Map<Direction, ObjectGroup> objectGroups = new EnumMap<>(Direction.class);
    objectGroups.put(Direction.IN, getObjectGroupDirection(Direction.IN, cidrInUrlCategoryFlag));
    objectGroups.put(Direction.OUT, getObjectGroupDirection(Direction.OUT, cidrInUrlCategoryFlag));
    objectGroupsCache.put(cidrInUrlCategoryFlag, objectGroups);
    return objectGroups;
  }

  public @Nullable ComponentInstance getHostingPlatform() {
    if (this.component != null && this.component.isContainer()) {
      return this.hostingPlatform;
    } else {
      // a hosting platform does not host itself
      return null;
    }
  }

  private String calculateObjectGroupName(Direction direction) {
    if (this.component != null && !this.component.isContainer()) {
      return calculateDefaultObjectGroupName(direction);
    }

    ComponentInstance platform = this.hostingPlatform;

    if (direction == Direction.OUT) {
      return calculateOutObjectGroupName(platform);
    }

    if (direction == Direction.IN) {
      return calculateInObjectGroupName(platform);
    }

    // when service tags are requested assume IN and OUT are the same
    return calculateDefaultObjectGroupName(direction);
  }

  // Helper for OUT direction
  private String calculateOutObjectGroupName(ComponentInstance platform) {
    if (platform.getEndpointType() == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S
        && this.outZoneInstance != null) {
      return StringUtils.normalize(
          new StringBuffer()
              .append("ag-")
              .append(platform.getName())
              .append("_out_")
              .append(this.outZoneInstance.getName())
              .toString());
    }
    return platform.getObjectGroupName(Direction.OUT);
  }

  // Helper for IN direction
  private String calculateInObjectGroupName(ComponentInstance platform) {
    if (platform.getEndpointType() == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
      if (this.inZoneInstance != null) {
        return StringUtils.normalize(
            new StringBuffer()
                .append("ag-")
                .append(platform.getName())
                .append("_in_")
                .append(this.inZoneInstance.getName())
                .toString());
      }
      return StringUtils.normalize(
          new StringBuffer()
              .append("ag-")
              .append(platform.getName())
              .append("_in-")
              .append(this.zoneType.getName())
              .toString());
    } else if (AZURE_PROVIDER.equals(this.getLandingZone().getTarget().getProvider().getName())
        && platform.isChildOfComponent("ACS")) {
      return StringUtils.normalize(
          new StringBuffer()
              .append("ag-global-")
              .append(this.getLandingZone().getName())
              .append("-")
              .append(this.zoneType.getName())
              .toString());
    }
    return platform.getObjectGroupName(Direction.IN);
  }

  // Helper for default case
  private String calculateDefaultObjectGroupName(Direction direction) {
    StringBuilder sb = new StringBuilder();
    sb.append("ag-").append(this.getName());
    if (!Objects.equals(this.inSubnetInstance, this.outSubnetInstance)
        && this.serviceTag == null
        && direction == Direction.OUT) {
      sb.append("_out");
    }
    return StringUtils.normalize(sb.toString());
  }

  public String getObjectGroupName(Direction direction) {
    return objectGroupNameCache.computeIfAbsent(direction, this::calculateObjectGroupName);
  }

  /**
   * Logic:
   *
   * <p>AND within a single selector (all entries must match)
   *
   * <p>OR across selectors (any one selector matching is sufficient)
   */
  public boolean areLabelsMatching(@Nullable Set<Map<String, String>> selectors) {
    if (selectors == null || selectors.isEmpty()) {
      return false;
    }
    Map<String, String> ciTags = getTagsAsMap();
    if (ciTags == null) {
      return false;
    }
    for (Map<String, String> selector : selectors) {
      Set<Map.Entry<String, String>> entries = selector.entrySet();
      int numberOfTagsToMatch = entries.size();
      for (Map.Entry<String, String> entry : entries) {
        // Check if the tag key exists and the value matches
        String tagValue = ciTags.get(entry.getKey());
        if (Objects.equals(tagValue, entry.getValue())) {
          numberOfTagsToMatch--;
        }
      }
      if (numberOfTagsToMatch == 0) {
        return true;
      }
    }
    return false;
  }

  public boolean isAutomaticallyInstantiated() {
    return isAutomaticallyInstantiated;
  }

  public void setAutomaticallyInstantiated(boolean automaticallyInstantiated) {
    isAutomaticallyInstantiated = automaticallyInstantiated;
  }

  public ZoneType getZoneType() {
    return zoneType;
  }

  public void setZoneType(ZoneType zoneType) {
    this.zoneType = zoneType;
  }

  public ServiceTag getServiceTag() {
    return serviceTag;
  }

  public void setServiceTag(ServiceTag serviceTag) {
    this.serviceTag = serviceTag;
  }

  public ApplicationPhase getApplicationPhase() {
    return applicationPhase;
  }

  public void setApplicationPhase(ApplicationPhase applicationPhase) {
    this.applicationPhase = applicationPhase;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }

  public SubnetInstance getInSubnetInstance() {
    return inSubnetInstance;
  }

  public void setInSubnetInstance(SubnetInstance inSubnetInstance) {
    this.inSubnetInstance = inSubnetInstance;
  }

  public SubnetInstance getOutSubnetInstance() {
    return outSubnetInstance;
  }

  public void setOutSubnetInstance(SubnetInstance outSubnetInstance) {
    this.outSubnetInstance = outSubnetInstance;
  }

  public ZoneInstance getInZoneInstance() {
    return inZoneInstance;
  }

  public void setInZoneInstance(ZoneInstance inZoneInstance) {
    this.inZoneInstance = inZoneInstance;
  }

  public ZoneInstance getOutZoneInstance() {
    return outZoneInstance;
  }

  public void setOutZoneInstance(ZoneInstance outZoneInstance) {
    this.outZoneInstance = outZoneInstance;
  }

  public Namespace getNamespace() {
    return namespace;
  }

  public void setNamespace(Namespace namespace) {
    this.namespace = namespace;
  }

  public Set<AutomaticTemplateInstance> getAutomaticTemplateInstances() {
    return automaticTemplateInstances;
  }

  public void setAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    this.automaticTemplateInstances = automaticTemplateInstances;
  }

  public void addAutomaticTemplateInstance(AutomaticTemplateInstance automaticTemplateInstance) {
    if (this.automaticTemplateInstances == null) {
      this.automaticTemplateInstances = new HashSet<>();
    }
    this.automaticTemplateInstances.add(automaticTemplateInstance);
  }

  public void addAutomaticTemplateInstances(
      Set<AutomaticTemplateInstance> automaticTemplateInstances) {
    if (this.automaticTemplateInstances == null) {
      this.automaticTemplateInstances = new HashSet<>();
    }
    this.automaticTemplateInstances.addAll(automaticTemplateInstances);
  }

  public ComponentInstance getDeployComponentInstance() {
    return hostingPlatform;
  }

  public void setDeployComponentInstance(ComponentInstance deployComponentInstance) {
    this.hostingPlatform = deployComponentInstance;
  }

  public Set<ComponentInstance> getHostedComponentInstances() {
    return hostedComponentInstances;
  }

  public void setHostedComponentInstances(Set<ComponentInstance> hostedComponentInstances) {
    this.hostedComponentInstances = hostedComponentInstances;
  }

  public void addHostedComponentInstance(ComponentInstance hostedComponentInstance) {
    if (this.hostedComponentInstances == null) {
      this.hostedComponentInstances = new HashSet<>();
    }
    this.hostedComponentInstances.add(hostedComponentInstance);
  }

  public void addHostedComponentInstances(Set<ComponentInstance> hostedComponentInstances) {
    if (this.hostedComponentInstances == null) {
      this.hostedComponentInstances = new HashSet<>();
    }
    this.hostedComponentInstances.addAll(hostedComponentInstances);
  }

  public boolean isValidZoneType(@Nullable ZoneType zoneTypeLookup, Direction direction) {
    if (zoneTypeLookup == null) {
      return false;
    }

    V1ComponentInstanceSpec spec = this.getData().getSpec();

    if (this.component.isContainer()) {
      return Objects.equals(zoneTypeLookup, this.zoneType);
    }

    if (!this.component.isHosting()) {
      if (direction == Direction.IN) {
        return Objects.equals(
            this.inSubnetInstance.getZoneInstance().getZoneType(), zoneTypeLookup);
      }
      return Objects.equals(this.outSubnetInstance.getZoneInstance().getZoneType(), zoneTypeLookup);
    } else {
      if (spec.getEndpointType() == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
        if (direction == Direction.IN) {
          return "TechnicalZone".equals(zoneTypeLookup.getName())
              || this.zoneTypes.contains(zoneTypeLookup);
        }
        return true;
      }
      return true;
    }
  }

  public Set<ZoneType> getZoneTypes() {
    return zoneTypes;
  }

  public void setZoneTypes(Set<ZoneType> zoneTypes) {
    this.zoneTypes = zoneTypes;
  }

  public void addZoneType(ZoneType zoneType) {
    if (this.zoneTypes == null) {
      this.zoneTypes = new HashSet<>();
    }
    this.zoneTypes.add(zoneType);
  }

  public void addZoneTypes(Set<ZoneType> zoneTypes) {
    if (this.zoneTypes == null) {
      this.zoneTypes = new HashSet<>();
    }
    this.zoneTypes.addAll(zoneTypes);
  }

  public Set<IPAddressInstance> getIpAddressInstances() {
    if (ipAddressInstances == null) {
      ipAddressInstances = new HashSet<>();
    }
    return ipAddressInstances;
  }

  public void setIpAddressInstances(Set<IPAddressInstance> ipAddressInstances) {
    this.ipAddressInstances = ipAddressInstances;
  }

  public void addIpAddressInstance(IPAddressInstance ipAddressInstance) {
    if (ipAddressInstances == null) {
      ipAddressInstances = new HashSet<>();
    }
    ipAddressInstances.add(ipAddressInstance);
  }

  public void addIpAddressInstances(Set<IPAddressInstance> ipAddressInstances) {
    if (this.ipAddressInstances == null) {
      this.ipAddressInstances = new HashSet<>();
    }
    this.ipAddressInstances.addAll(ipAddressInstances);
  }

  public Set<DependencyInstance> getDependencyInstances() {
    return dependencyInstances;
  }

  public void setDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    this.dependencyInstances = dependencyInstances;
  }

  public void addDependencyInstance(DependencyInstance dependencyInstance) {
    if (dependencyInstances == null) {
      dependencyInstances = new HashSet<>();
    }
    dependencyInstances.add(dependencyInstance);
  }

  public void addDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    if (this.dependencyInstances == null) {
      this.dependencyInstances = new HashSet<>();
    }
    this.dependencyInstances.addAll(dependencyInstances);
  }

  public Set<SubnetInstance> getPaasZoneOutSubnetInstances() {
    return paasZoneOutSubnetInstances;
  }

  public void setPaasZoneOutSubnetInstances(Set<SubnetInstance> paasZoneOutSubnetInstances) {
    this.paasZoneOutSubnetInstances = paasZoneOutSubnetInstances;
  }

  public void addPaasZoneOutSubnetInstance(SubnetInstance paaSZoneOutSubnetInstance) {
    if (paasZoneOutSubnetInstances == null) {
      paasZoneOutSubnetInstances = new HashSet<>();
    }
    paasZoneOutSubnetInstances.add(paaSZoneOutSubnetInstance);
  }

  public void addPaasZoneOutSubnetInstances(Set<SubnetInstance> paaSZoneOutSubnetInstances) {
    if (this.paasZoneOutSubnetInstances == null) {
      this.paasZoneOutSubnetInstances = new HashSet<>();
    }
    this.paasZoneOutSubnetInstances.addAll(paaSZoneOutSubnetInstances);
  }

  public Set<SubnetInstance> getPaasZoneInSubnetInstances() {
    return paasZoneInSubnetInstances;
  }

  public void setPaasZoneInSubnetInstances(Set<SubnetInstance> paasZoneInSubnetInstances) {
    this.paasZoneInSubnetInstances = paasZoneInSubnetInstances;
  }

  public void addPaasZoneInSubnetInstance(SubnetInstance paaSZoneInSubnetInstance) {
    if (paasZoneInSubnetInstances == null) {
      paasZoneInSubnetInstances = new HashSet<>();
    }
    paasZoneInSubnetInstances.add(paaSZoneInSubnetInstance);
  }

  public void addPaasZoneInSubnetInstances(Set<SubnetInstance> paaSZoneInSubnetInstances) {
    if (this.paasZoneInSubnetInstances == null) {
      this.paasZoneInSubnetInstances = new HashSet<>();
    }
    this.paasZoneInSubnetInstances.addAll(paaSZoneInSubnetInstances);
  }

  public @Nullable String getInternalZoneTypeNames(Direction direction) {
    return getInternalZoneTypeNames(direction, false);
  }

  public @Nullable String getInternalZoneTypeNames(Direction direction, boolean firewallUsage) {
    if (internalZoneTypeNames == null) {
      return null;
    }
    return internalZoneTypeNames.get(Objects.hash(direction, firewallUsage));
  }

  public void initInternalZoneTypeNames(
      Direction direction, boolean firewallUsage, Component linkVipComponent) {
    if (internalZoneTypeNames == null) {
      internalZoneTypeNames = new HashMap<>();
    }

    if (this.component == null) {
      return;
    }

    int key = Objects.hash(direction, firewallUsage);

    if (this.component.isContainer()) {
      String zoneTypeName = resolveContainerZoneTypeName(direction, firewallUsage);
      internalZoneTypeNames.put(key, zoneTypeName);
    } else {
      resolveNonContainerZoneTypeName(direction, linkVipComponent, key);
    }
  }

  private String resolveContainerZoneTypeName(Direction direction, boolean firewallUsage) {
    if (this.hostingPlatform == null) {
      return Objects.requireNonNullElse(this.zoneType.getName(), "");
    }

    boolean useDeployInstance =
        (direction == Direction.OUT && firewallUsage)
            || (direction == Direction.IN
                && this.hostingPlatform.getEndpointType()
                    != V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S);

    if (useDeployInstance) {
      return this.hostingPlatform.getInternalZoneTypeNames(direction);
    }

    return Objects.requireNonNullElse(this.zoneType.getName(), "");
  }

  private void resolveNonContainerZoneTypeName(
      Direction direction, Component linkVipComponent, int key) {

    String equinixZoneType = resolveEquinixZoneType(linkVipComponent);
    if (equinixZoneType != null) {
      internalZoneTypeNames.put(key, equinixZoneType);
      return;
    }

    String zoneTypeName = resolveZoneTypeByLandingZone(direction);
    internalZoneTypeNames.put(key, zoneTypeName);
  }

  private @Nullable String resolveEquinixZoneType(Component linkVipComponent) {
    if (!Objects.equals(this.getLandingZone().getTarget().getProviderName(), "Equinix")) {
      return null;
    }

    Set<Component> ancestors = this.component.getInternalAncestors();
    if (linkVipComponent == null || !ancestors.contains(linkVipComponent)) {
      return null;
    }

    if (this.ipAddressInstances == null || this.ipAddressInstances.isEmpty()) {
      return null;
    }

    V1IPAddressInstanceSpec.AdvertisedToEnum advertisedTo =
        this.ipAddressInstances.iterator().next().getAdvertisedTo();

    return advertisedTo != null ? advertisedTo.getValue() : null;
  }

  private String resolveZoneTypeByLandingZone(Direction direction) {
    return switch (this.getLandingZoneName()) {
      case Constants.INTERNET_TARGET_NAME -> "internet";
      case Constants.EXTRANET_TARGET_NAME -> "extranet";
      case Constants.STR_OFFICES -> "office";
      default -> getInternalZoneTypeNamesByDirection(direction);
    };
  }

  private String getInternalZoneTypeNamesByDirection(Direction direction) {
    return internalZoneTypeNamesByDirection.get(direction);
  }

  public void initInternalZoneTypeNamesByDirection(Direction direction, Component acsComponent) {
    if (internalZoneTypeNamesByDirection == null) {
      internalZoneTypeNamesByDirection = new HashMap<>();
    }

    // TODO: check whether this logic can be optimized

    String zoneTypeName = null;
    if (direction == Direction.OUT) {
      if (this.outZoneInstance != null) {
        zoneTypeName = this.outZoneInstance.getZoneType().getName();
      }

      if (this.outSubnetInstance != null) {
        zoneTypeName = this.outSubnetInstance.getZoneInstance().getZoneType().getName();
      } else if (this.getEndpointType() == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
        zoneTypeName = initPaaSInternalZoneTypeNamesByDirection(acsComponent);
      }
    } else {
      if (this.inZoneInstance != null) {
        zoneTypeName = this.inZoneInstance.getZoneType().getName();
      }

      if (this.inSubnetInstance != null) {
        zoneTypeName = this.inSubnetInstance.getZoneInstance().getZoneType().getName();
      } else if (this.getEndpointType() == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
        zoneTypeName = initPaaSInternalZoneTypeNamesByDirection(acsComponent);
      }
    }
    internalZoneTypeNamesByDirection.put(direction, zoneTypeName == null ? "" : zoneTypeName);
  }

  private String initPaaSInternalZoneTypeNamesByDirection(Component acsComponent) {
    if (Objects.equals(this.getScopeName(), "ACS")
        && this.component.getInternalAncestors().contains(acsComponent)) {
      return "PaaS";
    }
    // TODO: clarify why only the first element is taken
    return this.paasZoneOutSubnetInstances.stream()
        .findFirst()
        .map((SubnetInstance i) -> i.getZoneInstance().getZoneTypeName())
        .orElse(null);
  }

  public @Nullable Set<Endpoint> getInternalEndpoints(Direction direction) {
    if (internalEndpoints == null) {
      return null;
    }
    return internalEndpoints.get(direction);
  }

  public void initInternalEndpoints(
      Direction direction,
      @Nullable Component acsComponent,
      Component internetUrlComponent,
      Component extranetUrlComponent) {
    if (internalEndpoints == null) {
      internalEndpoints = new HashMap<>();
    }

    if (component.isContainer()) {
      initContainerEndpoints(direction, acsComponent);
    } else {
      initNonContainerEndpoints(internetUrlComponent, extranetUrlComponent);
    }

    internalEndpoints.computeIfAbsent(direction, k -> new HashSet<>());
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // Container Endpoints
  // ═══════════════════════════════════════════════════════════════════════════════

  private void initContainerEndpoints(Direction direction, @Nullable Component acsComponent) {
    if (hostingPlatform == null) {
      internalEndpoints.put(direction, new HashSet<>());
      return;
    }

    if (direction == Direction.IN) {
      initContainerInboundEndpoints(acsComponent);
    } else if (direction == Direction.OUT) {
      initContainerOutboundEndpoints();
    }
  }

  private void initContainerInboundEndpoints(@Nullable Component acsComponent) {
    V1ComponentInstanceSpec.EndpointTypeEnum endpointType = hostingPlatform.getEndpointType();

    if (inZoneInstance != null
        && endpointType == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
      addEndpoints(Direction.IN, inZoneInstance.getInternalEndpoints());
      return;
    }

    if (endpointType == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
      initContainerInboundZonedPaasEndpoints();
      return;
    }

    initContainerInboundDefaultEndpoints(acsComponent);
  }

  private void initContainerInboundZonedPaasEndpoints() {
    if (Objects.equals(getZoneTypeName(), "TechnicalZone")) {
      addEndpoints(Direction.IN, hostingPlatform.getInternalEndpoints(Direction.OUT));
      return;
    }

    Set<SubnetInstance> paasZoneInSubnetInstances1 = hostingPlatform.getPaasZoneInSubnetInstances();
    if (paasZoneInSubnetInstances1 == null || paasZoneInSubnetInstances1.isEmpty()) {
      return;
    }

    for (SubnetInstance subnetInstance : paasZoneInSubnetInstances1) {
      String subnetInstanceZoneTypeName = subnetInstance.getZoneInstance().getZoneTypeName();
      if (Objects.equals(getZoneTypeName(), subnetInstanceZoneTypeName)) {
        addEndpoints(Direction.IN, subnetInstance.getInternalEndpoints());
      }
    }
  }

  private void initContainerInboundDefaultEndpoints(@Nullable Component acsComponent) {
    Set<Component> ancestors = hostingPlatform.getComponent().getInternalAncestors();
    boolean isAzureAcs =
        Objects.equals(
                hostingPlatform.getLandingZone().getTarget().getProvider().getName(),
                AZURE_PROVIDER)
            && acsComponent != null
            && ancestors.contains(acsComponent);

    if (isAzureAcs) {
      for (ZoneInstance zoneInstance : hostingPlatform.getLandingZone().getZoneInstances()) {
        if (Objects.equals(zoneInstance.getZoneType(), getZoneType())) {
          for (SubnetInstance subnetInstance : zoneInstance.getSubnetInstances()) {
            addEndpoints(Direction.IN, subnetInstance.getInternalEndpoints());
          }
        }
      }
    } else {
      Set<Endpoint> endpoints = hostingPlatform.getInternalEndpoints(Direction.IN);
      if (endpoints == null) {
        Log.warnf("CI %s has no internal endpoints [IN]", hostingPlatform.getName());
      } else {
        addEndpoints(Direction.IN, endpoints);
      }
    }
  }

  private void initContainerOutboundEndpoints() {
    V1ComponentInstanceSpec.EndpointTypeEnum endpointType = hostingPlatform.getEndpointType();

    if (this.outZoneInstance != null
        && endpointType == V1ComponentInstanceSpec.EndpointTypeEnum.ZONED_PAA_S) {
      addEndpoints(Direction.OUT, this.outZoneInstance.getInternalEndpoints());
      return;
    }

    if (this.outSubnetInstance == null) {
      Set<Endpoint> endpoints = hostingPlatform.getInternalEndpoints(Direction.OUT);
      if (endpoints == null) {
        Log.warnf("CI %s has no internal endpoints [OUT]", hostingPlatform.getName());
      } else {
        addEndpoints(Direction.OUT, endpoints);
      }
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════════
  // Non-Container Endpoints
  // ═══════════════════════════════════════════════════════════════════════════════

  private void initNonContainerEndpoints(
      Component internetUrlComponent, Component extranetUrlComponent) {
    V1ComponentInstanceSpec.EndpointTypeEnum endpointType = getEndpointType();

    if (endpointType == null) {
      initDefaultEndpoints(internetUrlComponent, extranetUrlComponent);
      return;
    }

    switch (endpointType) {
      case CID_RLIST -> initCidrListEndpoints();
      case UR_LLIST -> initUrlListEndpoints();
      case ZONED_PAA_S -> initZonedPaasEndpoints();
      default -> initDefaultEndpoints(internetUrlComponent, extranetUrlComponent);
    }
  }

  private void initCidrListEndpoints() {
    List<Object> endpoints = getData().getSpec().getEndpoints();
    String name = getName();

    if (endpoints == null || endpoints.isEmpty()) {
      return;
    }

    for (Object endpoint : endpoints) {
      if (endpoint instanceof String endpointString) {
        Endpoint cidrEndpoint = new Endpoint(Endpoint.TypeEnum.CIDR, endpointString, name);
        addEndpointBothDirections(cidrEndpoint);
      }
    }
  }

  private void initUrlListEndpoints() {
    List<Object> endpoints = getData().getSpec().getEndpoints();
    String name = getName();

    if (endpoints == null || endpoints.isEmpty()) {
      return;
    }

    for (Object endpoint : endpoints) {
      if (endpoint instanceof String endpointString) {
        parseAndAddUrlEndpoint(endpointString, name, Endpoint.TypeEnum.URL);
      }
    }
  }

  private void initZonedPaasEndpoints() {
    if (this.paasZoneOutSubnetInstances == null || this.paasZoneOutSubnetInstances.isEmpty()) {
      return;
    }

    for (SubnetInstance subnetInstance : this.paasZoneOutSubnetInstances) {
      Set<Endpoint> subnetEndpoints = subnetInstance.getInternalEndpoints();
      addEndpoints(Direction.IN, subnetEndpoints);
      addEndpoints(Direction.OUT, subnetEndpoints);
    }
  }

  private void initDefaultEndpoints(
      Component internetUrlComponent, Component extranetUrlComponent) {
    initInternetUrlEndpoints(internetUrlComponent, extranetUrlComponent);
    initSubnetAndZoneEndpoints();
    initIpAddressEndpoints();
  }

  private void initInternetUrlEndpoints(
      Component internetUrlComponent, Component extranetUrlComponent) {
    Set<Component> ancestors = component.getInternalAncestors();
    String name = getName();
    boolean containsInternetUrlAncestor =
        internetUrlComponent != null
            && ancestors != null
            && ancestors.contains(internetUrlComponent);
    boolean containsExtranetUrlAncestor =
        extranetUrlComponent != null
            && ancestors != null
            && ancestors.contains(extranetUrlComponent);

    if (!containsInternetUrlAncestor && !containsExtranetUrlAncestor) {
      return;
    }

    if (INTERNET_DNS_USED_AS_SOURCE.equals(component.getName())) {
      Endpoint fqdnEndpoint = new Endpoint(Endpoint.TypeEnum.FQDN, name, this.getComment());
      addEndpointBothDirections(fqdnEndpoint);
    } else {
      parseAndAddFqdnEndpoint(name);
    }
  }

  private void parseAndAddFqdnEndpoint(String name) {
    try {
      String hostname;
      if (name.startsWith("tcp://") || name.startsWith("udp://")) {
        String withoutPrefix = name.substring(6);
        String[] parts = withoutPrefix.split(":", 2);
        hostname = parts[0];
      } else {
        URI uri = new URI(name);
        if (uri.getScheme() == null) {
          hostname = name;
          Log.warnf("URL does not contain a schema, using the name as hostname: %s", name);
        } else {
          hostname = uri.getHost();
        }
      }
      if (hostname == null) {
        throw new MalformedURLException("Invalid hostname.");
      }

      Endpoint.TypeEnum endpointType = determineEndpointType(hostname, Endpoint.TypeEnum.FQDN);
      Endpoint urlEndpoint = new Endpoint(endpointType, hostname, name);
      addEndpointBothDirections(urlEndpoint);
    } catch (URISyntaxException | MalformedURLException e) {
      Log.warnf("ComponentInstance %s has a name with invalid URL.", name, e);
    }
  }

  private void parseAndAddUrlEndpoint(
      String endpointString, String name, Endpoint.TypeEnum fallbackType) {
    try {
      URL url = new URI(endpointString).toURL();
      String hostname = url.getHost();

      if (hostname == null) {
        throw new MalformedURLException("Invalid hostname.");
      }

      Endpoint.TypeEnum endpointType = determineEndpointType(hostname, fallbackType);
      Endpoint urlEndpoint = new Endpoint(endpointType, hostname, name);
      addEndpointBothDirections(urlEndpoint);
    } catch (URISyntaxException | MalformedURLException e) {
      Log.warnf(
          e, "ComponentInstance %s has an endpoint with invalid URL: %s", name, endpointString);
    }
  }

  private static Endpoint.TypeEnum determineEndpointType(
      String hostname, Endpoint.TypeEnum fallbackType) throws MalformedURLException {
    if (InetAddresses.isInetAddress(hostname)) {
      return Endpoint.TypeEnum.CIDR;
    }
    if (hostname.equals(UrlEscapers.urlFragmentEscaper().escape(hostname))) {
      return fallbackType;
    }
    throw new MalformedURLException("Invalid hostname.");
  }

  private void initSubnetAndZoneEndpoints() {
    if (this.serviceTag != null) {
      return;
    }

    boolean isAzureProvider =
        Objects.equals(getLandingZone().getTarget().getProvider().getName(), AZURE_PROVIDER);

    initDirectionalSubnetOrZoneEndpoints(
        Direction.IN, inSubnetInstance, inZoneInstance, isAzureProvider);
    initDirectionalSubnetOrZoneEndpoints(
        Direction.OUT, outSubnetInstance, outZoneInstance, isAzureProvider);
  }

  private void initDirectionalSubnetOrZoneEndpoints(
      Direction direction,
      @Nullable SubnetInstance subnetInstance,
      @Nullable ZoneInstance zoneInstance,
      boolean isAzureProvider) {

    if (subnetInstance != null) {
      Set<Endpoint> endpoints =
          isAzureProvider
              ? subnetInstance.getZoneInstance().getInternalEndpoints()
              : subnetInstance.getInternalEndpoints();
      addEndpoints(direction, endpoints);
    } else if (zoneInstance != null) {
      addEndpoints(direction, zoneInstance.getInternalEndpoints());
    }
  }

  private void initIpAddressEndpoints() {
    if (!shouldProcessIpAddresses()) {
      return;
    }

    internalEndpoints.remove(Direction.IN);
    internalEndpoints.remove(Direction.OUT);

    String name = getName();
    for (IPAddressInstance ipAddressInstance : ipAddressInstances) {
      addIpAddressEndpoint(ipAddressInstance, name);
    }
  }

  private boolean shouldProcessIpAddresses() {
    if (ipAddressInstances == null || ipAddressInstances.isEmpty()) {
      return false;
    }

    boolean hasSubnetInstances = inSubnetInstance != null && outSubnetInstance != null;
    boolean hasZoneInstances = inZoneInstance != null && outZoneInstance != null;

    return hasSubnetInstances || hasZoneInstances || serviceTag != null;
  }

  private void addIpAddressEndpoint(IPAddressInstance ipAddressInstance, String name) {
    String ipAddressInstanceName = ipAddressInstance.getName();
    try {
      String ipAddress =
          String.join("/", InetAddresses.forString(ipAddressInstanceName).getHostAddress(), "32");

      Endpoint cidrEndpoint =
          new Endpoint(Endpoint.TypeEnum.CIDR, ipAddress, ipAddressInstance.getHost());
      addEndpointBothDirections(cidrEndpoint);
    } catch (IllegalArgumentException e) {
      Log.warnf(
          "ComponentInstance %s has an IP address with invalid format: %s, the checker should have found it, this is a bug.",
          name, ipAddressInstanceName);
    }
  }

  private void addEndpoints(Direction direction, @Nullable Set<Endpoint> endpoints) {
    if (endpoints != null) {
      internalEndpoints.computeIfAbsent(direction, d -> new HashSet<>()).addAll(endpoints);
    }
  }

  private void addEndpointBothDirections(Endpoint endpoint) {
    internalEndpoints.computeIfAbsent(Direction.IN, d -> new HashSet<>()).add(endpoint);
    internalEndpoints.computeIfAbsent(Direction.OUT, d -> new HashSet<>()).add(endpoint);
  }

  public void initExtraTags() {
    addTag(
        Map.of(
            KEY, "aud/securityzone",
            VALUE, Optional.ofNullable(getInternalZoneTypeNames(Direction.OUT)).orElse("unknown"),
            IS_INHERITED, String.valueOf(false),
            IS_GENERATED, String.valueOf(true)));
    LandingZone landingZone = getLandingZone();
    Target target = landingZone.getTarget();
    addTag(
        Map.of(
            KEY, "aud/provider",
            VALUE, target.getProviderName(),
            IS_INHERITED, String.valueOf(false),
            IS_GENERATED, String.valueOf(true)));
    addTag(
        Map.of(
            KEY, "aud/target",
            VALUE, landingZone.getTargetName(),
            IS_INHERITED, String.valueOf(false),
            IS_GENERATED, String.valueOf(true)));
    addTag(
        Map.of(
            KEY, "aud/landingzone",
            VALUE, landingZone.getName(),
            IS_INHERITED, String.valueOf(false),
            IS_GENERATED, String.valueOf(true)));
    addTag(
        Map.of(
            KEY, "aud/macrophase",
            VALUE, landingZone.getMacroPhaseName(),
            IS_INHERITED, String.valueOf(false),
            IS_GENERATED, String.valueOf(true)));
  }

  public void inheritTagsFromComponent() {
    if (component == null) {
      return;
    }

    Set<Map<String, String>> componentTags = component.getTags();
    if (componentTags == null) {
      return;
    }

    Map<String, String> currentTagsAsMap = this.getTagsAsMap();

    for (Map<String, String> tag : componentTags) {
      processInheritedTag(tag, currentTagsAsMap);
    }
  }

  private void processInheritedTag(
      Map<String, String> tag, @Nullable Map<String, String> currentTagsAsMap) {
    String key = tag.get(KEY);
    String value = tag.get(VALUE);

    if ("aud_interface".equals(key)) {
      return;
    }

    String currentValue = currentTagsAsMap != null ? currentTagsAsMap.get(key) : null;

    if (currentValue == null) {
      addTag(Map.of(KEY, key, VALUE, value, IS_INHERITED, "true"));
    } else if (!Objects.equals(currentValue, value)) {
      addTag(Map.of(KEY, String.join("/", "discarded", key), VALUE, value, IS_INHERITED, "true"));
    }
  }

  public List<Map<String, String>> getSimpleAutomaticTemplateInstances() {
    return getSimpleJsonRepresentation(getAutomaticTemplateInstances());
  }

  public List<Map<String, String>> getSimpleHostedComponentInstances() {
    return getSimpleJsonRepresentation(getHostedComponentInstances());
  }

  public @Nullable String getHostingPlatformName() {
    if (getHostingPlatform() == null) {
      return null;
    }
    return getHostingPlatform().getName();
  }

  public Map<String, List<CidrRange>> getCidrRanges() {
    Map<String, List<CidrRange>> result = new LinkedHashMap<>();

    Map<String, CidrRange> inBucket = new LinkedHashMap<>();
    addSubnetCidr(inBucket, inSubnetInstance);
    if (paasZoneInSubnetInstances != null) {
      for (SubnetInstance subnet : paasZoneInSubnetInstances) {
        addSubnetCidr(inBucket, subnet);
      }
    }
    addZoneSubnetCidrs(inBucket, inZoneInstance);
    addHostingPlatformSubnetCidrs(inBucket);
    addIpAddressCidrs(inBucket);
    if (!inBucket.isEmpty()) {
      result.put("in", sortedCidrRanges(inBucket));
    }

    Map<String, CidrRange> outBucket = new LinkedHashMap<>();
    addSubnetCidr(outBucket, outSubnetInstance);
    if (paasZoneOutSubnetInstances != null) {
      for (SubnetInstance subnet : paasZoneOutSubnetInstances) {
        addSubnetCidr(outBucket, subnet);
      }
    }
    addZoneSubnetCidrs(outBucket, outZoneInstance);
    addHostingPlatformSubnetCidrs(outBucket);
    addIpAddressCidrs(outBucket);
    if (!outBucket.isEmpty()) {
      result.put("out", sortedCidrRanges(outBucket));
    }

    return result;
  }

  private static List<CidrRange> sortedCidrRanges(Map<String, CidrRange> bucket) {
    List<String> sortedCidrs = IPv4Utils.sortedCidrs(bucket.keySet());
    List<CidrRange> ranges = new ArrayList<>(sortedCidrs.size());
    for (String cidr : sortedCidrs) {
      ranges.add(bucket.get(cidr));
    }
    return ranges;
  }

  private static void addSubnetCidr(
      Map<String, CidrRange> bucket, @Nullable SubnetInstance subnet) {
    if (subnet == null || subnet.getCidr() == null) {
      return;
    }
    String cidr = subnet.getCidr().strip();
    if (cidr.isEmpty()) {
      return;
    }
    bucket.putIfAbsent(cidr, new CidrRange(cidr, subnet.getName()));
  }

  private static void addZoneSubnetCidrs(
      Map<String, CidrRange> bucket, @Nullable ZoneInstance zoneInstance) {
    if (zoneInstance == null || zoneInstance.getSubnetInstances() == null) {
      return;
    }
    for (SubnetInstance subnet : zoneInstance.getSubnetInstances()) {
      addSubnetCidr(bucket, subnet);
    }
  }

  private void addHostingPlatformSubnetCidrs(Map<String, CidrRange> bucket) {
    if (hostingPlatform == null
        || hostingPlatform.getLandingZone() == null
        || hostingPlatform.getLandingZone().getZoneInstances() == null) {
      return;
    }
    for (ZoneInstance zone : hostingPlatform.getLandingZone().getZoneInstances()) {
      addZoneSubnetCidrs(bucket, zone);
    }
  }

  private void addIpAddressCidrs(Map<String, CidrRange> bucket) {
    addIpAddressCidrs(bucket, this);
    if (hostingPlatform != null) {
      addIpAddressCidrs(bucket, hostingPlatform);
    }
  }

  private static void addIpAddressCidrs(
      Map<String, CidrRange> bucket, ComponentInstance componentInstance) {
    if (componentInstance.ipAddressInstances == null) {
      return;
    }
    for (IPAddressInstance ipAddressInstance : componentInstance.ipAddressInstances) {
      String address = ipAddressInstance.getName();
      if (address == null || address.isBlank()) {
        continue;
      }
      try {
        String value = String.join("/", InetAddresses.forString(address).getHostAddress(), "32");
        bucket.putIfAbsent(value, new CidrRange(value, ipAddressInstance.getHost()));
      } catch (IllegalArgumentException e) {
        Log.warnf(
            "ComponentInstance %s has an IP address with invalid format: %s, the checker should have found it, this is a bug.",
            componentInstance.getName(), address);
      }
    }
  }

  public record CidrRange(String value, String name) {}

  public synchronized void initAncestorNames() {
    if (ancestorNames == null || ancestorNames.isEmpty()) {
      this.ancestorNames = new HashSet<>();
      if (this.component != null) {
        for (Component ancestor : this.component.getInternalAncestors()) {
          this.ancestorNames.add(ancestor.getName());
        }
      }
    }
  }

  /**
   * @return a LinkedHashSet to ensure order is preserved
   */
  public LinkedHashSet<String> componentAndAncestorNames() {
    LinkedHashSet<String> names = new LinkedHashSet<>();
    names.add(this.component.getName());
    names.addAll(this.ancestorNames);
    return names;
  }
}

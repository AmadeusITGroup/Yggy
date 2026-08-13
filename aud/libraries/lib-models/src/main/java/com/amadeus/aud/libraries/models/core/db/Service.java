package com.amadeus.aud.libraries.models.core.db;

import static com.amadeus.aud.libraries.models.core.db.base.BaseInstance.ID_SEPARATOR;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.models.core.utils.PortUtils;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import com.amadeus.aud.libraries.yaml.models.V1Service;
import com.amadeus.aud.libraries.yaml.models.V1ServiceSpec;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Service extends AbstractBase<V1Service> implements WithDeprecation {

  public static final String ALL_WILD_CARD = "*";

  @ManyToOne(targetEntity = Component.class, cascade = CascadeType.ALL)
  private Component component;

  @ManyToOne(targetEntity = ApplicationId.class, cascade = CascadeType.ALL)
  private ApplicationId applicationId;

  @Transient private RangeSet<Integer> approvedPorts;

  private V1ServiceSpec.ProtocolEnum approvedProtocol;

  private String serviceNowId;

  @ManyToMany(mappedBy = "services")
  private Set<DependencyInstance> dependencyInstances;

  private String ports;

  private V1ServiceSpec.ProtocolEnum protocol;

  @Transient private RangeSet<Integer> portsRange;

  public Service(V1Service service) {
    super(
        service,
        Objects.requireNonNull(
                Objects.requireNonNull(service, "service cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
  }

  public Service() {
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
    Service that = (Service) obj;
    return super.equals(that)
        && Objects.equals(approvedPorts, that.approvedPorts)
        && Objects.equals(approvedProtocol, that.approvedProtocol)
        && Objects.equals(serviceNowId, that.serviceNowId)
        && Objects.equals(ports, that.ports)
        && Objects.equals(protocol, that.protocol)
        && Objects.equals(portsRange, that.portsRange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        getApprovedPorts(),
        getApprovedProtocol(),
        getServiceNowId(),
        getPorts(),
        getProtocol(),
        getPortsRange());
  }

  @Override
  public @Nullable DeprecatedStatus getDeprecatedStatus() {
    return getData().getSpec().getDeprecatedStatus();
  }

  @Override
  public @Nullable String getDeprecatedMessage() {
    return getData().getSpec().getDeprecatedMessage();
  }

  public boolean isPrivate() {
    return Boolean.TRUE.equals(getData().getSpec().getIsPrivate());
  }

  public boolean isPci() {
    return getData().getSpec().getIsPCI();
  }

  public boolean isPii() {
    return getData().getSpec().getIsPII();
  }

  public boolean isEncrypted() {
    return getData().getSpec().getIsEncrypted();
  }

  public boolean isDbSynchro() {
    return getData().getSpec().getIsDBSynchro();
  }

  public @Nullable String getApplicationIdName() {
    if (applicationId == null) {
      return null;
    }
    return applicationId.getName();
  }

  public @Nullable String getComponentName() {
    if (component == null) {
      return null;
    }
    return component.getName();
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
    this.setUniqueIdFrom(String.join(ID_SEPARATOR, component.getName(), getName()));
  }

  public ApplicationId getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(ApplicationId applicationId) {
    this.applicationId = applicationId;
  }

  public void setApprovedPorts(RangeSet<Integer> approvedPorts) {
    this.approvedPorts = approvedPorts;
  }

  public @Nullable RangeSet<Integer> getApprovedPorts() {
    return approvedPorts;
  }

  public void addApprovedPort(Range<Integer> approvedPort) {
    if (this.approvedPorts == null) {
      this.approvedPorts = TreeRangeSet.create();
    }
    this.approvedPorts.add(approvedPort);
  }

  public void addApprovedPorts(RangeSet<Integer> approvedPorts) {
    if (this.approvedPorts == null) {
      this.approvedPorts = TreeRangeSet.create();
    }
    this.approvedPorts.addAll(approvedPorts);
  }

  public V1ServiceSpec.ProtocolEnum getApprovedProtocol() {
    return approvedProtocol;
  }

  public void setApprovedProtocol(V1ServiceSpec.ProtocolEnum approvedProtocol) {
    this.approvedProtocol = approvedProtocol;
  }

  public String getServiceNowId() {
    return serviceNowId;
  }

  public void setServiceNowId(String serviceNowId) {
    this.serviceNowId = serviceNowId;
  }

  public Set<DependencyInstance> getDependencyInstances() {
    return dependencyInstances;
  }

  public void setDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    this.dependencyInstances = dependencyInstances;
  }

  public void addDependencyInstance(DependencyInstance dependencyInstance) {
    if (this.dependencyInstances == null) {
      this.dependencyInstances = new HashSet<>();
    }
    this.dependencyInstances.add(dependencyInstance);
  }

  public void addDependencyInstances(Set<DependencyInstance> dependencyInstances) {
    if (this.dependencyInstances == null) {
      this.dependencyInstances = new HashSet<>();
    }
    this.dependencyInstances.addAll(dependencyInstances);
  }

  public void setProtocol(V1ServiceSpec.ProtocolEnum protocol) {
    this.protocol = protocol;
  }

  public V1ServiceSpec.ProtocolEnum getProtocol() {
    if (this.protocol == null) {
      return getData().getSpec().getProtocol();
    }
    return this.protocol;
  }

  public RangeSet<Integer> getPortsRange() {
    return portsRange;
  }

  public void setPortsRange(RangeSet<Integer> portsRange) {
    this.portsRange = ImmutableRangeSet.copyOf(portsRange);
    this.ports = PortUtils.toString(portsRange);
  }

  public String getPorts() {
    return ports;
  }

  public void setPorts(String ports) {
    setPortsRange(PortUtils.toRangeSet(ports));
  }

  @Override
  public String id() {
    return this.getComponentName() + ID_SEPARATOR + this.getName();
  }
}

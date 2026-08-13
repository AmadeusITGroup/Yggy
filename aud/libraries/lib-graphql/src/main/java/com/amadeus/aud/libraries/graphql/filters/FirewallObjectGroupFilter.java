package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.Endpoint;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(FirewallObjectGroupFilter.NAME)
public class FirewallObjectGroupFilter {
  public static final String NAME = "FirewallObjectGroupFilter";
  public static final String PROVIDER = "firewallObjectGroups";
  private StringFilter name;
  private StringFilter uid;
  private StringFilter type;
  private StringFilter zoneType;
  private StringFilter owner;
  private List<EndpointFilter> endpoints = Collections.emptyList();
  private List<LabelFilter> labels = Collections.emptyList();

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setUid(StringFilter uid) {
    this.uid = uid;
  }

  public void setType(StringFilter type) {
    this.type = type;
  }

  public void setZoneType(StringFilter zoneType) {
    this.zoneType = zoneType;
  }

  public void setOwner(StringFilter owner) {
    this.owner = owner;
  }

  public void setEndpoints(List<EndpointFilter> endpoints) {
    this.endpoints = endpoints != null ? endpoints : Collections.emptyList();
  }

  public void setLabels(List<LabelFilter> labels) {
    this.labels = labels != null ? labels : Collections.emptyList();
  }

  public StringFilter getName() {
    return name;
  }

  public StringFilter getUid() {
    return uid;
  }

  public StringFilter getType() {
    return type;
  }

  public StringFilter getZoneType() {
    return zoneType;
  }

  public StringFilter getOwner() {
    return owner;
  }

  public List<EndpointFilter> getEndpoints() {
    return endpoints;
  }

  public List<LabelFilter> getLabels() {
    return labels;
  }

  public boolean matches(@Nullable ObjectGroup objectGroup) {
    if (objectGroup == null) {
      return false;
    }

    return (name == null || name.matches(objectGroup.getName()))
        && (uid == null || uid.matches(objectGroup.getUid()))
        && (type == null
            || type.matches(
                objectGroup.getType() != null ? objectGroup.getType().getValue() : null))
        && (zoneType == null || zoneType.matches(objectGroup.getZoneType()))
        && (owner == null || owner.matches(objectGroup.getOwner()))
        && matchesAllEndpoints(objectGroup.getEndpoints())
        && FilterUtils.allLabelsMatch(labels, objectGroup.getLabels());
  }

  /**
   * All endpoint filters must match at least one endpoint in the object group. If no endpoint
   * filters are specified, this returns true.
   */
  private boolean matchesAllEndpoints(List<Endpoint> objectGroupEndpoints) {
    if (endpoints.isEmpty()) {
      return true;
    }
    if (objectGroupEndpoints == null || objectGroupEndpoints.isEmpty()) {
      return false;
    }

    // Each endpoint filter must match at least one endpoint
    for (EndpointFilter endpointFilter : endpoints) {
      boolean filterMatched = false;
      for (Endpoint endpoint : objectGroupEndpoints) {
        if (endpointFilter.matches(endpoint)) {
          filterMatched = true;
          break;
        }
      }
      if (!filterMatched) {
        return false;
      }
    }
    return true;
  }
}

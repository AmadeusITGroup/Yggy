package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input(BlacklistFilter.NAME)
public class BlacklistFilter {
  public static final String NAME = "BlacklistFilter";
  public static final String PROVIDER = "blacklists";

  private StringFilter id;
  private StringFilter name;
  private ScopeFilter scope;
  private StringFilter sourceComponent;
  private List<String> destinationHostFqdns;
  private List<String> destinationHostCidrs;
  private List<String> sourceZones;
  private List<String> sourceMacrophases;
  private List<String> sourceTargets;
  private List<String> destinationApplicationProtocols;
  private List<String> destinationTransportProtocols;
  private List<String> destinationPortRanges;

  // Setters
  public void setId(StringFilter id) {
    this.id = id;
  }

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setScope(ScopeFilter scope) {
    this.scope = scope;
  }

  public void setSourceComponent(StringFilter sourceComponent) {
    this.sourceComponent = sourceComponent;
  }

  public void setDestinationHostFqdns(List<String> destinationHostFqdns) {
    this.destinationHostFqdns = destinationHostFqdns;
  }

  public void setDestinationHostCidrs(List<String> destinationHostCidrs) {
    this.destinationHostCidrs = destinationHostCidrs;
  }

  public void setSourceZones(List<String> sourceZones) {
    this.sourceZones = sourceZones;
  }

  public void setSourceMacrophases(List<String> sourceMacrophases) {
    this.sourceMacrophases = sourceMacrophases;
  }

  public void setSourceTargets(List<String> sourceTargets) {
    this.sourceTargets = sourceTargets;
  }

  public void setDestinationApplicationProtocols(List<String> destinationApplicationProtocols) {
    this.destinationApplicationProtocols = destinationApplicationProtocols;
  }

  public void setDestinationTransportProtocols(List<String> destinationTransportProtocols) {
    this.destinationTransportProtocols = destinationTransportProtocols;
  }

  public void setDestinationPortRanges(List<String> destinationPortRanges) {
    this.destinationPortRanges = destinationPortRanges;
  }

  // Getters
  public StringFilter getId() {
    return id;
  }

  public StringFilter getName() {
    return name;
  }

  public ScopeFilter getScope() {
    return scope;
  }

  public StringFilter getSourceComponent() {
    return sourceComponent;
  }

  public List<String> getDestinationHostFqdns() {
    return destinationHostFqdns;
  }

  public List<String> getDestinationHostCidrs() {
    return destinationHostCidrs;
  }

  public List<String> getSourceZones() {
    return sourceZones;
  }

  public List<String> getSourceMacrophases() {
    return sourceMacrophases;
  }

  public List<String> getSourceTargets() {
    return sourceTargets;
  }

  public List<String> getDestinationApplicationProtocols() {
    return destinationApplicationProtocols;
  }

  public List<String> getDestinationTransportProtocols() {
    return destinationTransportProtocols;
  }

  public List<String> getDestinationPortRanges() {
    return destinationPortRanges;
  }

  public boolean matches(@Nullable Blacklist blacklist) {
    return blacklist != null
        && (id == null || id.matches(blacklist.id()))
        && (name == null || name.matches(blacklist.getName()))
        && (scope == null || scope.matches(blacklist.getScope()))
        && (sourceComponent == null || sourceComponent.matches(blacklist.getSourceComponent()))
        && matchesAnyInList(destinationHostFqdns, blacklist.getDestinationHostFqdns())
        && matchesAnyInList(destinationHostCidrs, blacklist.getDestinationHostCidrs())
        && matchesAnyInList(sourceZones, blacklist.getSourceZones())
        && matchesAnyInList(sourceMacrophases, blacklist.getSourceMacrophases())
        && matchesAnyInList(sourceTargets, blacklist.getSourceTargets())
        && matchesAnyInList(
            destinationApplicationProtocols, blacklist.getDestinationApplicationProtocols())
        && matchesAnyInList(
            destinationTransportProtocols, blacklist.getDestinationTransportProtocols())
        && matchesAnyInList(destinationPortRanges, blacklist.getDestinationPortRanges());
  }

  private boolean matchesAnyInList(
      @Nullable List<String> filterList, @Nullable List<String> targetList) {
    // If no filter is specified, accept all
    if (filterList == null || filterList.isEmpty()) {
      return true;
    }
    // If target list is empty but filter is specified, no match
    if (targetList == null || targetList.isEmpty()) {
      return false;
    }
    // Check if any item in filter matches any item in target
    for (String filterItem : filterList) {
      for (String targetItem : targetList) {
        if (targetItem.equalsIgnoreCase(filterItem)) {
          return true;
        }
      }
    }
    return false;
  }
}

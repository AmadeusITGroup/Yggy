package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.FilterUtils;
import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.ConnectivityRule;
import jakarta.annotation.Nullable;
import java.util.List;
import org.eclipse.microprofile.graphql.Input;

@Input("ConnectivityRuleFilter")
public class ConnectivityRuleFilter {

  /** Substring match on rule name (case-insensitive). */
  private StringFilter name;

  /** Exact match on protocol value, e.g. "Tcp", "Udp", "Icmp", "*". */
  private StringFilter protocol;

  /** Exact match on owner (case-insensitive). */
  private StringFilter owner;

  /** Exact match on scope (case-insensitive). */
  private StringFilter scope;

  /** Filter on srcObjectGroup fields (name, type, zoneType, endpoints, labels, etc.). */
  private FirewallObjectGroupFilter srcObjectGroup;

  /** Filter on dstObjectGroup fields (name, type, zoneType, endpoints, labels, etc.). */
  private FirewallObjectGroupFilter dstObjectGroup;

  /** All ports in this list must be present in the rule's ports list. */
  private List<String> ports;

  /** Every entry must match a key/value pair in the rule's label map. */
  private List<LabelFilter> labels;

  // ---- setters ----

  public void setName(StringFilter name) {
    this.name = name;
  }

  public void setProtocol(StringFilter protocol) {
    this.protocol = protocol;
  }

  public void setOwner(StringFilter owner) {
    this.owner = owner;
  }

  public void setScope(StringFilter scope) {
    this.scope = scope;
  }

  public void setSrcObjectGroup(FirewallObjectGroupFilter srcObjectGroup) {
    this.srcObjectGroup = srcObjectGroup;
  }

  public void setDstObjectGroup(FirewallObjectGroupFilter dstObjectGroup) {
    this.dstObjectGroup = dstObjectGroup;
  }

  public void setPorts(List<String> ports) {
    this.ports = ports;
  }

  public void setLabels(List<LabelFilter> labels) {
    this.labels = labels;
  }

  // ---- getters ----

  public StringFilter getName() {
    return name;
  }

  public StringFilter getProtocol() {
    return protocol;
  }

  public StringFilter getOwner() {
    return owner;
  }

  public StringFilter getScope() {
    return scope;
  }

  public FirewallObjectGroupFilter getSrcObjectGroup() {
    return srcObjectGroup;
  }

  public FirewallObjectGroupFilter getDstObjectGroup() {
    return dstObjectGroup;
  }

  public List<String> getPorts() {
    return ports;
  }

  public List<LabelFilter> getLabels() {
    return labels;
  }

  // ---- matching logic ----

  @SuppressWarnings("NullAway")
  public boolean matches(@Nullable ConnectivityRule rule) {
    return rule != null
        && (name == null || name.matches(rule.getName()))
        && (protocol == null
            || protocol.matches(rule.getProtocol() != null ? rule.getProtocol().getValue() : null))
        && (owner == null || owner.matches(rule.getOwner()))
        && (scope == null || scope.matches(rule.getScope()))
        && (srcObjectGroup == null || srcObjectGroup.matches(rule.getSrcObjectGroup()))
        && (dstObjectGroup == null || dstObjectGroup.matches(rule.getDstObjectGroup()))
        && matchesAllPorts(rule.getPorts())
        && FilterUtils.allLabelsMatch(labels, rule.getLabels());
  }

  private boolean matchesAllPorts(@Nullable List<String> rulePorts) {
    if (ports == null || ports.isEmpty()) {
      return true;
    }
    if (rulePorts == null || rulePorts.isEmpty()) {
      return false;
    }

    return rulePorts.containsAll(ports);
  }
}

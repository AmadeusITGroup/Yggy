package com.amadeus.aud.libraries.models.core.db;

import com.amadeus.aud.libraries.models.core.db.base.AbstractBase;
import com.amadeus.aud.libraries.yaml.models.V1Blacklist;
import com.amadeus.aud.libraries.yaml.models.V1BlacklistSpec;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "blacklist")
public class Blacklist extends AbstractBase<V1Blacklist> {

  public Blacklist(V1Blacklist blacklist) {
    super(
        blacklist,
        Objects.requireNonNull(
                Objects.requireNonNull(blacklist, "blacklist cannot be null").getMetadata(),
                "metadata cannot be null")
            .getName());
    Objects.requireNonNull(blacklist.getSpec(), "blacklist spec cannot be null");
  }

  public Blacklist() {
    super();
  }

  public String getSourceComponent() {
    return getData().getSpec().getSourceComponent();
  }

  public List<String> getDestinationHostFqdns() {
    return copyList(getData().getSpec().getDestinationHostFqdns());
  }

  public List<String> getDestinationHostCidrs() {
    return copyList(getData().getSpec().getDestinationHostCidrs());
  }

  public List<String> getSourceZones() {
    return copyList(getData().getSpec().getSourceZones());
  }

  public List<String> getSourceMacrophases() {
    return copyList(getData().getSpec().getSourceMacrophases());
  }

  public List<String> getSourceTargets() {
    return copyList(getData().getSpec().getSourceTargets());
  }

  public List<String> getDestinationApplicationProtocols() {
    return mapApplicationProtocols(getData().getSpec().getDestinationApplicationProtocols());
  }

  public List<String> getDestinationTransportProtocols() {
    return mapTransportProtocols(getData().getSpec().getDestinationTransportProtocols());
  }

  public List<String> getDestinationPortRanges() {
    return copyList(getData().getSpec().getDestinationPortRanges());
  }

  private static <T> List<T> copyList(List<T> values) {
    return values == null ? null : new ArrayList<>(values);
  }

  private static List<String> mapApplicationProtocols(
      List<V1BlacklistSpec.DestinationApplicationProtocolsEnum> protocols) {
    if (protocols == null) {
      return List.of();
    }
    List<String> mapped = new ArrayList<>(protocols.size());
    for (V1BlacklistSpec.DestinationApplicationProtocolsEnum protocol : protocols) {
      mapped.add(protocol.getValue());
    }
    return mapped;
  }

  private static List<String> mapTransportProtocols(
      List<V1BlacklistSpec.DestinationTransportProtocolsEnum> protocols) {
    if (protocols == null) {
      return List.of();
    }
    List<String> mapped = new ArrayList<>(protocols.size());
    for (V1BlacklistSpec.DestinationTransportProtocolsEnum protocol : protocols) {
      mapped.add(protocol.getValue());
    }
    return mapped;
  }
}

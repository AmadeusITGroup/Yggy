package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.ComponentInstance;
import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.DeprecatedStatus;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SubnetInstanceFilterTest {

  private StringFilter sf(String value) {
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike(value);
    return stringFilter;
  }

  private SubnetInstance subnet(String name) {
    SubnetInstance subnetInstance = mock(SubnetInstance.class);
    when(subnetInstance.id()).thenReturn("subnet-" + name.hashCode());
    when(subnetInstance.getName()).thenReturn(name);
    when(subnetInstance.getScopeName()).thenReturn("scope-prod");
    when(subnetInstance.getLandingZoneName()).thenReturn("lz-ams");
    when(subnetInstance.getVlan()).thenReturn("123");
    when(subnetInstance.isShared()).thenReturn(true);
    when(subnetInstance.isGroup()).thenReturn(false);
    when(subnetInstance.isNetworkPrivate()).thenReturn(true);
    when(subnetInstance.getCidr()).thenReturn("10.0.0.0/24");
    when(subnetInstance.getSubnetTypeName()).thenReturn("Private");
    when(subnetInstance.getZoneInstanceName()).thenReturn("zi-main");
    when(subnetInstance.getParentName()).thenReturn("parent-a");
    when(subnetInstance.getDeprecatedStatus()).thenReturn(DeprecatedStatus.WARNING);
    when(subnetInstance.getDeprecatedMessage()).thenReturn("deprecated soon");

    SubnetInstance child = mock(SubnetInstance.class);
    when(child.getName()).thenReturn("child-a");
    when(subnetInstance.getChildren()).thenReturn(Set.of(child));

    ComponentInstance componentInstance = mock(ComponentInstance.class);
    when(componentInstance.getName()).thenReturn("ci-app");
    when(subnetInstance.getComponentInstances()).thenReturn(Set.of(componentInstance));

    return subnetInstance;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new SubnetInstanceFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new SubnetInstanceFilter().matches(subnet("subnet-a"))).isTrue();
  }

  @Test
  void filterByScope_match() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setScope(sf("SCOPE-PROD"));
    assertThat(filter.matches(subnet("subnet-a"))).isTrue();
  }

  @Test
  void filterByScope_noMatch() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setScope(sf("scope-dev"));
    assertThat(filter.matches(subnet("subnet-a"))).isFalse();
  }

  @Test
  void filterByLandingZoneName_match() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setLandingZoneName(sf("lz-ams"));
    assertThat(filter.matches(subnet("subnet-a"))).isTrue();
  }

  @Test
  void filterByIsShared_noMatch() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setIsShared(false);
    assertThat(filter.matches(subnet("subnet-a"))).isFalse();
  }

  @Test
  void filterByIsGroup_match() {
    SubnetInstance subnetInstance = subnet("subnet-a");
    when(subnetInstance.isGroup()).thenReturn(true);

    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setIsGroup(true);
    assertThat(filter.matches(subnetInstance)).isTrue();
  }

  @Test
  void filterByCidr_match() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setCidr(sf("10.0.0.0/24"));
    assertThat(filter.matches(subnet("subnet-a"))).isTrue();
  }

  @Test
  void filterByComponentInstanceName_match() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setComponentInstanceName(sf("ci-app"));
    assertThat(filter.matches(subnet("subnet-a"))).isTrue();
  }

  @Test
  void filterByChildName_noMatch() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setChildName(sf("child-z"));
    assertThat(filter.matches(subnet("subnet-a"))).isFalse();
  }

  @Test
  void filterByDeprecatedStatus_match() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    filter.setDeprecatedStatus(sf("WARNING"));
    assertThat(filter.matches(subnet("subnet-a"))).isTrue();
  }

  @Test
  void getters_returnSetValues() {
    SubnetInstanceFilter filter = new SubnetInstanceFilter();
    StringFilter idFilter = sf("subnet-");
    StringFilter nameFilter = sf("subnet-a");
    StringFilter scopeFilter = sf("scope-prod");
    StringFilter landingZoneNameFilter = sf("lz-ams");
    StringFilter vlanFilter = sf("123");
    StringFilter cidrFilter = sf("10.0.0.0/24");
    StringFilter subnetTypeNameFilter = sf("private");
    StringFilter zoneInstanceNameFilter = sf("zi-main");
    StringFilter parentNameFilter = sf("parent-a");
    StringFilter deprecatedStatusFilter = sf("warning");
    StringFilter deprecatedMessageFilter = sf("deprecated");
    StringFilter childNameFilter = sf("child-a");
    StringFilter componentInstanceNameFilter = sf("ci-app");

    filter.setId(idFilter);
    filter.setName(nameFilter);
    filter.setScope(scopeFilter);
    filter.setLandingZoneName(landingZoneNameFilter);
    filter.setVlan(vlanFilter);
    filter.setIsShared(true);
    filter.setIsGroup(false);
    filter.setIsNetworkPrivate(true);
    filter.setCidr(cidrFilter);
    filter.setSubnetTypeName(subnetTypeNameFilter);
    filter.setZoneInstanceName(zoneInstanceNameFilter);
    filter.setParentName(parentNameFilter);
    filter.setDeprecatedStatus(deprecatedStatusFilter);
    filter.setDeprecatedMessage(deprecatedMessageFilter);
    filter.setChildName(childNameFilter);
    filter.setComponentInstanceName(componentInstanceNameFilter);

    assertThat(filter.getId()).isEqualTo(idFilter);
    assertThat(filter.getName()).isEqualTo(nameFilter);
    assertThat(filter.getScope()).isEqualTo(scopeFilter);
    assertThat(filter.getLandingZoneName()).isEqualTo(landingZoneNameFilter);
    assertThat(filter.getVlan()).isEqualTo(vlanFilter);
    assertThat(filter.getIsShared()).isTrue();
    assertThat(filter.getIsGroup()).isFalse();
    assertThat(filter.getIsNetworkPrivate()).isTrue();
    assertThat(filter.getCidr()).isEqualTo(cidrFilter);
    assertThat(filter.getSubnetTypeName()).isEqualTo(subnetTypeNameFilter);
    assertThat(filter.getZoneInstanceName()).isEqualTo(zoneInstanceNameFilter);
    assertThat(filter.getParentName()).isEqualTo(parentNameFilter);
    assertThat(filter.getDeprecatedStatus()).isEqualTo(deprecatedStatusFilter);
    assertThat(filter.getDeprecatedMessage()).isEqualTo(deprecatedMessageFilter);
    assertThat(filter.getChildName()).isEqualTo(childNameFilter);
    assertThat(filter.getComponentInstanceName()).isEqualTo(componentInstanceNameFilter);
  }
}

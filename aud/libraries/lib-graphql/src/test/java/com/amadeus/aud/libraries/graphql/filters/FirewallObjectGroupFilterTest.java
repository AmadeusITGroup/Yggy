package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.Endpoint;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FirewallObjectGroupFilterTest {

  private static ObjectGroup buildObjectGroup() {
    Endpoint ep = new Endpoint();
    ep.setType("CIDR");
    ep.setValue("10.0.0.0/8");

    ObjectGroup og = new ObjectGroup();
    og.setName("ag-web-servers");
    og.setUid("uid-001");
    og.setType(ObjectGroup.TypeEnum.ADDRESS_GROUP);
    og.setZoneType("INTERNAL");
    og.setEndpoints(List.of(ep));
    og.setOwner("team-infra");
    og.setLabels(Map.of("env", "prod", "tier", "web"));
    return og;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new FirewallObjectGroupFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new FirewallObjectGroupFilter().matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByName_match() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("web");
    f.setName(stringFilter);
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("database");
    f.setName(stringFilter);
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByUid_match() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("uid-001");
    f.setUid(sf);
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByUid_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("uid-999");
    f.setUid(sf);
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByType_match() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("AddressGroup");
    f.setType(sf);
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByType_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("UrlCategory");
    f.setType(sf);
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByType_nullTypeOnObjectGroup_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("AddressGroup");
    f.setType(sf);
    ObjectGroup og = buildObjectGroup();
    og.setType(null);
    assertThat(f.matches(og)).isFalse();
  }

  @Test
  void filterByZoneType_match() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("INTERNAL");
    f.setZoneType(sf);
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByZoneType_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("EXTERNAL");
    f.setZoneType(sf);
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByZoneType_nullZoneTypeOnObjectGroup_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("INTERNAL");
    f.setZoneType(sf);
    ObjectGroup og = buildObjectGroup();
    og.setZoneType(null);
    assertThat(f.matches(og)).isFalse();
  }

  @Test
  void filterByOwner_match() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("team-infra");
    f.setOwner(sf);
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByOwner_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("team-security");
    f.setOwner(sf);
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByOwner_nullOwnerOnObjectGroup_noMatch() {
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("team-infra");
    f.setOwner(sf);
    ObjectGroup og = buildObjectGroup();
    og.setOwner(null);
    assertThat(f.matches(og)).isFalse();
  }

  @Test
  void filterByEndpointType_match() {
    EndpointFilter ef = new EndpointFilter();
    ef.setType("CIDR");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setEndpoints(List.of(ef));
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByEndpointType_noMatch() {
    EndpointFilter ef = new EndpointFilter();
    ef.setType("FQDN");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setEndpoints(List.of(ef));
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByEndpointType_nullEndpoints_noMatch() {
    EndpointFilter ef = new EndpointFilter();
    ef.setType("CIDR");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setEndpoints(List.of(ef));
    ObjectGroup og = buildObjectGroup();
    og.setEndpoints(null);
    assertThat(f.matches(og)).isFalse();
  }

  @Test
  void filterByEndpointValue_match() {
    EndpointFilter ef = new EndpointFilter();
    ef.setValue("10.0");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setEndpoints(List.of(ef));
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByEndpointValue_noMatch() {
    EndpointFilter ef = new EndpointFilter();
    ef.setValue("192.168");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setEndpoints(List.of(ef));
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByEndpointValue_nullEndpoints_noMatch() {
    EndpointFilter ef = new EndpointFilter();
    ef.setValue("10.0");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setEndpoints(List.of(ef));
    ObjectGroup og = buildObjectGroup();
    og.setEndpoints(null);
    assertThat(f.matches(og)).isFalse();
  }

  @Test
  void filterByEndpointValue_nullEndpointValue_noMatch() {
    Endpoint ep = new Endpoint();
    ep.setType("CIDR");
    ep.setValue(null);

    ObjectGroup og = buildObjectGroup();
    og.setEndpoints(List.of(ep));

    EndpointFilter ef = new EndpointFilter();
    ef.setValue("10.0");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setEndpoints(List.of(ef));
    assertThat(f.matches(og)).isFalse();
  }

  @Test
  void filterByLabelKey_match() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("prod");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByLabelKey_noMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("region");
    lf.setValue("eu");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByLabelKey_nullLabels_noMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("prod");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setLabels(List.of(lf));
    ObjectGroup og = buildObjectGroup();
    og.setLabels(null);
    assertThat(f.matches(og)).isFalse();
  }

  @Test
  void filterByLabelValue_noMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("staging");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void filterByMultipleLabels_allMatch() {
    LabelFilter lf1 = new LabelFilter();
    lf1.setKey("env");
    lf1.setValue("prod");
    LabelFilter lf2 = new LabelFilter();
    lf2.setKey("tier");
    lf2.setValue("web");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setLabels(List.of(lf1, lf2));
    assertThat(f.matches(buildObjectGroup())).isTrue();
  }

  @Test
  void filterByMultipleLabels_oneNoMatch() {
    LabelFilter lf1 = new LabelFilter();
    lf1.setKey("env");
    lf1.setValue("prod");
    LabelFilter lf2 = new LabelFilter();
    lf2.setKey("tier");
    lf2.setValue("db");
    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    f.setLabels(List.of(lf1, lf2));
    assertThat(f.matches(buildObjectGroup())).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("prod");

    EndpointFilter ef = new EndpointFilter();
    ef.setType("et");
    ef.setValue("ev");

    FirewallObjectGroupFilter f = new FirewallObjectGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter uidFilter = new StringFilter();
    uidFilter.setLike("u");
    StringFilter typeFilter = new StringFilter();
    typeFilter.setLike("t");
    StringFilter zoneTypeFilter = new StringFilter();
    zoneTypeFilter.setLike("z");
    StringFilter ownerFilter = new StringFilter();
    ownerFilter.setLike("o");
    f.setName(stringFilter);
    f.setUid(uidFilter);
    f.setType(typeFilter);
    f.setZoneType(zoneTypeFilter);
    f.setOwner(ownerFilter);
    f.setEndpoints(List.of(ef));
    f.setLabels(List.of(lf));

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getUid()).isEqualTo(uidFilter);
    assertThat(f.getType()).isEqualTo(typeFilter);
    assertThat(f.getZoneType()).isEqualTo(zoneTypeFilter);
    assertThat(f.getOwner()).isEqualTo(ownerFilter);
    assertThat(f.getEndpoints()).containsExactly(ef);
    assertThat(f.getLabels()).containsExactly(lf);
  }
}

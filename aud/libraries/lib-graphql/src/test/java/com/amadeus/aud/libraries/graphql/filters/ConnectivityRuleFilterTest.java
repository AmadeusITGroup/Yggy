package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.ConnectivityRule;
import com.amadeus.aud.libraries.yaml.models.Endpoint;
import com.amadeus.aud.libraries.yaml.models.FirewallGroup;
import com.amadeus.aud.libraries.yaml.models.ObjectGroup;
import com.amadeus.aud.libraries.yaml.models.ProtocolEnum;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConnectivityRuleFilterTest {

  private static ObjectGroup buildObjectGroup(String name) {
    Endpoint ep = new Endpoint();
    ep.setType("CIDR");
    ep.setValue("10.0.0.0/8");

    ObjectGroup og = new ObjectGroup();
    og.setName(name);
    og.setUid("uid-" + name);
    og.setType(ObjectGroup.TypeEnum.ADDRESS_GROUP);
    og.setZoneType("INTERNAL");
    og.setEndpoints(List.of(ep));
    og.setOwner("owner-" + name);
    og.setLabels(Map.of("env", "prod"));
    return og;
  }

  private static ConnectivityRule buildRule() {
    FirewallGroup fg = new FirewallGroup();
    fg.setName("fw-1");
    fg.setVendor("palo-alto");

    ConnectivityRule rule = new ConnectivityRule();
    rule.setName("allow-http-to-web");
    rule.setUid("uid-r1");
    rule.setSrcObjectGroup(buildObjectGroup("src-group"));
    rule.setDstObjectGroup(buildObjectGroup("dst-group"));
    rule.setPorts(List.of("80", "443"));
    rule.setProtocol(ProtocolEnum.TCP);
    rule.setOwner("team-infra");
    rule.setScope("internal");
    rule.setInTransitFirewallGroups(List.of(fg));
    rule.setLabels(Map.of("env", "prod", "tier", "web"));
    return rule;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ConnectivityRuleFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ConnectivityRuleFilter().matches(buildRule())).isTrue();
  }

  @Test
  void filterByName_match() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("http");
    f.setName(stringFilter);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByName_caseInsensitive() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("HTTP");
    f.setName(stringFilter);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("deny-ssh");
    f.setName(stringFilter);
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByProtocol_match() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("Tcp");
    f.setProtocol(sf);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByProtocol_caseInsensitive() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("TCP");
    f.setProtocol(sf);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByProtocol_noMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setEq("Udp");
    f.setProtocol(sf);
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByOwner_match() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("team-infra");
    f.setOwner(sf);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByOwner_noMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("team-security");
    f.setOwner(sf);
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByScope_match() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("internal");
    f.setScope(sf);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByScope_noMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("external");
    f.setScope(sf);
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByScope_nullScopeOnRule_noMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("internal");
    f.setScope(sf);
    ConnectivityRule rule = buildRule();
    rule.setScope(null);
    assertThat(f.matches(rule)).isFalse();
  }

  @Test
  void filterBySrcObjectGroup_match() {
    FirewallObjectGroupFilter of = new FirewallObjectGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("src-group");
    of.setName(stringFilter);
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setSrcObjectGroup(of);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterBySrcObjectGroup_noMatch() {
    FirewallObjectGroupFilter of = new FirewallObjectGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("non-existent");
    of.setName(stringFilter);
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setSrcObjectGroup(of);
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByDstObjectGroup_match() {
    FirewallObjectGroupFilter of = new FirewallObjectGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dst-group");
    of.setName(stringFilter);
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setDstObjectGroup(of);
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByDstObjectGroup_noMatch() {
    FirewallObjectGroupFilter of = new FirewallObjectGroupFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("other-group");
    of.setName(stringFilter);
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setDstObjectGroup(of);
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByPort_match() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setPorts(List.of("443"));
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByMultiplePorts_allMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setPorts(List.of("80", "443"));
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByPort_partialMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setPorts(List.of("80", "8080"));
    assertThat(f.matches(buildRule())).isFalse(); // 8080 is not in rule's ports
  }

  @Test
  void filterByPort_noMatch() {
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setPorts(List.of("8080"));
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByLabelKey_match() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("prod");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByLabelKey_noMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("region");
    lf.setValue("eu");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByLabelKey_nullLabels_noMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("prod");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf));
    ConnectivityRule rule = buildRule();
    rule.setLabels(null);
    assertThat(f.matches(rule)).isFalse();
  }

  @Test
  void filterByLabelValue_noMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("staging");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByLabelKeyAndValue_bothMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("prod");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByLabelKeyAndValue_keyMatchValueNoMatch() {
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("staging");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf));
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void filterByMultipleLabels_allMatch() {
    LabelFilter lf1 = new LabelFilter();
    lf1.setKey("env");
    lf1.setValue("prod");
    LabelFilter lf2 = new LabelFilter();
    lf2.setKey("tier");
    lf2.setValue("web");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf1, lf2));
    assertThat(f.matches(buildRule())).isTrue();
  }

  @Test
  void filterByMultipleLabels_oneNoMatch() {
    LabelFilter lf1 = new LabelFilter();
    lf1.setKey("env");
    lf1.setValue("prod");
    LabelFilter lf2 = new LabelFilter();
    lf2.setKey("tier");
    lf2.setValue("db");
    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setLabels(List.of(lf1, lf2));
    assertThat(f.matches(buildRule())).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    FirewallObjectGroupFilter src = new FirewallObjectGroupFilter();
    FirewallObjectGroupFilter dst = new FirewallObjectGroupFilter();
    LabelFilter lf = new LabelFilter();
    lf.setKey("env");
    lf.setValue("prod");

    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter protocolFilter = new StringFilter();
    protocolFilter.setLike("p");
    StringFilter ownerFilter = new StringFilter();
    ownerFilter.setLike("o");
    StringFilter scopeFilter = new StringFilter();
    scopeFilter.setLike("s");

    ConnectivityRuleFilter f = new ConnectivityRuleFilter();
    f.setName(stringFilter);
    f.setProtocol(protocolFilter);
    f.setOwner(ownerFilter);
    f.setScope(scopeFilter);
    f.setSrcObjectGroup(src);
    f.setDstObjectGroup(dst);
    f.setPorts(List.of("80", "443"));
    f.setLabels(List.of(lf));

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getProtocol()).isEqualTo(protocolFilter);
    assertThat(f.getOwner()).isEqualTo(ownerFilter);
    assertThat(f.getScope()).isEqualTo(scopeFilter);
    assertThat(f.getSrcObjectGroup()).isSameAs(src);
    assertThat(f.getDstObjectGroup()).isSameAs(dst);
    assertThat(f.getPorts()).containsExactly("80", "443");
    assertThat(f.getLabels()).containsExactly(lf);
  }
}

package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.Scope;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlacklistFilterTest {

  private Blacklist blacklist;
  private Scope scope;
  private BlacklistFilter filter;

  private Blacklist createMockBlacklist(
      String id, String name, String sourceComponent, String destinationHostFqdn) {
    Blacklist b = mock(Blacklist.class);
    when(b.id()).thenReturn(id);
    when(b.getName()).thenReturn(name);
    when(b.getScope()).thenReturn(scope);
    when(b.getSourceComponent()).thenReturn(sourceComponent);
    when(b.getDestinationHostFqdns()).thenReturn(List.of(destinationHostFqdn));
    when(b.getDestinationHostCidrs()).thenReturn(null);
    when(b.getSourceZones()).thenReturn(List.of("CSZ", "TNZ"));
    when(b.getSourceMacrophases()).thenReturn(List.of("TST", "PRD"));
    when(b.getSourceTargets()).thenReturn(List.of("prd-we"));
    when(b.getDestinationApplicationProtocols()).thenReturn(List.of("http", "https"));
    when(b.getDestinationTransportProtocols()).thenReturn(List.of("Tcp"));
    when(b.getDestinationPortRanges()).thenReturn(List.of("443", "8080"));
    return b;
  }

  @BeforeEach
  void setUp() {
    scope = mock(Scope.class);
    when(scope.getName()).thenReturn("SecurityOffice");

    blacklist = createMockBlacklist("test-id-123", "test-blacklist", "my-app", "*.google.com");
    filter = new BlacklistFilter();
  }

  @Test
  void noFiltersMatchesAllBlacklists() {
    // When no filters are set, everything should match
    assertThat(filter.matches(blacklist)).isTrue();
  }

  @Test
  void matchesById() {
    StringFilter idFilter = new StringFilter();
    idFilter.setEq("test-id-123");
    filter.setId(idFilter);

    assertThat(filter.matches(blacklist)).isTrue();

    idFilter.setEq("different-id");
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesByName() {
    StringFilter nameFilter = new StringFilter();
    nameFilter.setEq("test-blacklist");
    filter.setName(nameFilter);

    assertThat(filter.matches(blacklist)).isTrue();

    nameFilter.setEq("different-name");
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesBySourceComponent() {
    StringFilter componentFilter = new StringFilter();
    componentFilter.setEq("my-app");
    filter.setSourceComponent(componentFilter);

    assertThat(filter.matches(blacklist)).isTrue();

    componentFilter.setEq("different-app");
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesByDestinationHostFqdns() {
    filter.setDestinationHostFqdns(List.of("*.google.com"));

    assertThat(filter.matches(blacklist)).isTrue();

    filter.setDestinationHostFqdns(List.of("*.example.com"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesByDestinationHostCidrs() {
    Blacklist cidrBlacklist = mock(Blacklist.class);
    when(cidrBlacklist.id()).thenReturn("cidr-id");
    when(cidrBlacklist.getName()).thenReturn("cidr-test");
    when(cidrBlacklist.getScope()).thenReturn(scope);
    when(cidrBlacklist.getSourceComponent()).thenReturn("app");
    when(cidrBlacklist.getDestinationHostCidrs()).thenReturn(List.of("10.0.0.0/8"));
    when(cidrBlacklist.getDestinationHostFqdns()).thenReturn(null);
    when(cidrBlacklist.getSourceZones()).thenReturn(List.of("*"));
    when(cidrBlacklist.getSourceMacrophases()).thenReturn(List.of("*"));
    when(cidrBlacklist.getSourceTargets()).thenReturn(List.of("*"));
    when(cidrBlacklist.getDestinationTransportProtocols()).thenReturn(List.of("*"));
    when(cidrBlacklist.getDestinationPortRanges()).thenReturn(List.of("*"));

    filter.setDestinationHostCidrs(List.of("10.0.0.0/8"));

    assertThat(filter.matches(cidrBlacklist)).isTrue();

    filter.setDestinationHostCidrs(List.of("192.168.0.0/16"));
    assertThat(filter.matches(cidrBlacklist)).isFalse();
  }

  @Test
  void matchesBySourceZones() {
    filter.setSourceZones(List.of("CSZ"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setSourceZones(List.of("TNZ"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setSourceZones(List.of("CDE"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesBySourceZonesMultiple() {
    // Filter with multiple zones - should match if any zone in filter matches any zone in blacklist
    filter.setSourceZones(List.of("CSZ", "CDE"));
    assertThat(filter.matches(blacklist)).isTrue(); // CSZ exists in blacklist

    filter.setSourceZones(List.of("CDE", "PRE"));
    assertThat(filter.matches(blacklist)).isFalse(); // Neither exists in blacklist
  }

  @Test
  void matchesBySourceMacrophases() {
    filter.setSourceMacrophases(List.of("TST"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setSourceMacrophases(List.of("PRD"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setSourceMacrophases(List.of("DEV"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesBySourceTargets() {
    filter.setSourceTargets(List.of("prd-we"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setSourceTargets(List.of("dev-we"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesByDestinationApplicationProtocols() {
    filter.setDestinationApplicationProtocols(List.of("http"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setDestinationApplicationProtocols(List.of("https"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setDestinationApplicationProtocols(List.of("ftp"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesByDestinationTransportProtocols() {
    filter.setDestinationTransportProtocols(List.of("Tcp"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setDestinationTransportProtocols(List.of("Udp"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void matchesByDestinationPortRanges() {
    filter.setDestinationPortRanges(List.of("443"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setDestinationPortRanges(List.of("8080"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setDestinationPortRanges(List.of("9000"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void emptyFilterListMatchesAll() {
    filter.setSourceZones(Collections.emptyList());
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setSourceMacrophases(Collections.emptyList());
    assertThat(filter.matches(blacklist)).isTrue();
  }

  @Test
  void nullBlacklistDoesNotMatch() {
    assertThat(filter.matches(null)).isFalse();
  }

  @Test
  void combinedFiltersAllMustMatch() {
    StringFilter componentFilter = new StringFilter();
    componentFilter.setEq("my-app");
    filter.setSourceComponent(componentFilter);

    filter.setSourceZones(List.of("CSZ"));

    assertThat(filter.matches(blacklist)).isTrue();

    // Change one filter - should not match
    filter.setSourceZones(List.of("CDE"));
    assertThat(filter.matches(blacklist)).isFalse();
  }

  @Test
  void caseInsensitiveListMatching() {
    // Zones should be case-insensitive
    filter.setSourceZones(List.of("csz"));
    assertThat(filter.matches(blacklist)).isTrue();

    filter.setSourceZones(List.of("CSZ"));
    assertThat(filter.matches(blacklist)).isTrue();
  }
}

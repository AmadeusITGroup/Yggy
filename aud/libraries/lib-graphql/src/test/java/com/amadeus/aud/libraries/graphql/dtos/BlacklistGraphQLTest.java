package com.amadeus.aud.libraries.graphql.dtos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.models.core.db.Blacklist;
import com.amadeus.aud.libraries.models.core.db.Scope;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlacklistGraphQLTest {

  private Blacklist blacklist;
  private Scope scope;

  @BeforeEach
  void setUp() {
    scope = mock(Scope.class);
    when(scope.getName()).thenReturn("SecurityOffice");

    blacklist = mock(Blacklist.class);
    when(blacklist.id()).thenReturn("test-id-123");
    when(blacklist.getName()).thenReturn("test-blacklist");
    when(blacklist.getScope()).thenReturn(scope);
    when(blacklist.getSourceComponent()).thenReturn("my-app");
    when(blacklist.getDestinationHostFqdns()).thenReturn(List.of("*.google.com"));
    when(blacklist.getDestinationHostCidrs()).thenReturn(null);
    when(blacklist.getSourceZones()).thenReturn(List.of("CSZ", "TNZ"));
    when(blacklist.getSourceMacrophases()).thenReturn(List.of("TST", "PRD"));
    when(blacklist.getSourceTargets()).thenReturn(List.of("prd-we"));
    when(blacklist.getDestinationApplicationProtocols()).thenReturn(List.of("http", "https"));
    when(blacklist.getDestinationTransportProtocols()).thenReturn(List.of("Tcp"));
    when(blacklist.getDestinationPortRanges()).thenReturn(List.of("443", "8080"));
  }

  @Test
  void blacklistGraphQLFromBlacklist() {
    BlacklistGraphQL dto = BlacklistGraphQL.from(blacklist);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo("test-id-123");
    assertThat(dto.type()).isEqualTo(ObjectTypeListNamesEnum.BLACKLISTS.getUri());
    assertThat(dto.name()).isEqualTo("test-blacklist");
    assertThat(dto.scope()).isNotNull();
    assertThat(dto.sourceComponent()).isEqualTo("my-app");
    assertThat(dto.destinationHostFqdns()).contains("*.google.com");
    assertThat(dto.destinationHostCidrs()).isEmpty();
    assertThat(dto.sourceZones()).containsAll(List.of("CSZ", "TNZ"));
    assertThat(dto.sourceMacrophases()).containsAll(List.of("TST", "PRD"));
    assertThat(dto.sourceTargets()).contains("prd-we");
    assertThat(dto.destinationApplicationProtocols()).containsAll(List.of("http", "https"));
    assertThat(dto.destinationTransportProtocols()).contains("Tcp");
    assertThat(dto.destinationPortRanges()).containsAll(List.of("443", "8080"));
  }

  @Test
  void blacklistGraphQLFromNullBlacklist() {
    BlacklistGraphQL dto = BlacklistGraphQL.from(null);
    assertThat(dto).isNull();
  }

  @Test
  void blacklistGraphQLWithCIDR() {
    Blacklist cidrBlacklist = mock(Blacklist.class);
    when(cidrBlacklist.id()).thenReturn("cidr-id-456");
    when(cidrBlacklist.getName()).thenReturn("cidr-blacklist");
    when(cidrBlacklist.getScope()).thenReturn(scope);
    when(cidrBlacklist.getSourceComponent()).thenReturn("infra-team");
    when(cidrBlacklist.getDestinationHostFqdns()).thenReturn(null);
    when(cidrBlacklist.getDestinationHostCidrs()).thenReturn(List.of("10.0.0.0/8"));
    when(cidrBlacklist.getSourceZones()).thenReturn(List.of("*"));
    when(cidrBlacklist.getSourceMacrophases()).thenReturn(List.of("*"));
    when(cidrBlacklist.getSourceTargets()).thenReturn(List.of("*"));
    when(cidrBlacklist.getDestinationApplicationProtocols()).thenReturn(null);
    when(cidrBlacklist.getDestinationTransportProtocols()).thenReturn(List.of("*"));
    when(cidrBlacklist.getDestinationPortRanges()).thenReturn(List.of("*"));

    BlacklistGraphQL dto = BlacklistGraphQL.from(cidrBlacklist);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo("cidr-id-456");
    assertThat(dto.sourceComponent()).isEqualTo("infra-team");
    assertThat(dto.destinationHostCidrs()).contains("10.0.0.0/8");
    assertThat(dto.destinationHostFqdns()).isEmpty();
    assertThat(dto.sourceZones()).contains("*");
    assertThat(dto.destinationApplicationProtocols()).isEmpty();
  }

  @Test
  void blacklistGraphQLWithNullCollections() {
    Blacklist minimalBlacklist = mock(Blacklist.class);
    when(minimalBlacklist.id()).thenReturn("minimal-id");
    when(minimalBlacklist.getName()).thenReturn("minimal");
    when(minimalBlacklist.getScope()).thenReturn(scope);
    when(minimalBlacklist.getSourceComponent()).thenReturn("app");
    when(minimalBlacklist.getDestinationHostFqdns()).thenReturn(List.of("example.com"));
    when(minimalBlacklist.getDestinationHostCidrs()).thenReturn(null);
    when(minimalBlacklist.getSourceZones()).thenReturn(null);
    when(minimalBlacklist.getSourceMacrophases()).thenReturn(null);
    when(minimalBlacklist.getSourceTargets()).thenReturn(null);
    when(minimalBlacklist.getDestinationApplicationProtocols()).thenReturn(null);
    when(minimalBlacklist.getDestinationTransportProtocols()).thenReturn(null);
    when(minimalBlacklist.getDestinationPortRanges()).thenReturn(null);

    BlacklistGraphQL dto = BlacklistGraphQL.from(minimalBlacklist);

    assertThat(dto).isNotNull();
    assertThat(dto.destinationHostFqdns()).contains("example.com");
    assertThat(dto.destinationHostCidrs()).isEmpty();
    assertThat(dto.sourceZones()).isEmpty();
    assertThat(dto.sourceMacrophases()).isEmpty();
    assertThat(dto.sourceTargets()).isEmpty();
    assertThat(dto.destinationApplicationProtocols()).isEmpty();
    assertThat(dto.destinationTransportProtocols()).isEmpty();
    assertThat(dto.destinationPortRanges()).isEmpty();
  }
}

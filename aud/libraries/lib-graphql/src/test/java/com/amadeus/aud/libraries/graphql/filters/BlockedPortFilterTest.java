package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlockedPort;
import com.amadeus.aud.libraries.yaml.models.V1BlockedPortSpec;
import org.junit.jupiter.api.Test;

class BlockedPortFilterTest {

  private BlockedPort bp(String name, int port, V1BlockedPortSpec.ProtocolEnum protocol) {
    BlockedPort bp = mock(BlockedPort.class);
    when(bp.id()).thenReturn("bp-" + name.hashCode());
    when(bp.getName()).thenReturn(name);
    when(bp.getPort()).thenReturn(port);
    when(bp.getProtocol()).thenReturn(protocol);
    return bp;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new BlockedPortFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(
            new BlockedPortFilter().matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP)))
        .isTrue();
  }

  @Test
  void filterByName_match() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ssh");
    f.setName(stringFilter);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("http");
    f.setName(stringFilter);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isFalse();
  }

  @Test
  void filterByPort_match() {
    BlockedPortFilter f = new BlockedPortFilter();
    f.setPort(22);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isTrue();
  }

  @Test
  void filterByPort_noMatch() {
    BlockedPortFilter f = new BlockedPortFilter();
    f.setPort(80);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isFalse();
  }

  @Test
  void filterByProtocol_match() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("TCP");
    f.setProtocol(sf);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isTrue();
  }

  @Test
  void filterByProtocol_noMatch() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("UDP");
    f.setProtocol(sf);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isFalse();
  }

  @Test
  void filterByProtocol_nullProtocolOnEntity_noMatch() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter sf = new StringFilter();
    sf.setLike("TCP");
    f.setProtocol(sf);
    BlockedPort bp = mock(BlockedPort.class);
    when(bp.getName()).thenReturn("deny-ssh");
    when(bp.getPort()).thenReturn(22);
    when(bp.getProtocol()).thenReturn(null);
    assertThat(f.matches(bp)).isFalse();
  }

  @Test
  void getters_returnSetValues() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    StringFilter protocolFilter = new StringFilter();
    protocolFilter.setLike("TCP");
    f.setName(stringFilter);
    f.setPort(443);
    f.setProtocol(protocolFilter);
    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getPort()).isEqualTo(443);
    assertThat(f.getProtocol()).isEqualTo(protocolFilter);
  }

  @Test
  void filterById_match() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("bp-");
    f.setId(idFilter);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    BlockedPortFilter f = new BlockedPortFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(bp("deny-ssh", 22, V1BlockedPortSpec.ProtocolEnum.TCP))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    BlockedPortFilter f = new BlockedPortFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}

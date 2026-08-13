package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlackListedCIDR;
import org.junit.jupiter.api.Test;

class BlackListedCIDRFilterTest {

  private BlackListedCIDR createMockBlackListedCIDR(String id, String name, String cidr) {
    BlackListedCIDR mockCidr = mock(BlackListedCIDR.class);
    when(mockCidr.id()).thenReturn(id);
    when(mockCidr.getName()).thenReturn(name);
    when(mockCidr.getCidr()).thenReturn(cidr);
    return mockCidr;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new BlackListedCIDRFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    BlackListedCIDR cidr = createMockBlackListedCIDR("id1", "name1", "192.168.1.0/24");
    assertThat(new BlackListedCIDRFilter().matches(cidr)).isTrue();
  }

  @Test
  void filterByName_match() {
    BlackListedCIDRFilter filter = new BlackListedCIDRFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setEq("name1");
    filter.setName(stringFilter);

    BlackListedCIDR cidr = createMockBlackListedCIDR("id1", "name1", "192.168.1.0/24");
    assertThat(filter.matches(cidr)).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    BlackListedCIDRFilter filter = new BlackListedCIDRFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setEq("name2");
    filter.setName(stringFilter);

    BlackListedCIDR cidr = createMockBlackListedCIDR("id1", "name1", "192.168.1.0/24");
    assertThat(filter.matches(cidr)).isFalse();
  }

  @Test
  void filterByCidr_match() {
    BlackListedCIDRFilter filter = new BlackListedCIDRFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("192.168");
    filter.setCidr(stringFilter);

    BlackListedCIDR cidr = createMockBlackListedCIDR("id1", "name1", "192.168.1.0/24");
    assertThat(filter.matches(cidr)).isTrue();
  }

  @Test
  void filterByCidr_noMatch() {
    BlackListedCIDRFilter filter = new BlackListedCIDRFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("10.0.0");
    filter.setCidr(stringFilter);

    BlackListedCIDR cidr = createMockBlackListedCIDR("id1", "name1", "192.168.1.0/24");
    assertThat(filter.matches(cidr)).isFalse();
  }
}

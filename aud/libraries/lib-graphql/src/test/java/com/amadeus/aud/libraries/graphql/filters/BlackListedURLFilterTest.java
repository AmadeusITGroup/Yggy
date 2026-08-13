package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.BlackListedURL;
import org.junit.jupiter.api.Test;

class BlackListedURLFilterTest {

  private BlackListedURL createMockBlackListedURL(String id, String name, String fqdn) {
    BlackListedURL mockUrl = mock(BlackListedURL.class);
    when(mockUrl.id()).thenReturn(id);
    when(mockUrl.getName()).thenReturn(name);
    when(mockUrl.getFqdn()).thenReturn(fqdn);
    return mockUrl;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new BlackListedURLFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    BlackListedURL url = createMockBlackListedURL("id1", "name1", "example.com");
    assertThat(new BlackListedURLFilter().matches(url)).isTrue();
  }

  @Test
  void filterByName_match() {
    BlackListedURLFilter filter = new BlackListedURLFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setEq("name1");
    filter.setName(stringFilter);

    BlackListedURL url = createMockBlackListedURL("id1", "name1", "example.com");
    assertThat(filter.matches(url)).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    BlackListedURLFilter filter = new BlackListedURLFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setEq("name2");
    filter.setName(stringFilter);

    BlackListedURL url = createMockBlackListedURL("id1", "name1", "example.com");
    assertThat(filter.matches(url)).isFalse();
  }

  @Test
  void filterByFqdn_match() {
    BlackListedURLFilter filter = new BlackListedURLFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("example");
    filter.setFqdn(stringFilter);

    BlackListedURL url = createMockBlackListedURL("id1", "name1", "example.com");
    assertThat(filter.matches(url)).isTrue();
  }

  @Test
  void filterByFqdn_noMatch() {
    BlackListedURLFilter filter = new BlackListedURLFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("another.org");
    filter.setFqdn(stringFilter);

    BlackListedURL url = createMockBlackListedURL("id1", "name1", "example.com");
    assertThat(filter.matches(url)).isFalse();
  }
}

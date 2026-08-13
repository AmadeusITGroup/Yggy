package com.amadeus.aud.libraries.graphql.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringFilterTest {
  @Test
  void emtpyFilterMatches() {
    StringFilter filter = new StringFilter();

    assertThat(filter.matches("hello")).isTrue();
  }

  @Test
  void exactFilterMatches() {
    StringFilter filter = new StringFilter();
    filter.setEq("hello");

    assertThat(filter.matches("hello")).isTrue();
    assertThat(filter.matches("hi")).isFalse();
  }

  @Test
  void likeFilterMatches() {
    StringFilter filter = new StringFilter();
    filter.setLike("hello");

    assertThat(filter.matches("hello")).isTrue();
    assertThat(filter.matches("HElloo")).isTrue();
    assertThat(filter.matches("Oh, HELLO")).isTrue();
    assertThat(filter.matches("Oh, hello there")).isTrue();
    assertThat(filter.matches("Oh, hi there")).isFalse();
  }
}

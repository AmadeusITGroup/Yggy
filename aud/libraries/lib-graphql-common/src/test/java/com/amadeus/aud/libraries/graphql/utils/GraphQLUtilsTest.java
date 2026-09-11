package com.amadeus.aud.libraries.graphql.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class GraphQLUtilsTest {

  @Test
  void createsMappedPageMetadata() {
    PageGraphQL<String> page = GraphQLUtils.toPage(List.of(1, 2, 3), 2, 2, Object::toString);

    assertThat(page.items()).containsExactly("3");
    assertThat(page.pagination()).isEqualTo(new PaginationGraphQL(3, 2, 2));
  }
}

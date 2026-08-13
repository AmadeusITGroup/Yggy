package com.amadeus.aud.libraries.graphql.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class GraphQLMetadataUtilsTest {

  // ---------------------------------------------------------------------------
  // toPage
  // ---------------------------------------------------------------------------

  @Nested
  class ToPage {

    private final List<String> items = List.of("a", "b", "c", "d", "e");

    @Test
    void returnsFirstPage() {
      assertThat(GraphQLMetadataUtils.toPage(items, 1, 2, f -> f).items())
          .containsExactly("a", "b");
    }

    @Test
    void returnsSecondPage() {
      assertThat(GraphQLMetadataUtils.toPage(items, 2, 2, f -> f).items())
          .containsExactly("c", "d");
    }

    @Test
    void returnsPartialLastPage() {
      assertThat(GraphQLMetadataUtils.toPage(items, 3, 2, f -> f).items()).containsExactly("e");
    }

    @Test
    void returnsEmptyItemsWhenPageBeyondEnd() {
      assertThat(GraphQLMetadataUtils.toPage(items, 10, 2, f -> f).items()).isEmpty();
    }

    @Test
    void returnsNoItemsWhenPageSizeIsZero() {
      assertThat(GraphQLMetadataUtils.toPage(items, 1, 0, f -> f).items()).isEmpty();
    }

    @Test
    void returnsNoItemsWhenPageSizeIsNegative() {
      assertThat(GraphQLMetadataUtils.toPage(items, 1, -1, f -> f).items()).isEmpty();
    }

    @Test
    void returnsEmptyItemsWhenSourceListIsEmpty() {
      assertThat(GraphQLMetadataUtils.toPage(List.of(), 1, 10, f -> f).items()).isEmpty();
    }

    @Test
    void negativePageClampsToZeroReturnsFullPage() {
      assertThat(GraphQLMetadataUtils.toPage(items, -1, 5, f -> f).items())
          .containsExactlyElementsOf(items);
    }

    @Test
    void totalCountReflectsFullListSizeNotPageSlice() {
      PageGraphQL<String> page = GraphQLMetadataUtils.toPage(List.of("a", "b", "c"), 1, 2, f -> f);
      assertThat(page.pagination().count()).isEqualTo(3);
    }

    @Test
    void pageNumberPreservedInPagination() {
      PageGraphQL<String> page = GraphQLMetadataUtils.toPage(List.of("a", "b", "c"), 2, 10, f -> f);
      assertThat(page.pagination().pageNum()).isEqualTo(2);
    }

    @Test
    void pageSizePreservedInPagination() {
      PageGraphQL<String> page = GraphQLMetadataUtils.toPage(List.of("a", "b", "c"), 1, 10, f -> f);
      assertThat(page.pagination().pageSize()).isEqualTo(10);
    }
  }

  // ---------------------------------------------------------------------------
  // paginate
  // ---------------------------------------------------------------------------

  @Nested
  class Paginate {

    private final List<String> items = List.of("a", "b", "c", "d", "e");

    @Test
    void returnsFirstPage() {
      assertThat(GraphQLMetadataUtils.paginate(items, 1, 2, f -> f)).containsExactly("a", "b");
    }

    @Test
    void returnsSecondPage() {
      assertThat(GraphQLMetadataUtils.paginate(items, 2, 2, f -> f)).containsExactly("c", "d");
    }

    @Test
    void returnsPartialLastPage() {
      assertThat(GraphQLMetadataUtils.paginate(items, 3, 2, f -> f)).containsExactly("e");
    }

    @Test
    void returnsEmptyWhenPageBeyondEnd() {
      assertThat(GraphQLMetadataUtils.paginate(items, 10, 2, f -> f)).isEmpty();
    }

    @Test
    void returnsNoItemsWhenPageSizeIsZero() {
      assertThat(GraphQLMetadataUtils.paginate(items, 1, 0, f -> f)).isEmpty();
    }

    @Test
    void returnsNoItemsWhenPageSizeIsNegative() {
      assertThat(GraphQLMetadataUtils.paginate(items, 1, -1, f -> f)).isEmpty();
    }

    @Test
    void returnsEmptyWhenSourceListIsEmpty() {
      assertThat(GraphQLMetadataUtils.paginate(List.of(), 1, 10, f -> f)).isEmpty();
    }

    @Test
    void negativePageClampsToZeroReturnsFullPage() {
      assertThat(GraphQLMetadataUtils.paginate(items, -1, 5, f -> f))
          .containsExactlyElementsOf(items);
    }
  }
}

package com.amadeus.aud.libraries.graphql.dtos;

import org.eclipse.microprofile.graphql.Ignore;
import org.eclipse.microprofile.graphql.Type;

@Type("SearchResultDTO")
public record SearchResultGraphQL(
    String type, String name, String id, String scope, @Ignore int relevance)
    implements Comparable<SearchResultGraphQL> {

  @Override
  public int compareTo(SearchResultGraphQL other) {
    return Integer.compare(this.relevance, other.relevance);
  }
}

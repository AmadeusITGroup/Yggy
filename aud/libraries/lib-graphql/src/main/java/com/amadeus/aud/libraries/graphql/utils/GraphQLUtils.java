package com.amadeus.aud.libraries.graphql.utils;

import java.util.List;

public final class GraphQLUtils {

  public static final String PARAM_VERSION = "version";
  public static final String PARAM_PAGE_NUMBER = "pageNumber";
  public static final String PARAM_PAGE_SIZE = "pageSize";
  public static final String DEFAULT_VERSION = "latest";
  public static final String DEFAULT_PAGE_NUMBER = "1";
  public static final String DEFAULT_PAGE_SIZE = "100";

  private GraphQLUtils() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static <Datatype, GraphQLType> PageGraphQL<GraphQLType> toPage(
      List<Datatype> allItems,
      int pageNumber,
      int pageSize,
      java.util.function.Function<Datatype, GraphQLType> mapper) {
    return GraphQLMetadataUtils.toPage(allItems, pageNumber, pageSize, mapper);
  }
}

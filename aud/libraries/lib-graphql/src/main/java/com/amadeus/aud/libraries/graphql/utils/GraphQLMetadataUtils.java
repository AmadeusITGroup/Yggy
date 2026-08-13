package com.amadeus.aud.libraries.graphql.utils;

import java.util.List;

public final class GraphQLMetadataUtils {

  private GraphQLMetadataUtils() {}

  public static <DataType, GraphQLType> PageGraphQL<GraphQLType> toPage(
      List<DataType> allItems,
      int pageNumber,
      int pageSize,
      java.util.function.Function<DataType, GraphQLType> mapper) {
    return new PageGraphQL<>(
        paginate(allItems, pageNumber, pageSize, mapper),
        new PaginationGraphQL(allItems.size(), pageNumber, pageSize));
  }

  public static <DataType, GraphQLType> List<GraphQLType> paginate(
      List<DataType> allItems,
      int pageNumber,
      int pageSize,
      java.util.function.Function<DataType, GraphQLType> mapper) {
    if (pageSize <= 0) {
      return List.of();
    }
    int from = Math.max(0, (pageNumber - 1) * pageSize);
    if (from >= allItems.size()) {
      return List.of();
    }
    List<DataType> subList = allItems.subList(from, Math.min(from + pageSize, allItems.size()));
    List<GraphQLType> result = new java.util.ArrayList<>(subList.size());
    for (DataType item : subList) {
      result.add(mapper.apply(item));
    }
    return result;
  }
}

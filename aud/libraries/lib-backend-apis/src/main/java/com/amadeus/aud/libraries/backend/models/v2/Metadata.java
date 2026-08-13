package com.amadeus.aud.libraries.backend.models.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Metadata(
    @Nullable List<String> initialColumns,
    @Nullable Pagination pagination,
    @Nullable List<Link> links) {
  public Metadata() {
    this(null, null, null);
  }

  public Metadata withInitialColumns(List<String> initialColumns) {
    return new Metadata(initialColumns, this.pagination, this.links);
  }

  public Metadata withPagination(Pagination pagination) {
    return new Metadata(this.initialColumns, pagination, this.links);
  }

  public Metadata withLinks(List<Link> links) {
    return new Metadata(this.initialColumns, this.pagination, links);
  }
}

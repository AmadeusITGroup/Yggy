package com.amadeus.aud.libraries.graphql.utils;

import org.eclipse.microprofile.graphql.Type;

@Type("PaginationDTO")
public record PaginationGraphQL(int count, int pageNum, int pageSize) {}

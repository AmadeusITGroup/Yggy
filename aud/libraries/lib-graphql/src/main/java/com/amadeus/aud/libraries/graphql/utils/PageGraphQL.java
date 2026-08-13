package com.amadeus.aud.libraries.graphql.utils;

import java.util.List;
import org.eclipse.microprofile.graphql.Type;

@Type("PageDTO")
public record PageGraphQL<T>(List<T> items, PaginationGraphQL pagination) {}

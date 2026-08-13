package com.amadeus.aud.libraries.graphql.utils;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class FilterUtils {

  private FilterUtils() {}

  public static boolean containsIgnoreCase(@Nullable String filter, @Nullable String value) {
    return filter == null || (value != null && value.toLowerCase().contains(filter.toLowerCase()));
  }

  public static boolean equalsIgnoreCase(@Nullable String filter, @Nullable String value) {
    return filter == null || filter.equalsIgnoreCase(value);
  }

  public static boolean matchesBoolean(@Nullable Boolean filter, boolean value) {
    return filter == null || filter == value;
  }

  public static <T> boolean stringFilterMatches(
      @Nullable StringFilter filter, @Nullable T object, Function<T, String> nameExtractor) {
    if (filter == null) {
      return true;
    }
    if (object == null) {
      return false;
    }
    String name = nameExtractor.apply(object);
    return filter.matches(name);
  }

  public static <T> boolean anyStringFilterMatches(
      @Nullable StringFilter filter,
      @Nullable Collection<T> items,
      Function<T, String> valueExtractor) {
    if (filter == null) {
      return true;
    }
    if (items == null) {
      return false;
    }
    for (T item : items) {
      String value = valueExtractor.apply(item);
      if (filter.matches(value)) {
        return true;
      }
    }
    return false;
  }

  public static <F, T> boolean allFilterMatch(
      @Nullable Collection<F> filters,
      @Nullable Collection<T> items,
      BiPredicate<F, T> matchesFunction) {
    if (filters == null || filters.isEmpty()) {
      return true;
    }
    if (items == null) {
      return false;
    }
    for (F filter : filters) {
      boolean matched = false;
      for (T item : items) {
        if (matchesFunction.test(filter, item)) {
          matched = true;
          break;
        }
      }
      if (!matched) {
        return false;
      }
    }
    return true;
  }

  public static boolean allLabelsMatch(
      @Nullable List<LabelFilter> labelFilters, @Nullable Map<String, String> labels) {
    if (labelFilters == null || labelFilters.isEmpty()) {
      return true;
    }
    for (LabelFilter lf : labelFilters) {
      String filterKey = lf.getKey();
      String filterValue = lf.getValue();
      if (filterKey == null || filterValue == null) {
        continue;
      }
      if (labels == null) {
        return false;
      }
      String actual = labels.get(filterKey);
      if (!filterValue.equalsIgnoreCase(actual)) {
        return false;
      }
    }
    return true;
  }
}

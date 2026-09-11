package com.amadeus.aud.libraries.graphql.utils;

import jakarta.annotation.Nullable;
import org.eclipse.microprofile.graphql.Input;

/**
 * Filter for string matching with support for partial (like) and exact (eq) matches. Both can be
 * specified for combined filtering (AND logic).
 */
@Input("StringFilter")
public class StringFilter {
  private String like;
  private String eq;

  public void setLike(String like) {
    this.like = like;
  }

  public void setEq(String eq) {
    this.eq = eq;
  }

  public String getLike() {
    return like;
  }

  public String getEq() {
    return eq;
  }

  public boolean matches(@Nullable String value) {
    boolean likeMatch = like == null || FilterUtils.containsIgnoreCase(like, value);
    boolean eqMatch = eq == null || (value != null && value.equals(eq));
    return likeMatch && eqMatch;
  }
}

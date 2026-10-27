package com.amadeus.aud.libraries.checker.models;

import java.time.LocalDate;

public record Transitional(LocalDate limitDate) {

  /**
   * @param date in the format 2007-12-03 (ISO_LOCAL_DATE)
   */
  public static Transitional of(CharSequence date) {
    return new Transitional(LocalDate.parse(date));
  }
}

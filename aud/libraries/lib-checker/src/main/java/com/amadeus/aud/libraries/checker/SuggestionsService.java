package com.amadeus.aud.libraries.checker;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.similarity.LevenshteinDistance;

public final class SuggestionsService {
  private static final int LEVENSHTEIN_DISTANCE_THRESHOLD = 5;
  private static final int MAX_SUGGESTIONS = 4;
  private final LevenshteinDistance distance;

  public SuggestionsService() {
    this.distance = new LevenshteinDistance(LEVENSHTEIN_DISTANCE_THRESHOLD);
  }

  /**
   * Find typo suggestions for an invalid reference.
   *
   * <p>This returns the first N matching suggestions, not the best N, as a performance
   * optimization. Since this only runs on errors, early termination avoids computing Levenshtein
   * distance for all values.
   *
   * <p>Beware: this can be slow, but it's only paid on errors
   */
  public List<String> suggestions(String text, Iterable<String> knownValues) {
    List<String> typoSuggestions = new ArrayList<>();
    for (String value : knownValues) {
      int score = this.distance.apply(text, value);
      if (score > 0) {
        typoSuggestions.add(value);
      }
      if (typoSuggestions.size() >= MAX_SUGGESTIONS) {
        break;
      }
    }
    return typoSuggestions;
  }
}

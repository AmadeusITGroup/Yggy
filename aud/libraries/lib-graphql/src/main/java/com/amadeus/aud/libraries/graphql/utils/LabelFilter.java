package com.amadeus.aud.libraries.graphql.utils;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.graphql.Input;

@Input("LabelFilter")
public class LabelFilter {

  private String key;
  private String value;

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public static boolean matchesAllLabels(
      @Nullable Map<String, String> tags, @Nullable List<LabelFilter> labelFilters) {
    if (labelFilters == null || labelFilters.isEmpty()) {
      return true;
    }
    if (tags == null || tags.isEmpty()) {
      return false;
    }

    for (LabelFilter labelFilter : labelFilters) {
      String key = labelFilter.getKey();
      String value = labelFilter.getValue();

      if (key == null || key.isBlank()) {
        continue;
      }

      String tagValue = tags.get(key);
      if (tagValue == null) {
        return false;
      }

      if (value != null && !value.isBlank() && !tagValue.contains(value)) {
        return false;
      }
    }

    return true;
  }
}

package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.graphql.Input;

@Input("NetworkSetFilter")
public class NetworkSetFilter {
  private @Nullable StringFilter name;
  private List<LabelFilter> labels = Collections.emptyList();

  public boolean matches(com.amadeus.aud.libraries.yaml.models.NetworkSet networkSet) {
    if (name != null && !name.matches(networkSet.getName())) {
      return false;
    }
    if (!labels.isEmpty()) {
      return networkSet.getLabels() != null && matchesLabels(networkSet.getLabels());
    }
    return true;
  }

  private boolean matchesLabels(Map<String, String> networkSetLabels) {
    for (LabelFilter labelFilter : labels) {
      String key = labelFilter.getKey();
      String value = labelFilter.getValue();

      if (key != null && !networkSetLabels.containsKey(key)) {
        return false;
      }
      if (key != null && value != null && !value.equals(networkSetLabels.get(key))) {
        return false;
      }
    }
    return true;
  }

  public @Nullable StringFilter getName() {
    return name;
  }

  public void setName(@Nullable StringFilter name) {
    this.name = name;
  }

  public List<LabelFilter> getLabels() {
    return labels;
  }

  public void setLabels(List<LabelFilter> labels) {
    this.labels = labels != null ? labels : Collections.emptyList();
  }
}

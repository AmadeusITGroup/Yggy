package com.amadeus.aud.libraries.graphql.filters;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import jakarta.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.graphql.Input;

@Input(PAASComponentFilter.NAME)
public class PAASComponentFilter {
  public static final String NAME = "PAASComponentFilter";
  public static final String PROVIDER = "kubernetesPaasComponents";
  private @Nullable StringFilter name;
  private @Nullable StringFilter namespace;
  private List<LabelFilter> labels = Collections.emptyList();
  private List<LabelFilter> identifier = Collections.emptyList();

  public boolean matches(com.amadeus.aud.libraries.yaml.models.PAASComponent component) {
    if (name != null && !name.matches(component.getName())) {
      return false;
    }
    if (namespace != null
        && (component.getNamespace() == null || !namespace.matches(component.getNamespace()))) {
      return false;
    }
    if (!labels.isEmpty()
        && (component.getLabels() == null || !matchesLabels(component.getLabels(), labels))) {
      return false;
    }
    if (!identifier.isEmpty() && component.getIdentifier() != null) {
      try {
        @SuppressWarnings("unchecked")
        Map<String, String> identifierMap = (Map<String, String>) component.getIdentifier();
        if (!matchesLabels(identifierMap, identifier)) {
          return false;
        }
      } catch (ClassCastException e) {
        return false;
      }
    }
    return true;
  }

  private boolean matchesLabels(Map<String, String> componentMap, List<LabelFilter> filterList) {
    for (LabelFilter labelFilter : filterList) {
      String key = labelFilter.getKey();
      String value = labelFilter.getValue();

      if (key != null && !componentMap.containsKey(key)) {
        return false;
      }
      if (key != null && value != null && !value.equals(componentMap.get(key))) {
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

  public @Nullable StringFilter getNamespace() {
    return namespace;
  }

  public void setNamespace(@Nullable StringFilter namespace) {
    this.namespace = namespace;
  }

  public List<LabelFilter> getLabels() {
    return labels;
  }

  public void setLabels(List<LabelFilter> labels) {
    this.labels = labels != null ? labels : Collections.emptyList();
  }

  public List<LabelFilter> getIdentifier() {
    return identifier;
  }

  public void setIdentifier(List<LabelFilter> identifier) {
    this.identifier = identifier != null ? identifier : Collections.emptyList();
  }
}

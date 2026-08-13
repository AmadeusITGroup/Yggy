package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.PAASComponent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PAASComponentFilterTest {

  @Test
  void matchesReturnsTrueWhenAllFiltersAreNull() {
    PAASComponentFilter filter = new PAASComponentFilter();
    PAASComponent component = createComponent("test-component", "default");

    assertThat(filter.matches(component)).isTrue();
  }

  @Test
  void matchesReturnsTrueWhenNameMatches() {
    PAASComponentFilter filter = new PAASComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("test");
    filter.setName(stringFilter);

    PAASComponent component = createComponent("test-component", "default");

    assertThat(filter.matches(component)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenNameDoesNotMatch() {
    PAASComponentFilter filter = new PAASComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("other");
    filter.setName(stringFilter);

    PAASComponent component = createComponent("test-component", "default");

    assertThat(filter.matches(component)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenNamespaceMatches() {
    PAASComponentFilter filter = new PAASComponentFilter();
    StringFilter namespaceFilter = new StringFilter();
    namespaceFilter.setLike("default");
    filter.setNamespace(namespaceFilter);

    PAASComponent component = createComponent("test-component", "default");

    assertThat(filter.matches(component)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenNamespaceDoesNotMatch() {
    PAASComponentFilter filter = new PAASComponentFilter();
    StringFilter namespaceFilter = new StringFilter();
    namespaceFilter.setLike("kube-system");
    filter.setNamespace(namespaceFilter);

    PAASComponent component = createComponent("test-component", "default");

    assertThat(filter.matches(component)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenLabelsMatch() {
    PAASComponentFilter filter = new PAASComponentFilter();
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("app");
    labelFilter.setValue("backend");
    filter.setLabels(List.of(labelFilter));

    PAASComponent component = createComponent("test-component", "default");
    component.setLabels(Map.of("app", "backend", "version", "1.0"));

    assertThat(filter.matches(component)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenLabelKeyDoesNotExist() {
    PAASComponentFilter filter = new PAASComponentFilter();
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("missing");
    filter.setLabels(List.of(labelFilter));

    PAASComponent component = createComponent("test-component", "default");
    component.setLabels(Map.of("app", "backend"));

    assertThat(filter.matches(component)).isFalse();
  }

  @Test
  void matchesReturnsFalseWhenLabelValueDoesNotMatch() {
    PAASComponentFilter filter = new PAASComponentFilter();
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("app");
    labelFilter.setValue("frontend");
    filter.setLabels(List.of(labelFilter));

    PAASComponent component = createComponent("test-component", "default");
    component.setLabels(Map.of("app", "backend"));

    assertThat(filter.matches(component)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenMultipleFiltersMatch() {
    PAASComponentFilter filter = new PAASComponentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("test");
    filter.setName(stringFilter);
    StringFilter namespaceFilter = new StringFilter();
    namespaceFilter.setLike("default");
    filter.setNamespace(namespaceFilter);
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("app");
    labelFilter.setValue("backend");
    filter.setLabels(List.of(labelFilter));

    PAASComponent component = createComponent("test-component", "default");
    component.setLabels(Map.of("app", "backend"));

    assertThat(filter.matches(component)).isTrue();
  }

  @Test
  void matchesReturnsTrueWhenIdentifierMatches() {
    PAASComponentFilter filter = new PAASComponentFilter();
    LabelFilter identifierFilter = new LabelFilter();
    identifierFilter.setKey("app.kubernetes.io/part-of");
    identifierFilter.setValue("JFrog_Artifactory");
    filter.setIdentifier(List.of(identifierFilter));

    PAASComponent component = createComponent("test-component", "default");
    component.setIdentifier(Map.of("app.kubernetes.io/part-of", "JFrog_Artifactory"));

    assertThat(filter.matches(component)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenIdentifierKeyDoesNotExist() {
    PAASComponentFilter filter = new PAASComponentFilter();
    LabelFilter identifierFilter = new LabelFilter();
    identifierFilter.setKey("missing-key");
    filter.setIdentifier(List.of(identifierFilter));

    PAASComponent component = createComponent("test-component", "default");
    component.setIdentifier(Map.of("app.kubernetes.io/part-of", "JFrog_Artifactory"));

    assertThat(filter.matches(component)).isFalse();
  }

  @Test
  void matchesReturnsFalseWhenIdentifierValueDoesNotMatch() {
    PAASComponentFilter filter = new PAASComponentFilter();
    LabelFilter identifierFilter = new LabelFilter();
    identifierFilter.setKey("app.kubernetes.io/part-of");
    identifierFilter.setValue("different-value");
    filter.setIdentifier(List.of(identifierFilter));

    PAASComponent component = createComponent("test-component", "default");
    component.setIdentifier(Map.of("app.kubernetes.io/part-of", "JFrog_Artifactory"));

    assertThat(filter.matches(component)).isFalse();
  }

  private PAASComponent createComponent(String name, String namespace) {
    PAASComponent component = new PAASComponent();
    component.setName(name);
    component.setNamespace(namespace);
    component.setIdentifier(Map.of("id", "123"));
    return component;
  }
}

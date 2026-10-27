package com.amadeus.aud.libraries.checker.models;

import static com.amadeus.aud.libraries.models.parsing.YamlService.LOCATION_LABEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageTest {
  @Test
  void errorWithDetailsWithoutLocation() {
    String code = "code";
    String message = "message";
    V1Metadata metadata = new V1Metadata();
    metadata.setName("name");
    metadata.setScope("scope");
    V1Base element = new V1Base();
    element.metadata(metadata);
    element.setKind("kind");

    Message output = Message.errorWithDetails(code, message, element);

    assertThat(output.code()).isEqualTo(code);
    assertThat(output.message()).isEqualTo(message);
    assertThat(output.level()).isEqualTo(Level.ERROR);
    assertThat(output.transitional()).isNull();
    assertThat(output.details()).isEqualTo(new Details("scope", "kind", "name"));
    assertThat(output.links()).isEmpty();
    assertThatNoException().isThrownBy(() -> output.links().add(URI.create("https://example.com")));
  }

  @Test
  void errorWithDetailsWithLocation() {
    String code = "code";
    String message = "message";
    V1Metadata metadata = new V1Metadata();
    metadata.setName("name");
    metadata.setScope("scope");
    metadata.putLabelsItem(LOCATION_LABEL, "/path/to/resource.yaml");
    V1Base element = new V1Base();
    element.metadata(metadata);
    element.setKind("kind");

    Message output = Message.errorWithDetails(code, message, element);

    assertThat(output.code()).isEqualTo(code);
    assertThat(output.message()).isEqualTo(message);
    assertThat(output.level()).isEqualTo(Level.ERROR);
    assertThat(output.transitional()).isNull();
    assertThat(output.details()).isEqualTo(new Details("scope", "kind", "name"));
    assertThat(output.links()).containsExactly(URI.create("file:///path/to/resource.yaml"));
  }

  @Test
  void warningWithDetailsWithoutLocation() {
    String code = "code";
    String message = "message";
    V1Metadata metadata = new V1Metadata();
    metadata.setName("name");
    metadata.setScope("scope");
    V1Base element = new V1Base();
    element.metadata(metadata);
    element.setKind("kind");

    Message output = Message.warningWithDetails(code, message, element);

    assertThat(output.code()).isEqualTo(code);
    assertThat(output.message()).isEqualTo(message);
    assertThat(output.level()).isEqualTo(Level.WARNING);
    assertThat(output.transitional()).isNull();
    assertThat(output.details()).isEqualTo(new Details("scope", "kind", "name"));
    assertThat(output.links()).isEmpty();
    assertThatNoException().isThrownBy(() -> output.links().add(URI.create("https://example.com")));
  }

  @Test
  void warningWithDetailsWithLocation() {
    String code = "code";
    String message = "message";
    V1Metadata metadata = new V1Metadata();
    metadata.setName("name");
    metadata.setScope("scope");
    metadata.putLabelsItem(LOCATION_LABEL, "/path/to/resource.yaml");
    V1Base element = new V1Base();
    element.metadata(metadata);
    element.setKind("kind");

    Message output = Message.warningWithDetails(code, message, element);

    assertThat(output.code()).isEqualTo(code);
    assertThat(output.message()).isEqualTo(message);
    assertThat(output.level()).isEqualTo(Level.WARNING);
    assertThat(output.transitional()).isNull();
    assertThat(output.details()).isEqualTo(new Details("scope", "kind", "name"));
    assertThat(output.links()).containsExactly(URI.create("file:///path/to/resource.yaml"));
  }

  @Test
  void canonicalConstructorEnsuresLinksAreMutable() {
    String code = "code";
    String message = "message";
    V1Metadata metadata = new V1Metadata();
    metadata.setName("name");
    metadata.setScope("scope");
    metadata.putLabelsItem(LOCATION_LABEL, "/path/to/resource.yaml");
    V1Base element = new V1Base();
    element.metadata(metadata);
    element.setKind("kind");

    Message output = new Message(code, Level.ERROR, message, null, null, List.of());

    assertThat(output).isNotNull();
    assertThat(output.links()).isEmpty();
    assertThatNoException().isThrownBy(() -> output.links().add(URI.create("https://example.com")));
  }

  @Test
  void infoWithDetailsWithoutLocation() {
    String code = "code";
    String message = "message";
    V1Metadata metadata = new V1Metadata();
    metadata.setName("name");
    metadata.setScope("scope");
    V1Base element = new V1Base();
    element.metadata(metadata);
    element.setKind("kind");

    Message output = Message.infoWithDetails(code, message, element);

    assertThat(output.code()).isEqualTo(code);
    assertThat(output.message()).isEqualTo(message);
    assertThat(output.level()).isEqualTo(Level.INFO);
    assertThat(output.transitional()).isNull();
    assertThat(output.details()).isEqualTo(new Details("scope", "kind", "name"));
    assertThat(output.links()).isEmpty();
    assertThatNoException().isThrownBy(() -> output.links().add(URI.create("https://example.com")));
  }

  @Test
  void infoWithDetailsWithLocation() {
    String code = "code";
    String message = "message";
    V1Metadata metadata = new V1Metadata();
    metadata.setName("name");
    metadata.setScope("scope");
    metadata.putLabelsItem(LOCATION_LABEL, "/path/to/resource.yaml");
    V1Base element = new V1Base();
    element.metadata(metadata);
    element.setKind("kind");

    Message output = Message.infoWithDetails(code, message, element);

    assertThat(output.code()).isEqualTo(code);
    assertThat(output.message()).isEqualTo(message);
    assertThat(output.level()).isEqualTo(Level.INFO);
    assertThat(output.transitional()).isNull();
    assertThat(output.details()).isEqualTo(new Details("scope", "kind", "name"));
    assertThat(output.links()).containsExactly(URI.create("file:///path/to/resource.yaml"));
  }
}

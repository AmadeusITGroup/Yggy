package com.amadeus.aud.libraries.json.schema.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.json.schema.ports.SchemaPort;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import com.amadeus.aud.libraries.yaml.models.V1OutboundSNATSpec;
import com.amadeus.aud.libraries.yaml.models.V1Scope;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class V1JsonSchemaGeneratorTest {
  private final SchemaPort sut =
      new V1JsonSchemaGenerator(new ObjectMapper().findAndRegisterModules());

  @Test
  void generatesRootSchemaForEveryMappedKind() {
    JsonNode schema = sut.get();
    ArrayNode branches = (ArrayNode) schema.path("oneOf");
    Map<String, Class<? extends V1Base>> mappings = new DefaultKindMapper().mappings();

    assertThat(schema.path("$schema").asText())
        .isEqualTo("https://json-schema.org/draft/2020-12/schema");
    assertThat(schema.has("components")).isFalse();
    assertThat(branches.size()).isEqualTo(mappings.size());

    for (Map.Entry<String, Class<? extends V1Base>> mapping : mappings.entrySet()) {
      JsonNode branch = findBranch(branches, mapping.getKey());
      assertThat(branch).isNotNull();
      assertThat(branch.path("type").asText()).isEqualTo("object");
      assertThat(branch.path("additionalProperties").asBoolean()).isFalse();
      assertThat(branch.path("properties").path("kind").path("enum"))
          .hasToString("[\"" + mapping.getKey() + "\"]");
      assertThat(branch.path("properties").properties().stream().map(Map.Entry::getKey))
          .allMatch(Set.of("apiVersion", "api_version", "kind", "metadata", "spec")::contains);
      assertThat(branch.path("properties").path("api_version").path("deprecated").asBoolean())
          .isTrue();
      assertThat(schema.path("$defs").has(mapping.getValue().getSimpleName())).isTrue();
    }
  }

  @Test
  void usesModelDescriptionsRequiredPropertiesAndNestedReferences() {
    JsonNode schema = sut.get();
    JsonNode component = schema.path("$defs").path(V1Component.class.getSimpleName());
    JsonNode componentSpec = schema.path("$defs").path("V1ComponentSpec");
    JsonNode definitions = schema.path("$defs");

    assertThat(component.path("description").asText()).startsWith("Definition of a component.");
    assertThat(component.path("required").toString()).contains("kind", "metadata", "spec");
    assertThat(component.path("allOf").toString()).contains("apiVersion", "api_version");
    assertThat(component.path("properties").path("apiVersion").path("pattern").asText())
        .isEqualTo("^v1$");
    assertThat(component.path("properties").path("api_version").path("pattern").asText())
        .isEqualTo("^v1$");
    assertThat(component.path("properties").path("spec").path("$ref").asText())
        .isEqualTo("#/$defs/V1ComponentSpec");
    assertThat(componentSpec.path("properties").path("isPublic").path("description").asText())
        .contains(
            "This attribute controls the permission to instantiate your component.",
            "By default (false value), nobody can instantiate your component outside your scope.");
    assertThat(schema.path("$defs").has("ProtocolEnum")).isTrue();
    assertThat(
            definitions
                .path("V1ServiceSpec")
                .path("properties")
                .path("isPci")
                .path("deprecated")
                .isMissingNode())
        .isTrue();
    assertThat(
            definitions
                .path("V1ServiceSpec")
                .path("properties")
                .path("is_pci")
                .path("deprecated")
                .asBoolean())
        .isTrue();
    assertThat(definitions.path("AccessControlDeny").has("anyOf")).isTrue();
    assertThat(definitions.has("Class")).isFalse();
    assertThat(definitions.has("Method")).isFalse();
    assertThat(
            definitions
                .path(V1Scope.class.getSimpleName())
                .path("properties")
                .path("spec")
                .path("$ref")
                .asText())
        .isEqualTo("#/$defs/V1ScopeSpec");
    JsonNode destinationCidrs =
        definitions
            .path(V1OutboundSNATSpec.class.getSimpleName())
            .path("properties")
            .path("destinationCidrs");
    assertThat(destinationCidrs.path("type").asText()).isEqualTo("array");
    assertThat(destinationCidrs.has("anyOf")).isFalse();
    assertThat(
            definitions
                .path(V1OutboundSNATSpec.class.getSimpleName())
                .path("properties")
                .path("destination_cidrs")
                .path("deprecated")
                .asBoolean())
        .isTrue();
  }

  @Test
  void disallowsAdditionalPropertiesInSpecDefinitions() {
    JsonNode schema = sut.get();
    JsonNode definitions = schema.path("$defs");

    definitions.properties().stream()
        .filter(entry -> entry.getKey().endsWith("Spec"))
        .forEach(
            entry ->
                assertThat(entry.getValue().path("additionalProperties").asBoolean())
                    .as(entry.getKey())
                    .isFalse());
  }

  @Test
  void requiresEveryRequiredPropertyWhenAliasesArePresent() {
    JsonNode connectionSpec = sut.get().path("$defs").path(V1ConnectionSpec.class.getSimpleName());
    ArrayNode aliasGroups = (ArrayNode) connectionSpec.path("allOf");

    assertThat(aliasGroups).hasSize(2);
    assertThat(aliasGroups.get(0).path("anyOf").toString())
        .contains("sourceZoneType", "source_zone_type");
    assertThat(aliasGroups.get(1).path("anyOf").toString())
        .contains("destinationZoneType", "destination_zone_type");
  }

  @Test
  void emitsEnumValuesAndStableOutput() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    JsonNode schema = sut.get();
    JsonNode enumSchema = schema.path("$defs").path("DeprecatedStatus");

    assertThat(enumSchema.path("type").asText()).isEqualTo("string");
    assertThat(enumSchema.path("enum")).hasToString("[\"None\",\"Warning\",\"Error\"]");

    String first = objectMapper.writeValueAsString(schema);
    String second = objectMapper.writeValueAsString(sut.get());
    assertThat(second).isEqualTo(first);
  }

  @Test
  void emitsOnlyResolvableReferences() {
    JsonNode schema = sut.get();

    assertReferencesResolve(schema, schema);
  }

  private static @Nullable JsonNode findBranch(ArrayNode branches, String kind) {
    for (JsonNode branch : branches) {
      if (kind.equals(branch.at("/properties/kind/enum/0").asText())) {
        return branch;
      }
    }
    return null;
  }

  private static void assertReferencesResolve(JsonNode node, JsonNode root) {
    if (node.isObject()) {
      JsonNode reference = node.get("$ref");
      if (reference != null) {
        assertThat(reference.asText()).startsWith("#/$defs/");
        assertThat(root.at(reference.asText().substring(1)).isMissingNode()).isFalse();
      }
      node.elements().forEachRemaining(child -> assertReferencesResolve(child, root));
    } else if (node.isArray()) {
      node.elements().forEachRemaining(child -> assertReferencesResolve(child, root));
    }
  }
}

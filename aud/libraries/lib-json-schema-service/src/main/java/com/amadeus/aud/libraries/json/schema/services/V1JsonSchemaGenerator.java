package com.amadeus.aud.libraries.json.schema.services;

import com.amadeus.aud.libraries.json.schema.ports.SchemaPort;
import com.amadeus.aud.libraries.yaml.models.AbstractOpenApiSchema;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.amadeus.aud.libraries.yaml.models.utils.JacksonPropertyUtils;
import com.amadeus.aud.libraries.yaml.models.utils.KindMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Builds a standalone JSON Schema for the V1 YAML resources.
 *
 * <p>The generated schema is intentionally based on Jackson's serialized view of the model classes,
 * while using the model annotations for descriptions and required properties.
 */
@ApplicationScoped
public final class V1JsonSchemaGenerator implements SchemaPort {
  private static final String JSON_SCHEMA_DRAFT = "https://json-schema.org/draft/2020-12/schema";
  private static final String MODEL_TITLE = "AUD V1 YAML Model";
  private static final String DEFINITIONS_KEY = "$defs";

  private final ObjectMapper objectMapper;
  private final KindMapper kindMapper;
  private final ObjectNode schema;

  public V1JsonSchemaGenerator(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.kindMapper = new DefaultKindMapper();
    this.schema = buildSchema();
  }

  /**
   * Returns a defensive copy of the generated schema.
   *
   * @return standalone JSON Schema document
   */
  @Override
  public JsonNode get() {
    return schema;
  }

  private ObjectNode buildSchema() {
    SchemaContext context = new SchemaContext(objectMapper);
    ObjectNode root = objectMapper.createObjectNode();
    root.put("$schema", JSON_SCHEMA_DRAFT);
    root.put("title", MODEL_TITLE);
    root.put("type", "object");
    ArrayNode oneOf = root.putArray("oneOf");

    List<Map.Entry<String, Class<? extends V1Base>>> mappings =
        new ArrayList<>(kindMapper.mappings().entrySet());
    mappings.sort(Map.Entry.comparingByKey());
    for (Map.Entry<String, Class<? extends V1Base>> mapping : mappings) {
      context.addDefinition(mapping.getValue());
      ObjectNode branch = context.definition(mapping.getValue());
      ObjectNode kindSchema = (ObjectNode) branch.get("properties").get("kind");
      kindSchema.putArray("enum").add(mapping.getKey());
      branch.put("additionalProperties", false);
      oneOf.add(branch);
    }

    root.set(DEFINITIONS_KEY, context.definitions());
    return root;
  }

  private static final class SchemaContext {
    private final ObjectMapper objectMapper;
    private final JacksonPropertyUtils propertyUtils;
    private final Map<Class<?>, ObjectNode> definitions = new HashMap<>();
    private final Set<Class<?>> classesBeingBuilt = new HashSet<>();

    private SchemaContext(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      this.propertyUtils = new JacksonPropertyUtils(objectMapper);
    }

    private String reference(Class<?> type) {
      return "#/" + DEFINITIONS_KEY + "/" + schemaName(type);
    }

    private void addDefinition(Class<?> type) {
      if (definitions.containsKey(type) || classesBeingBuilt.contains(type)) {
        return;
      }

      ObjectNode definition = objectMapper.createObjectNode();
      definitions.put(type, definition);
      classesBeingBuilt.add(type);
      try {
        populateDefinition(type, definition);
      } finally {
        classesBeingBuilt.remove(type);
      }
    }

    private ObjectNode definitions() {
      ObjectNode result = objectMapper.createObjectNode();
      List<Map.Entry<Class<?>, ObjectNode>> toSort = new ArrayList<>(definitions.entrySet());
      toSort.sort(Comparator.comparing(entry -> schemaName(entry.getKey())));
      for (Map.Entry<Class<?>, ObjectNode> entry : toSort) {
        result.set(schemaName(entry.getKey()), entry.getValue());
      }
      return result;
    }

    private ObjectNode definition(Class<?> type) {
      return definitions.get(type).deepCopy();
    }

    private void populateDefinition(Class<?> type, ObjectNode definition) {
      Schema classSchema = type.getAnnotation(Schema.class);
      addDescription(definition, classSchema == null ? null : classSchema.description());

      if (type.isEnum()) {
        populateEnum(type, definition);
        return;
      }
      if (AbstractOpenApiSchema.class.isAssignableFrom(type)) {
        populateComposedDefinition(type, definition);
        return;
      }

      definition.put("type", "object");
      if (type.getSimpleName().endsWith("Spec")) {
        definition.put("additionalProperties", false);
      }
      ObjectNode properties = definition.putObject("properties");
      ArrayNode required = objectMapper.createArrayNode();
      ArrayNode requiredAliasGroups = null;
      JavaType javaType = objectMapper.constructType(type);
      for (BeanPropertyDefinition property :
          objectMapper.getSerializationConfig().introspect(javaType).findProperties()) {
        AnnotatedMember member = property.getPrimaryMember();
        if (member == null) {
          continue;
        }
        if (!member.hasAnnotation(JsonProperty.class)) {
          continue;
        }

        String propertyName = property.getFullName().getSimpleName();
        Set<String> acceptedPropertyNames = propertyUtils.getAcceptedPropertyNames(propertyName);
        String canonicalPropertyName = acceptedPropertyNames.iterator().next();
        ObjectNode propertySchema = schemaForType(member.getType());
        Schema propertyAnnotation = member.getAnnotation(Schema.class);
        applyBeanValidationConstraints(propertySchema, member);
        addDescription(
            propertySchema, propertyAnnotation == null ? null : propertyAnnotation.description());
        properties.set(canonicalPropertyName, propertySchema);
        for (String acceptedPropertyName : acceptedPropertyNames) {
          if (canonicalPropertyName.equals(acceptedPropertyName)
              || properties.has(acceptedPropertyName)) {
            continue;
          }
          ObjectNode aliasSchema = propertySchema.deepCopy();
          aliasSchema.put("deprecated", true);
          properties.set(acceptedPropertyName, aliasSchema);
        }

        if (isRequired(propertyAnnotation, member)) {
          if (acceptedPropertyNames.size() == 1) {
            required.add(canonicalPropertyName);
          } else {
            if (requiredAliasGroups == null) {
              requiredAliasGroups = definition.putArray("allOf");
            }
            ArrayNode requiredAlternatives = requiredAliasGroups.addObject().putArray("anyOf");
            for (String acceptedPropertyName : acceptedPropertyNames) {
              requiredAlternatives.addObject().putArray("required").add(acceptedPropertyName);
            }
          }
        }
      }
      if (!required.isEmpty()) {
        definition.set("required", required);
      }
    }

    private void populateComposedDefinition(Class<?> type, ObjectNode definition) {
      AbstractOpenApiSchema composedSchema = instantiateComposedSchema(type);
      String compositionKeyword = composedSchema.getSchemaType();
      if (!"oneOf".equals(compositionKeyword) && !"anyOf".equals(compositionKeyword)) {
        throw new IllegalStateException(
            "Unsupported composed schema type '" + compositionKeyword + "' for " + type.getName());
      }

      ArrayNode composedSchemas = definition.putArray(compositionKeyword);
      List<Map.Entry<String, Class<?>>> schemas =
          new ArrayList<>(composedSchema.getSchemas().entrySet());
      schemas.sort(Map.Entry.comparingByKey());
      for (Map.Entry<String, Class<?>> schema : schemas) {
        addDefinition(schema.getValue());
        composedSchemas.addObject().put("$ref", reference(schema.getValue()));
      }
    }

    private static AbstractOpenApiSchema instantiateComposedSchema(Class<?> type) {
      try {
        Constructor<? extends AbstractOpenApiSchema> constructor =
            type.asSubclass(AbstractOpenApiSchema.class).getDeclaredConstructor();
        return constructor.newInstance();
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Cannot instantiate composed schema " + type.getName(), e);
      }
    }

    private ObjectNode schemaForType(JavaType type) {
      if (type == null) {
        return objectSchema();
      }

      Class<?> rawType = type.getRawClass();
      if (rawType == Object.class || JsonNode.class.isAssignableFrom(rawType)) {
        return objectMapper.createObjectNode();
      }
      if (rawType.isEnum()) {
        addDefinition(rawType);
        return referenceSchema(rawType);
      }
      if (rawType.isArray() || Collection.class.isAssignableFrom(rawType)) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "array");
        JavaType contentType = type.getContentType();
        schema.set("items", schemaForType(contentType));
        return schema;
      }
      if (Map.class.isAssignableFrom(rawType)) {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.set("additionalProperties", schemaForType(type.getContentType()));
        return schema;
      }

      ObjectNode scalarSchema = scalarSchema(rawType);
      if (scalarSchema != null) {
        return scalarSchema;
      }

      addDefinition(rawType);
      return referenceSchema(rawType);
    }

    private static void applyBeanValidationConstraints(
        ObjectNode propertySchema, AnnotatedMember member) {
      Pattern pattern = member.getAnnotation(Pattern.class);
      if (pattern != null) {
        propertySchema.put("pattern", anchorBeanValidationPattern(pattern.regexp()));
      }

      Size size = member.getAnnotation(Size.class);
      if (size != null) {
        Class<?> propertyType = member.getType().getRawClass();
        if (CharSequence.class.isAssignableFrom(propertyType)) {
          addSizeBounds(propertySchema, size, "minLength", "maxLength");
        } else if (propertyType.isArray() || Collection.class.isAssignableFrom(propertyType)) {
          addSizeBounds(propertySchema, size, "minItems", "maxItems");
        } else if (Map.class.isAssignableFrom(propertyType)) {
          addSizeBounds(propertySchema, size, "minProperties", "maxProperties");
        }
      }
    }

    private static void addSizeBounds(
        ObjectNode propertySchema, Size size, String minimumKeyword, String maximumKeyword) {
      if (size.min() > 0) {
        propertySchema.put(minimumKeyword, size.min());
      }
      if (size.max() < Integer.MAX_VALUE) {
        propertySchema.put(maximumKeyword, size.max());
      }
    }

    private static String anchorBeanValidationPattern(String regexp) {
      StringBuilder buffer = new StringBuilder();
      if (!regexp.startsWith("^")) {
        buffer.append("^");
      }
      buffer.append(regexp);
      if (!regexp.endsWith("$")) {
        buffer.append("$");
      }
      return buffer.toString();
    }

    private @Nullable ObjectNode scalarSchema(Class<?> type) {
      if (type == String.class || type == Character.class || type == char.class) {
        return objectMapper.createObjectNode().put("type", "string");
      }
      if (type == Boolean.class || type == boolean.class) {
        return objectMapper.createObjectNode().put("type", "boolean");
      }
      if (type == Byte.class
          || type == byte.class
          || type == Short.class
          || type == short.class
          || type == Integer.class
          || type == int.class
          || type == Long.class
          || type == long.class
          || type == BigInteger.class) {
        return objectMapper.createObjectNode().put("type", "integer");
      }
      if (Number.class.isAssignableFrom(type) || type == float.class || type == double.class) {
        return objectMapper.createObjectNode().put("type", "number");
      }
      if (type == URI.class) {
        return objectMapper.createObjectNode().put("type", "string").put("format", "uri");
      }
      if (type == UUID.class) {
        return objectMapper.createObjectNode().put("type", "string").put("format", "uuid");
      }
      if (type == BigDecimal.class
          || type.getName().startsWith("java.time.")
          || type == java.util.Date.class) {
        return objectMapper.createObjectNode().put("type", "string");
      }
      return null;
    }

    private void populateEnum(Class<?> type, ObjectNode definition) {
      definition.put("type", "string");
      ArrayNode values = definition.putArray("enum");
      Object[] enumConstants = type.getEnumConstants();
      if (enumConstants != null) {
        for (Object enumConstant : enumConstants) {
          values.add(objectMapper.valueToTree(enumConstant));
        }
      }
    }

    private ObjectNode referenceSchema(Class<?> type) {
      return objectMapper.createObjectNode().put("$ref", reference(type));
    }

    private ObjectNode objectSchema() {
      return objectMapper.createObjectNode().put("type", "object");
    }

    private static boolean isRequired(Schema schema, AnnotatedMember member) {
      if (schema != null) {
        return schema.required();
      }
      JsonProperty jsonProperty = member.getAnnotation(JsonProperty.class);
      return member.hasAnnotation(NotNull.class)
          || (jsonProperty != null && jsonProperty.required());
    }

    private static void addDescription(ObjectNode schema, String description) {
      if (description != null && !description.isBlank()) {
        schema.put("description", description);
      }
    }

    private static String schemaName(Class<?> type) {
      String binaryName = type.getName();
      int packageSeparator = binaryName.lastIndexOf('.');
      return binaryName.substring(packageSeparator + 1).replace('$', '_');
    }
  }
}

package com.amadeus.aud.libraries.yaml.models.deserializers;

import com.amadeus.aud.libraries.yaml.models.JacksonProperty;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.utils.JacksonPropertyUtils;
import com.amadeus.aud.libraries.yaml.models.utils.KindMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.CaseFormat;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jboss.logging.Logger;

public class KindBasedDeserializer extends JsonDeserializer<Object> {

  private static final Logger log = Logger.getLogger(KindBasedDeserializer.class);

  private final ObjectMapper objectMapper;

  private final boolean enableWarnings;
  private final boolean enableOnTheFlyFixes;

  private final KindMapper kindMapper;
  private final Map<Class<?>, Map<String, JacksonProperty>> fieldMappings;

  public KindBasedDeserializer(
      KindMapper kindMapper,
      ObjectMapper objectMapper,
      boolean enableWarnings,
      boolean enableOnTheFlyFixes) {
    this.kindMapper = kindMapper;
    this.objectMapper = objectMapper;
    this.enableWarnings = enableWarnings;
    this.enableOnTheFlyFixes = enableOnTheFlyFixes;

    JacksonPropertyUtils propertyUtil = new JacksonPropertyUtils(objectMapper);

    this.fieldMappings = new HashMap<>();
    for (Class<?> clazz : kindMapper.mappings().values()) {
      this.fieldMappings.put(clazz, propertyUtil.getFieldMapping(clazz));
    }
  }

  private void logWarnings(String format, Object... params) {
    if (enableWarnings) {
      log.warnf(format, params);
    }
  }

  @Override
  public Object deserialize(final JsonParser jsonParser, final DeserializationContext ctxt)
      throws IOException {
    JsonNode rootNode = objectMapper.readTree(jsonParser);

    JsonNode kindNode = rootNode.get("kind");
    if (kindNode != null) {
      String kind = kindNode.asText();
      if (kind != null && !kind.isEmpty()) {
        Class<? extends V1Base> targetClass = kindMapper.getClass(kind);
        if (targetClass != null) {
          // Get JSON to field mapping
          Map<String, JacksonProperty> fieldMap = fieldMappings.get(targetClass);
          Objects.requireNonNull(fieldMap);
          // Deserialize to target class
          try {
            return objectMapper.treeToValue(convertKeys(rootNode, fieldMap), targetClass);
          } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            String fieldName = e.getPath().getLast().getFieldName();
            if (fieldName != null) {
              logWarnings("Field '%s' does not exist. Delete it.", fieldName);
            }
          }
        } else {
          logWarnings("No class found for kind: %s. Skipping conversion.", kind);
        }
      }
    }

    return new Object();
  }

  private ObjectNode convertKeys(
      final JsonNode rootNode, final Map<String, JacksonProperty> fieldMap) {
    return convertKeys(rootNode, fieldMap, null);
  }

  private ObjectNode convertKeys(
      final JsonNode rootNode,
      final Map<String, JacksonProperty> fieldMap,
      @Nullable final String parentPrefix) {
    ObjectNode convertedNode = objectMapper.createObjectNode();

    for (Map.Entry<String, JsonNode> field : rootNode.properties()) {
      String originalKey = field.getKey();
      JsonNode value = field.getValue();

      if (value.isObject()) {
        String camelCaseKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, originalKey);
        String fullCamelCaseKey = computeKey(parentPrefix, camelCaseKey);
        value = convertKeys(value, fieldMap, fullCamelCaseKey);
      }
      String originalFullKey = computeKey(parentPrefix, originalKey);
      JacksonProperty jacksonProperty = fieldMap.get(originalFullKey);
      String convertedKey = null;
      if (jacksonProperty != null) {
        convertedKey = jacksonProperty.fieldName();

        if (enableOnTheFlyFixes) {
          value = applyOnTheFlyFixes(jacksonProperty, value, originalFullKey);
        }
      }
      if (convertedKey == null || convertedKey.isEmpty()) {
        convertedKey = originalKey;
      }
      convertedNode.set(convertedKey, value);
    }
    return convertedNode;
  }

  private static String computeKey(@Nullable String parentPrefix, String camelCaseKey) {
    String fullCamelCaseKey;
    if (parentPrefix == null || parentPrefix.isEmpty()) {
      fullCamelCaseKey = camelCaseKey;
    } else {
      fullCamelCaseKey = new StringBuffer(parentPrefix).append(".").append(camelCaseKey).toString();
    }
    return fullCamelCaseKey;
  }

  private @Nullable JsonNode applyOnTheFlyFixes(
      final JacksonProperty jacksonProperty, JsonNode value, final String originalFullKey) {
    // Attempt to fix wrong user inputs
    Class<?> expectedType = jacksonProperty.fieldClass();
    if (expectedType == null) {
      return value;
    }

    if (value.isTextual() && Objects.equals(expectedType, Boolean.class)) {
      String textValue = value.textValue();
      if (textValue != null) {
        logWarnings(
            "MismatchedInput: Converting boolean string %s to respective boolean value for key %s.",
            value, originalFullKey);

        String textValueLowerCase = textValue.toLowerCase(Locale.ROOT);
        if (textValueLowerCase.startsWith("t")) {
          value = BooleanNode.valueOf(true);
        } else if (textValueLowerCase.startsWith("f")) {
          value = BooleanNode.valueOf(false);
        } else {
          log.warnf(
              "Cannot convert boolean string %s to boolean value for key %s. Using false as default value.",
              value, originalFullKey);
          value = BooleanNode.valueOf(false);
        }
      }
    } else if (value.isArray() && Objects.equals(expectedType, String.class)) {
      logWarnings("MismatchedInput: Converting %s to String for key %s.", value, originalFullKey);
      value = TextNode.valueOf(value.toString());
    } else if (value.isObject()) {
      if (Objects.equals(expectedType, Map.class)) {
        // Check that the values of the map are in the expected format
        // (jacksonProperty.contentClass())
        // We are doing it iterating over the content of the map and checking the type of the
        // values
        // If the type is not the expected one, we will convert it
        ObjectNode convertedValue = objectMapper.createObjectNode();
        Class<?> contentClass = jacksonProperty.contentClass();
        boolean shouldBeString = Objects.equals(contentClass, String.class);

        for (Map.Entry<String, JsonNode> valueField : value.properties()) {
          JsonNode mapValue = valueField.getValue();
          if (shouldBeString && mapValue.isArray()) {
            logWarnings(
                "MismatchedInput: Converting %s to String for key %s.", mapValue, originalFullKey);
            mapValue = TextNode.valueOf(mapValue.toString());
          }
          convertedValue.set(valueField.getKey(), mapValue);
        }
        value = convertedValue;
      } else if (Objects.equals(expectedType, String.class)) {
        logWarnings("MismatchedInput: Converting %s to String for key %s.", value, originalFullKey);
        value = TextNode.valueOf(value.toString());
      }
    }
    return value;
  }

  @SuppressWarnings("java:S3776")
  @Deprecated
  public Map<String, Object> convertKeys(
      Map<String, Object> rootMap, Map<String, JacksonProperty> fieldMap, String parentPrefix) {
    Map<String, Object> convertedMap = new HashMap<>();

    for (Map.Entry<String, Object> entry : rootMap.entrySet()) {
      String originalKey = entry.getKey();
      Object value = entry.getValue();

      if (value instanceof Map) {
        String camelCaseKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, originalKey);
        String fullCamelCaseKey =
            parentPrefix.isEmpty() ? camelCaseKey : String.join(".", parentPrefix, camelCaseKey);
        @SuppressWarnings("unchecked")
        var valueAsMap = (Map<String, Object>) value;
        value = convertKeys(valueAsMap, fieldMap, fullCamelCaseKey);
      }
      String originalFullKey =
          parentPrefix.isEmpty() ? originalKey : String.join(".", parentPrefix, originalKey);
      JacksonProperty jacksonProperty = fieldMap.get(originalFullKey);
      String convertedKey = null;
      if (jacksonProperty != null) {
        convertedKey = jacksonProperty.fieldName();

        if (enableOnTheFlyFixes) {
          value = applyOnTheFlyFixes(jacksonProperty, value, originalFullKey);
        }
      }
      if (convertedKey == null || convertedKey.isEmpty()) {
        convertedKey = originalKey;
      }
      convertedMap.put(convertedKey, value);
    }
    return convertedMap;
  }

  @SuppressWarnings("java:S3776")
  @Deprecated
  private Object applyOnTheFlyFixes(
      JacksonProperty jacksonProperty, Object value, String originalFullKey) {
    // Attempt to fix wrong user inputs
    Class<?> expectedType = jacksonProperty.fieldClass();
    if (expectedType != null && value != null) {
      if (expectedType.equals(String.class) && !(value instanceof String)) {
        logWarnings("MismatchedInput: Converting %s to String for key %s.", value, originalFullKey);
        return value.toString();
      }

      if (value instanceof String textValue && Objects.equals(expectedType, Boolean.class)) {
        logWarnings(
            "MismatchedInput: Converting boolean string %s to respective boolean value for key% s.",
            value, originalFullKey);

        String textValueLowerCase = textValue.toLowerCase(Locale.ROOT);
        if (textValueLowerCase.startsWith("t")) {
          return true;
        } else if (textValueLowerCase.startsWith("f")) {
          return false;
        } else {
          log.warnf(
              "Cannot convert boolean string %s to boolean value for key %s. Using false as default value.",
              value, originalFullKey);
          return false;
        }
      }

      if (value instanceof Map<?, ?> mapValue) {
        if (Objects.equals(expectedType, Map.class)) {
          // Check that the values of the map are in the expected format
          // (jacksonProperty.contentClass())
          // We are doing it iterating over the content of the map and checking the type of the
          // values
          // If the type is not the expected one, we will convert it
          Class<?> contentClass = jacksonProperty.contentClass();
          Map<Object, Object> convertedMap = new HashMap<>();
          for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
            Object key = entry.getKey();
            Object entryValue = entry.getValue();
            if (entryValue instanceof List && Objects.equals(contentClass, String.class)) {
              logWarnings(
                  "MismatchedInput: Converting %s to String for key %s.%s.",
                  entryValue, originalFullKey, key);
              entryValue = entryValue.toString();
            }
            convertedMap.put(key, entryValue);
          }
          return convertedMap;
        }
      }
    }
    return value;
  }
}

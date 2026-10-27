package com.amadeus.aud.libraries.checks.file;

import com.amadeus.aud.libraries.checker.models.Details;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.NodeInfo;
import com.amadeus.aud.libraries.yaml.models.JacksonProperty;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.amadeus.aud.libraries.yaml.models.utils.JacksonPropertyUtils;
import com.amadeus.aud.libraries.yaml.models.utils.KindMapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.CaseFormat;
import jakarta.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.hc.core5.net.URIBuilder;

public class KindBasedDeserializer extends JsonDeserializer<Object> {
  private static final String ERROR_MESSAGE_PREFIX = "Field '";
  private static final Class<?> defaultClass = Map.class;
  private final ObjectMapper objectMapper;
  private final Map<Class<?>, Map<String, JacksonProperty>> fieldMappings;
  private final KindMapper kindMapper;
  private final Collection<Message> messages;

  public KindBasedDeserializer(ObjectMapper objectMapper, Collection<Message> messages) {
    this.objectMapper = objectMapper;
    JacksonPropertyUtils propertyUtil = new JacksonPropertyUtils(objectMapper);

    this.kindMapper = new DefaultKindMapper();
    this.fieldMappings = new HashMap<>();
    this.fieldMappings.put(defaultClass, propertyUtil.getFieldMapping(defaultClass));
    for (Class<?> clazz : kindMapper.mappings().values()) {
      this.fieldMappings.put(clazz, propertyUtil.getFieldMapping(clazz));
    }
    this.messages = messages;
  }

  private void storeErrors(NodeInfo nodeInfo, String messageDetails) {
    Details details = new Details(nodeInfo.scope(), nodeInfo.kind(), nodeInfo.objectName());
    List<URI> links = new ArrayList<>();
    try {
      URI uri =
          new URIBuilder()
              .setScheme("file")
              .setPath(Optional.ofNullable(nodeInfo.location()).orElse("unknown"))
              .build();
      links.add(uri);
    } catch (URISyntaxException e) {
      // unreachable
    }
    Message message = Message.error(SyntaxChecker.CODE, messageDetails, details, null, links);
    messages.add(message);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object deserialize(final JsonParser jsonParser, final DeserializationContext ctxt)
      throws IOException {
    JsonNode rootNode = objectMapper.readTree(jsonParser);

    NodeInfo nodeInfo = extractNodeInfo(rootNode, ctxt);

    Class<? extends V1Base> targetClass;
    JsonNode convertedNode;
    String kind = nodeInfo.kind();
    if (kind != null && !nodeInfo.kind().isEmpty()) {
      targetClass =
          kindMapper.mappings().getOrDefault(kind, (Class<? extends V1Base>) defaultClass);
      if (targetClass == defaultClass) {
        String message =
            new StringBuilder().append("Kind '").append(kind).append("' is invalid.").toString();
        storeErrors(nodeInfo, message);
      }

      // Get JSON to field mapping
      Map<String, JacksonProperty> fieldMap = fieldMappings.get(targetClass);
      Objects.requireNonNull(fieldMap);
      // Convert keys accordingly
      convertedNode = convertKeys(rootNode, fieldMap, "", nodeInfo);
    } else {
      targetClass = (Class<? extends V1Base>) defaultClass;
      convertedNode = rootNode;
    }

    // Deserialize to target class
    try {
      return objectMapper.treeToValue(convertedNode, targetClass);
    } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
      String message =
          new StringBuilder()
              .append(ERROR_MESSAGE_PREFIX)
              .append(e.getPath().getLast().getFieldName())
              .append("' does not exist. Delete it.")
              .toString();
      storeErrors(nodeInfo, message);
      if (nodeInfo.objectName() == null) {
        String m =
            new StringBuilder().append("Unknown: ").append(convertedNode.toString()).toString();
        storeErrors(nodeInfo, m);
      }
    }
    return new Object();
  }

  private static NodeInfo extractNodeInfo(final JsonNode node, final DeserializationContext ctxt) {
    JsonNode jsonKindNode = node.get("kind");
    String kind = null;
    if (jsonKindNode != null) {
      kind = jsonKindNode.asText();
    }

    String location;
    Object rawContent = ctxt.getParser().currentLocation().contentReference().getRawContent();
    // ctxt.getParser().currentLocation().getLineNr() is not pointing at the right line, so unused
    if (rawContent != null) {
      location = rawContent.toString();
    } else {
      location = "Unknown";
    }

    String name = null;
    String scope = null;
    JsonNode metadataJsonNode = node.get("metadata");
    if (metadataJsonNode != null) {
      JsonNode nameJsonNode = metadataJsonNode.get("name");
      if (nameJsonNode != null) {
        name = nameJsonNode.asText();
      }
      JsonNode scopeJsonNode = metadataJsonNode.get("scope");
      if (scopeJsonNode != null) {
        scope = scopeJsonNode.asText();
      }
    }

    return new NodeInfo(location, kind, name, scope);
  }

  private ObjectNode convertKeys(
      final JsonNode rootNode,
      final Map<String, JacksonProperty> fieldMap,
      final String parentPrefix,
      final NodeInfo nodeInfo) {
    ObjectNode convertedNode = objectMapper.createObjectNode();

    for (Map.Entry<String, JsonNode> field : rootNode.properties()) {
      String originalKey = field.getKey();
      JsonNode value = field.getValue();

      if (value.isObject()) {
        String camelCaseKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, originalKey);
        String fullCamelCaseKey = computeKey(parentPrefix, camelCaseKey);
        value = convertKeys(value, fieldMap, fullCamelCaseKey, nodeInfo);
      }

      String originalFullKey = computeKey(parentPrefix, originalKey);
      JacksonProperty jacksonProperty = fieldMap.get(originalFullKey);

      String convertedKey = null;
      if (jacksonProperty != null) {
        convertedKey = jacksonProperty.fieldName();
        value = applyOnTheFlyFixes(jacksonProperty, value, originalFullKey, nodeInfo);
      }
      if (convertedKey == null || convertedKey.isEmpty()) {
        convertedKey = originalKey;
      }
      convertedNode.set(convertedKey, value);
    }
    return convertedNode;
  }

  private static String computeKey(String parentPrefix, String camelCaseKey) {
    String fullCamelCaseKey;
    if (parentPrefix.isEmpty()) {
      fullCamelCaseKey = camelCaseKey;
    } else {
      fullCamelCaseKey = parentPrefix + "." + camelCaseKey;
    }
    return fullCamelCaseKey;
  }

  private @Nullable JsonNode applyOnTheFlyFixes(
      final JacksonProperty jacksonProperty,
      JsonNode value,
      final String originalFullKey,
      final NodeInfo nodeInfo) {
    // Attempt to detect wrong user inputs
    Class<?> expectedType = jacksonProperty.fieldClass();
    if (expectedType == null) {
      return value;
    }

    if (value.isTextual() && Objects.equals(expectedType, Boolean.class)) {
      String textValue = value.textValue();
      if (textValue != null) {
        String message =
            new StringBuilder()
                .append(ERROR_MESSAGE_PREFIX)
                .append(originalFullKey)
                .append("' is a string, but a boolean must be set")
                .toString();
        storeErrors(nodeInfo, message);
      }
    } else if (value.isArray() && Objects.equals(expectedType, String.class)) {
      String message =
          new StringBuilder()
              .append(ERROR_MESSAGE_PREFIX)
              .append(originalFullKey)
              .append("' is a string, but an array is set")
              .toString();
      storeErrors(nodeInfo, message);
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
          if (shouldBeString && !mapValue.isTextual()) {
            String message =
                new StringBuilder()
                    .append(ERROR_MESSAGE_PREFIX)
                    .append(originalFullKey)
                    .append("' is an object with strings as values,\nbut '")
                    .append(valueField.getKey())
                    .append("' is a ")
                    .append(mapValue.getNodeType().name().toLowerCase(Locale.ROOT))
                    .toString();
            storeErrors(nodeInfo, message);
            mapValue = TextNode.valueOf(mapValue.toString());
          }
          convertedValue.set(valueField.getKey(), mapValue);
        }
        value = convertedValue;
      } else if (Objects.equals(expectedType, String.class)) {
        String message =
            new StringBuilder()
                .append(ERROR_MESSAGE_PREFIX)
                .append(originalFullKey)
                .append("' must be a string")
                .toString();
        storeErrors(nodeInfo, message);
        value = TextNode.valueOf(value.toString());
      }
    }
    return value;
  }

  @SuppressWarnings("java:S3776")
  private Object applyOnTheFlyFixes(
      JacksonProperty jacksonProperty, Object value, String originalFullKey, NodeInfo nodeInfo) {
    // Attempt to fix wrong user inputs
    Class<?> expectedType = jacksonProperty.fieldClass();
    if (expectedType != null && value != null) {
      if (expectedType.equals(String.class) && !(value instanceof String)) {
        String message =
            new StringBuilder()
                .append(ERROR_MESSAGE_PREFIX)
                .append(originalFullKey)
                .append("' must be a string.")
                .toString();
        storeErrors(nodeInfo, message);
        return value.toString();
      }

      if (value instanceof String && Objects.equals(expectedType, Boolean.class)) {
        String message =
            new StringBuilder()
                .append(ERROR_MESSAGE_PREFIX)
                .append(originalFullKey)
                .append("' must be a boolean.")
                .toString();
        storeErrors(nodeInfo, message);
        return false;
      }

      if (value instanceof Map<?, ?> mapValue && Objects.equals(expectedType, Map.class)) {
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
            String message =
                new StringBuilder()
                    .append(ERROR_MESSAGE_PREFIX)
                    .append(originalFullKey)
                    .append("' must be a string.")
                    .toString();
            storeErrors(nodeInfo, message);
            entryValue = entryValue.toString();
          }
          convertedMap.put(key, entryValue);
        }
        return convertedMap;
      }
    }
    return value;
  }
}

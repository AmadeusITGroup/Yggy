package com.amadeus.aud.libraries.yaml.models.utils;

import com.amadeus.aud.libraries.yaml.models.JacksonProperty;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.google.common.base.CaseFormat;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: This class is needed because in the OpenAPI spec and respective yaml files there is no
//   consistency in the naming of the properties. This class is used to map the properties to be
//   either camelCase or snake_case. This class should be removed once the OpenAPI spec is fixed.
public class JacksonPropertyUtils {

  // TODO: Fix the following property names in the OpenAPI spec in PRD and remove the Mapping.
  private final Map<String, String> propertyFixes =
      Map.of(
          "nanoID", "nanoId",
          "isNSGNeeded", "is_nsg_needed",
          "isPCI", "is_pci",
          "isPII", "is_pii",
          "isDBSynchro", "is_db_synchro");

  private final ObjectMapper objectMapper;

  private final Map<Class<?>, Map<String, JacksonProperty>> cache = new HashMap<>();

  public JacksonPropertyUtils(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public @Nullable Map<String, JacksonProperty> getFieldMapping(Class<?> targetClass) {
    if (cache.containsKey(targetClass)) {
      return cache.get(targetClass);
    }

    Map<String, JacksonProperty> fieldMap = buildFieldMapping(targetClass, "");
    cache.put(targetClass, fieldMap);
    return fieldMap;
  }

  private Map<String, JacksonProperty> buildFieldMapping(
      Class<?> targetClass, String parentPrefix) {
    JavaType javaType = objectMapper.constructType(targetClass);
    BeanDescription beanDescription = objectMapper.getSerializationConfig().introspect(javaType);

    Map<String, JacksonProperty> fieldMap = new HashMap<>();

    List<BeanPropertyDefinition> properties = beanDescription.findProperties();
    for (BeanPropertyDefinition property : properties) {
      PropertyName propertyName = property.getFullName();
      String originalFieldName = propertyName.getSimpleName();
      String fieldName = propertyFixes.getOrDefault(originalFieldName, originalFieldName);

      String camelCaseName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_CAMEL, fieldName);
      String snakeCaseName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);

      String fullCamelCaseName =
          parentPrefix.isEmpty() ? camelCaseName : String.join(".", parentPrefix, camelCaseName);
      String fullSnakeCaseName =
          parentPrefix.isEmpty() ? snakeCaseName : String.join(".", parentPrefix, snakeCaseName);

      AnnotatedMember annotatedMember = property.getPrimaryMember();
      Class<?> fieldType = null;
      Class<?> contentClass = null;
      Class<?> keyClass = null;
      if (annotatedMember != null) {
        JavaType type = annotatedMember.getType();
        if (type != null) {
          fieldType = type.getRawClass();
          JavaType contentType = type.getContentType();
          if (contentType != null) {
            contentClass = contentType.getRawClass();
          }
          JavaType keyType = type.getKeyType();
          if (keyType != null) {
            keyClass = keyType.getRawClass();
          }
        }
      }

      JacksonProperty jacksonProperty =
          new JacksonProperty(originalFieldName, fieldType, contentClass, keyClass);
      fieldMap.put(fullCamelCaseName, jacksonProperty);
      fieldMap.put(fullSnakeCaseName, jacksonProperty);

      // Recursively handle nested objects
      if (fieldType != null
          && !fieldType.isPrimitive()
          && !fieldType.isEnum()
          && !fieldType.getName().startsWith("java.")) {
        Map<String, JacksonProperty> res = cache.get(fieldType);
        if (res == null) {
          res = buildFieldMapping(fieldType, fullCamelCaseName);
          cache.put(fieldType, res);
        }
        fieldMap.putAll(res);
      }
    }
    return fieldMap;
  }
}

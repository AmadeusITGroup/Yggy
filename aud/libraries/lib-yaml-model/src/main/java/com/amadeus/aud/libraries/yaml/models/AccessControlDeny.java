package com.amadeus.aud.libraries.yaml.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(using = AccessControlDeny.AccessControlDenyDeserializer.class)
@JsonSerialize(using = AccessControlDeny.AccessControlDenySerializer.class)
public class AccessControlDeny extends AbstractOpenApiSchema {
  private static final Logger log = Logger.getLogger(AccessControlDeny.class.getName());

  /** A map of oneOf/anyOf descendants for each model class. */
  private static final Map<Class<?>, Map<String, Class<?>>> modelDescendants = new HashMap<>();

  public static class AccessControlDenySerializer extends StdSerializer<AccessControlDeny> {
    public AccessControlDenySerializer(Class<AccessControlDeny> t) {
      super(t);
    }

    public AccessControlDenySerializer() {
      this(null);
    }

    @Override
    public void serialize(AccessControlDeny value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      jgen.writeObject(value.getActualInstance());
    }
  }

  public static class AccessControlDenyDeserializer extends StdDeserializer<AccessControlDeny> {
    public AccessControlDenyDeserializer() {
      this(AccessControlDeny.class);
    }

    public AccessControlDenyDeserializer(Class<?> vc) {
      super(vc);
    }

    @Override
    public AccessControlDeny deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      JsonNode tree = ctxt.readTree(jp);

      Object deserialized = null;
      // deserialize AccessControlDenyAnyOf
      try {
        deserialized = tree.traverse(jp.getCodec()).readValueAs(AccessControlDenyAnyOf.class);
        AccessControlDeny ret = new AccessControlDeny();
        ret.setActualInstance(deserialized);
        return ret;
      } catch (Exception e) {
        // deserialization failed, continue, log to help debugging
        log.log(Level.FINER, "Input data does not match 'AccessControlDeny'", e);
      }

      // deserialize AccessDenialMessage
      try {
        deserialized = tree.traverse(jp.getCodec()).readValueAs(AccessDenialMessage.class);
        AccessControlDeny ret = new AccessControlDeny();
        ret.setActualInstance(deserialized);
        return ret;
      } catch (Exception e) {
        // deserialization failed, continue, log to help debugging
        log.log(Level.FINER, "Input data does not match 'AccessControlDeny'", e);
      }

      throw new IOException("Failed deserialization for AccessControlDeny: no match found");
    }

    /** Handle deserialization of the 'null' value. */
    @Override
    public AccessControlDeny getNullValue(DeserializationContext ctxt) throws JsonMappingException {
      throw new JsonMappingException(ctxt.getParser(), "AccessControlDeny cannot be null");
    }
  }

  // store a list of schema names defined in anyOf
  public static final Map<String, Class<?>> schemas = new HashMap<String, Class<?>>();

  public AccessControlDeny() {
    super("anyOf", Boolean.FALSE);
  }

  @SuppressWarnings("this-escape")
  public AccessControlDeny(AccessControlDenyAnyOf o) {
    super("anyOf", Boolean.FALSE);
    setActualInstance(o);
  }

  @SuppressWarnings("this-escape")
  public AccessControlDeny(AccessDenialMessage o) {
    super("anyOf", Boolean.FALSE);
    setActualInstance(o);
  }

  static {
    schemas.put("AccessControlDenyAnyOf", AccessControlDenyAnyOf.class);
    schemas.put("AccessDenialMessage", AccessDenialMessage.class);
    registerDescendants(AccessControlDeny.class, Collections.unmodifiableMap(schemas));
  }

  @Override
  public Map<String, Class<?>> getSchemas() {
    return AccessControlDeny.schemas;
  }

  /**
   * Set the instance that matches the anyOf child schema, check the instance parameter is valid
   * against the anyOf child schemas: AccessControlDenyAnyOf, AccessDenialMessage
   *
   * <p>It could be an instance of the 'anyOf' schemas. The anyOf child schemas may themselves be a
   * composed schema (allOf, anyOf, anyOf).
   */
  @Override
  public void setActualInstance(Object instance) {
    if (isInstanceOf(AccessControlDenyAnyOf.class, instance, new HashSet<Class<?>>())) {
      super.setActualInstance(instance);
      return;
    }

    if (isInstanceOf(AccessDenialMessage.class, instance, new HashSet<Class<?>>())) {
      super.setActualInstance(instance);
      return;
    }

    throw new RuntimeException(
        "Invalid instance type. Must be AccessControlDenyAnyOf, AccessDenialMessage");
  }

  /**
   * Get the actual instance, which can be the following: AccessControlDenyAnyOf,
   * AccessDenialMessage
   *
   * @return The actual instance (AccessControlDenyAnyOf, AccessDenialMessage)
   */
  @Override
  public Object getActualInstance() {
    return super.getActualInstance();
  }

  /**
   * Get the actual instance of `AccessControlDenyAnyOf`. If the actual instance is not
   * `AccessControlDenyAnyOf`, the ClassCastException will be thrown.
   *
   * @return The actual instance of `AccessControlDenyAnyOf`
   * @throws ClassCastException if the instance is not `AccessControlDenyAnyOf`
   */
  public AccessControlDenyAnyOf getAccessControlDenyAnyOf() throws ClassCastException {
    return (AccessControlDenyAnyOf) super.getActualInstance();
  }

  /**
   * Get the actual instance of `AccessDenialMessage`. If the actual instance is not
   * `AccessDenialMessage`, the ClassCastException will be thrown.
   *
   * @return The actual instance of `AccessDenialMessage`
   * @throws ClassCastException if the instance is not `AccessDenialMessage`
   */
  public AccessDenialMessage getAccessDenialMessage() throws ClassCastException {
    return (AccessDenialMessage) super.getActualInstance();
  }

  private static boolean isInstanceOf(
      Class<?> modelClass, Object inst, Set<Class<?>> visitedClasses) {
    if (modelClass.isInstance(inst)) {
      // This handles the 'allOf' use case with single parent inheritance.
      return true;
    }
    if (visitedClasses.contains(modelClass)) {
      // This is to prevent infinite recursion when the composed schemas have
      // a circular dependency.
      return false;
    }
    visitedClasses.add(modelClass);

    // Traverse the oneOf/anyOf composed schemas.
    Map<String, Class<?>> descendants = modelDescendants.get(modelClass);
    if (descendants != null) {
      for (Class<?> childType : descendants.values()) {
        if (isInstanceOf(childType, inst, visitedClasses)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Register the oneOf/anyOf descendants of the modelClass.
   *
   * @param modelClass the model class
   * @param descendants a map of oneOf/anyOf descendants.
   */
  private static void registerDescendants(Class<?> modelClass, Map<String, Class<?>> descendants) {
    modelDescendants.put(modelClass, descendants);
  }
}

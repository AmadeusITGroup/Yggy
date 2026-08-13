package com.amadeus.aud.libraries.yaml.models.resolvers;

import com.amadeus.aud.libraries.yaml.models.V1Base;
import com.amadeus.aud.libraries.yaml.models.utils.DefaultKindMapper;
import com.amadeus.aud.libraries.yaml.models.utils.KindMapper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.annotation.Nullable;
import org.jboss.logging.Logger;

public class DefaultKindTypeIdResolver implements TypeIdResolver {
  private static final Logger log = Logger.getLogger(DefaultKindTypeIdResolver.class);
  private static final KindMapper kindMapper = new DefaultKindMapper();
  private JavaType baseType;

  @Override
  public void init(JavaType baseType) {
    this.baseType = baseType;
  }

  @Override
  public @Nullable String idFromValue(Object value) {
    if (value instanceof V1Base base) {
      return base.getKind();
    }
    return null;
  }

  @Override
  public @Nullable String idFromValueAndType(Object value, Class<?> suggestedType) {
    return idFromValue(value);
  }

  @Override
  public @Nullable String idFromBaseType() {
    return null;
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    Class<?> clazz = kindMapper.getClass(id);
    if (clazz == null) {
      clazz = baseType.getRawClass();
      log.warnf("Unknown kind: %s, falling back to base type: %s", id, clazz.getName());
    }
    return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
  }

  @Override
  public String getDescForKnownTypeIds() {
    return "kind field mapped to class via KindMapperUtils";
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.NAME;
  }
}

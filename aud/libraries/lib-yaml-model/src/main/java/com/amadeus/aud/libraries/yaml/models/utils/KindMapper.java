package com.amadeus.aud.libraries.yaml.models.utils;

import com.amadeus.aud.libraries.yaml.models.V1Base;
import jakarta.annotation.Nullable;
import java.util.Map;

public interface KindMapper {
  Map<String, Class<? extends V1Base>> mappings();

  default @Nullable Class<? extends V1Base> getClass(String kind) {
    return mappings().get(kind);
  }
}

package com.amadeus.aud.services.apis.models.core.matrix;

import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import com.fasterxml.jackson.annotation.JsonValue;
import io.smallrye.common.constraint.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record SecurityMatrix(Map<String, Map<String, V1ConnectionSpec.ControlEnum>> matrix) {

  public void addControl(String source, String target, V1ConnectionSpec.ControlEnum control) {
    matrix.computeIfAbsent(source, k -> new HashMap<>()).put(target, control);
  }

  public @Nullable V1ConnectionSpec.ControlEnum getControl(String source, String target) {
    Map<String, V1ConnectionSpec.ControlEnum> targetMap = matrix.get(source);
    return Optional.ofNullable(targetMap).map(m -> m.get(target)).orElse(null);
  }

  @JsonValue
  public Map<String, Map<String, V1ConnectionSpec.ControlEnum>> toJson() {
    return matrix;
  }
}

package com.amadeus.aud.services.apis.models.core.matrix;

import com.amadeus.aud.libraries.yaml.models.V1ConnectionPhaseSpec;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public record SecurityMatrixMacrophase(
    Map<String, Map<String, V1ConnectionPhaseSpec.ControlEnum>> matrix) {

  public void addPhaseControl(
      String source, String target, V1ConnectionPhaseSpec.ControlEnum control) {
    matrix.computeIfAbsent(source, k -> new HashMap<>()).put(target, control);
  }

  @JsonValue
  public Map<String, Map<String, V1ConnectionPhaseSpec.ControlEnum>> toPhaseJson() {
    return matrix;
  }
}

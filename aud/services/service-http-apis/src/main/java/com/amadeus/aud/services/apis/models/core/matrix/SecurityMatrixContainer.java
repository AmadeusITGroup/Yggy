package com.amadeus.aud.services.apis.models.core.matrix;

import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;

public record SecurityMatrixContainer(
    Map<V1ConnectionSpec.TypeEnum, SecurityMatrix> securityMatrices) {

  public void addSecurityMatrix(V1ConnectionSpec.TypeEnum type, SecurityMatrix securityMatrix) {
    securityMatrices.put(type, securityMatrix);
  }

  @JsonValue
  public Map<V1ConnectionSpec.TypeEnum, SecurityMatrix> toJson() {
    return securityMatrices;
  }

  public static SecurityMatrixContainerBuilder builder() {
    return new SecurityMatrixContainerBuilder();
  }
}

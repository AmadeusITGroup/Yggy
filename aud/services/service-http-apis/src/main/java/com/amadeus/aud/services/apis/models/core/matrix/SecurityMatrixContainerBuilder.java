package com.amadeus.aud.services.apis.models.core.matrix;

import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import java.util.HashMap;
import java.util.Map;

public class SecurityMatrixContainerBuilder {
  private final Map<V1ConnectionSpec.TypeEnum, SecurityMatrix> securityMatrices = new HashMap<>();

  public SecurityMatrixContainerBuilder withZoneTypes(Map<String, ZoneType> zoneTypes) {
    for (V1ConnectionSpec.TypeEnum type : V1ConnectionSpec.TypeEnum.values()) {
      SecurityMatrix securityMatrix = new SecurityMatrix(new HashMap<>());
      for (ZoneType source : zoneTypes.values()) {
        for (ZoneType target : zoneTypes.values()) {
          addDefaultControl(securityMatrix, source, target);
          if (shouldApplyOverrides(source, target)) {
            updateOverrideControl(securityMatrix, type, source, target);
          }
        }
      }
      this.securityMatrices.put(type, securityMatrix);
    }
    return this;
  }

  private void addDefaultControl(SecurityMatrix matrix, ZoneType source, ZoneType target) {
    matrix.addControl(source.getName(), target.getName(), V1ConnectionSpec.ControlEnum.RED);
  }

  private boolean shouldApplyOverrides(ZoneType source, ZoneType target) {
    return !(source.isTechnical() || target.isTechnical() || source.isPaas() || target.isPaas());
  }

  private void updateOverrideControl(
      SecurityMatrix matrix, V1ConnectionSpec.TypeEnum type, ZoneType source, ZoneType target) {
    for (Connection connection : source.getConnections()) {
      if (connection.getSourceZoneType().equals(source)
          && connection.getDestinationZoneType().equals(target)
          && connection.getConnectionType().equals(type)) {
        matrix.addControl(source.getName(), target.getName(), connection.getSecurityControl());
        break;
      }
    }
  }

  public SecurityMatrixContainer build() {
    return new SecurityMatrixContainer(securityMatrices);
  }
}

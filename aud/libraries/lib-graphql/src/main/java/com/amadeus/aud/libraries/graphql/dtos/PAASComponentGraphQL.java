package com.amadeus.aud.libraries.graphql.dtos;

import com.amadeus.aud.libraries.yaml.models.PAASComponent;
import io.quarkus.logging.Log;
import jakarta.annotation.Nullable;
import java.util.Map;
import org.eclipse.microprofile.graphql.Type;

@Type("PAASComponentDTO")
public record PAASComponentGraphQL(
    String name,
    String namespace,
    Map<String, String> identifier,
    String inNetwork,
    String outNetwork,
    Map<String, String> labels) {

  public static @Nullable PAASComponentGraphQL from(@Nullable PAASComponent component) {
    if (component == null) {
      return null;
    }

    Map<String, String> identifierMap = null;
    if (component.getIdentifier() != null) {
      try {
        @SuppressWarnings("unchecked")
        Map<String, String> casted = (Map<String, String>) component.getIdentifier();
        identifierMap = casted;
      } catch (ClassCastException e) {
        Log.warnf(
            "Failed to cast identifier to Map<String, String> for component %s: %s",
            component.getName(), e.getMessage());
      }
    }

    return new PAASComponentGraphQL(
        component.getName(),
        component.getNamespace(),
        identifierMap,
        component.getInNetwork(),
        component.getOutNetwork(),
        component.getLabels());
  }
}

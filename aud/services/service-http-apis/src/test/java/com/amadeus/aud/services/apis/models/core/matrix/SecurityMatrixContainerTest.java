package com.amadeus.aud.services.apis.models.core.matrix;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.models.core.db.Connection;
import com.amadeus.aud.libraries.models.core.db.ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1Connection;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec;
import com.amadeus.aud.libraries.yaml.models.V1ConnectionSpec.TypeEnum;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1ZoneType;
import com.amadeus.aud.libraries.yaml.models.V1ZoneTypeSpec;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SecurityMatrixContainerTest {

  private final V1ZoneTypeSpec sampleZoneTypeConfig =
      new V1ZoneTypeSpec().isTechnical(false).isPaas(false).isReal(true);
  private final V1ZoneTypeSpec sampleZoneTypeConfigWithPaas =
      new V1ZoneTypeSpec().isTechnical(true).isPaas(true).isReal(false);

  private final V1Metadata zoneAMetadata = new V1Metadata().name("zoneA");
  private final ZoneType zoneA =
      new ZoneType(new V1ZoneType().metadata(zoneAMetadata).spec(sampleZoneTypeConfig));

  private final V1Metadata zoneBMetadata = new V1Metadata().name("zoneB");
  private final ZoneType zoneB =
      new ZoneType(new V1ZoneType().metadata(zoneBMetadata).spec(sampleZoneTypeConfig));

  private final ZoneType zoneAWithPaas =
      new ZoneType(new V1ZoneType().metadata(zoneAMetadata).spec(sampleZoneTypeConfigWithPaas));

  @Test
  void addSecurityMatrix() {
    SecurityMatrix dummyMatrix = new SecurityMatrix(new HashMap<>());

    SecurityMatrixContainer container =
        new SecurityMatrixContainer(new EnumMap<>(V1ConnectionSpec.TypeEnum.class));
    container.addSecurityMatrix(TypeEnum.INTERNAL, dummyMatrix);

    assertThat(container.securityMatrices()).containsEntry(TypeEnum.INTERNAL, dummyMatrix);
  }

  @Test
  void generateSecurityMatricesWithoutConnections() {
    Map<String, ZoneType> zoneTypes = new HashMap<>();

    zoneA.setConnections(new HashSet<>());
    zoneB.setConnections(new HashSet<>());
    zoneTypes.put(zoneA.getName(), zoneA);
    zoneTypes.put(zoneB.getName(), zoneB);

    SecurityMatrixContainer container =
        SecurityMatrixContainer.builder().withZoneTypes(zoneTypes).build();

    // For each connection type defined in V1ConnectionSpec.TypeEnum,
    // every zone pair should have the default control RED.
    Map<V1ConnectionSpec.TypeEnum, SecurityMatrix> securityMatrices = container.securityMatrices();
    assertThat(securityMatrices).hasSize(2);
    securityMatrices.forEach(
        (V1ConnectionSpec.TypeEnum type, SecurityMatrix matrix) -> {
          assertThat(matrix.getControl("zoneA", "zoneA"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
          assertThat(matrix.getControl("zoneA", "zoneB"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
          assertThat(matrix.getControl("zoneB", "zoneA"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
          assertThat(matrix.getControl("zoneB", "zoneB"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
        });
  }

  @Test
  void generateSecurityMatricesWithConnectionOverride() {
    V1Metadata connectionMetadata = new V1Metadata().name("connection");
    V1ConnectionSpec connectionSpec =
        new V1ConnectionSpec()
            .level(V1ConnectionSpec.LevelEnum.L4)
            .control(V1ConnectionSpec.ControlEnum.GREEN)
            .type(TypeEnum.INTERNAL)
            .sourceZoneType(zoneA.getName())
            .destinationZoneType(zoneB.getName());
    Connection connection =
        new Connection(new V1Connection().spec(connectionSpec).metadata(connectionMetadata));
    connection.setSourceZoneType(zoneA);
    connection.setDestinationZoneType(zoneB);
    zoneA.setConnections(Set.of(connection));
    zoneB.setConnections(Set.of(connection));

    Map<String, ZoneType> zoneTypes = new HashMap<>();
    zoneTypes.put(zoneA.getName(), zoneA);
    zoneTypes.put(zoneB.getName(), zoneB);

    SecurityMatrixContainer container =
        SecurityMatrixContainer.builder().withZoneTypes(zoneTypes).build();

    SecurityMatrix matrix = container.securityMatrices().get(TypeEnum.INTERNAL);
    // Validate that for the pair A->B the control is overridden to GREEN,
    // while other pairs remain at the default RED.
    assertThat(matrix.getControl("zoneA", "zoneA")).isEqualTo(V1ConnectionSpec.ControlEnum.RED);
    assertThat(matrix.getControl("zoneA", "zoneB")).isEqualTo(V1ConnectionSpec.ControlEnum.GREEN);
    assertThat(matrix.getControl("zoneB", "zoneA")).isEqualTo(V1ConnectionSpec.ControlEnum.RED);
    assertThat(matrix.getControl("zoneB", "zoneB")).isEqualTo(V1ConnectionSpec.ControlEnum.RED);

    SecurityMatrix externalMatrix = container.securityMatrices().get(TypeEnum.EXTERNAL);
    assertThat(externalMatrix.getControl("zoneA", "zoneA"))
        .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
    assertThat(externalMatrix.getControl("zoneA", "zoneB"))
        .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
    assertThat(externalMatrix.getControl("zoneB", "zoneA"))
        .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
    assertThat(externalMatrix.getControl("zoneB", "zoneB"))
        .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
  }

  @Test
  void generateSecurityMatricesWithConnectionOverrideWithTechnicalAndPaas() {
    V1Metadata connectionMetadata = new V1Metadata().name("connection");
    V1ConnectionSpec connectionSpec =
        new V1ConnectionSpec()
            .level(V1ConnectionSpec.LevelEnum.L4)
            .control(V1ConnectionSpec.ControlEnum.GREEN)
            .type(TypeEnum.INTERNAL)
            .sourceZoneType(zoneAWithPaas.getName())
            .destinationZoneType(zoneB.getName());
    Connection connection =
        new Connection(new V1Connection().spec(connectionSpec).metadata(connectionMetadata));
    connection.setSourceZoneType(zoneAWithPaas);
    connection.setDestinationZoneType(zoneB);
    zoneAWithPaas.setConnections(Set.of(connection));
    zoneB.setConnections(Set.of(connection));

    Map<String, ZoneType> zoneTypes = new HashMap<>();
    zoneTypes.put(zoneAWithPaas.getName(), zoneAWithPaas);
    zoneTypes.put(zoneB.getName(), zoneB);

    SecurityMatrixContainer container =
        SecurityMatrixContainer.builder().withZoneTypes(zoneTypes).build();

    // Validate that for the pair A->B the control remains RED,
    // since both zone A is technical and PaaS.
    Map<V1ConnectionSpec.TypeEnum, SecurityMatrix> securityMatrices = container.securityMatrices();
    assertThat(securityMatrices).hasSize(2);
    securityMatrices.forEach(
        (V1ConnectionSpec.TypeEnum type, SecurityMatrix matrix) -> {
          assertThat(matrix.getControl("zoneA", "zoneA"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
          assertThat(matrix.getControl("zoneA", "zoneB"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
          assertThat(matrix.getControl("zoneB", "zoneA"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
          assertThat(matrix.getControl("zoneB", "zoneB"))
              .isEqualTo(V1ConnectionSpec.ControlEnum.RED);
        });
  }
}

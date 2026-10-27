package com.amadeus.aud.libraries.models.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.amadeus.aud.libraries.models.core.db.SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstance;
import com.amadeus.aud.libraries.yaml.models.V1SubnetInstanceSpec;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SubnetInstanceTest {
  @Nested
  class TestNullCidrConstructor {
    @Test
    void translatesDataEndpointsToYamlEndpoints() {
      V1MetadataInstance v1MetadataInstance =
          new V1MetadataInstance()
              .name("test-name")
              .scope("test-scope")
              .landingzone("test-landingzone");
      V1SubnetInstanceSpec v1SubnetInstanceSpec = new V1SubnetInstanceSpec().subnetType("Mixed");
      V1SubnetInstance v1SubnetInstance = new V1SubnetInstance().spec(v1SubnetInstanceSpec);
      v1SubnetInstance.metadata(v1MetadataInstance);
      SubnetInstance subnetInstance =
          assertDoesNotThrow(() -> new SubnetInstance(v1SubnetInstance));
      assertNotNull(subnetInstance);
      assertThat(subnetInstance.getData().getSpec().getSubnetType()).isEqualTo("Mixed");
    }
  }
}

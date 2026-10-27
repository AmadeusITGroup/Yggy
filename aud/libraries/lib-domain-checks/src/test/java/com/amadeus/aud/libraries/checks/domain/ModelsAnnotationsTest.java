package com.amadeus.aud.libraries.checks.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.types.DomainCheck;
import com.amadeus.aud.libraries.yaml.models.V1Component;
import com.amadeus.aud.libraries.yaml.models.V1ComponentInstance;
import com.amadeus.aud.libraries.yaml.models.V1Dependency;
import com.amadeus.aud.libraries.yaml.models.V1DependencyInstance;
import com.amadeus.aud.libraries.yaml.models.V1DependencySpec;
import com.amadeus.aud.libraries.yaml.models.V1Metadata;
import com.amadeus.aud.libraries.yaml.models.V1MetadataInstance;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ModelsAnnotations}.
 *
 * <p>Key guarantee: our {@code META-INF/validation.xml} + {@code v1base-validation-override.xml} in
 * {@code service-http-apis} are NOT on the classpath here. This test uses {@code
 * Validation.buildDefaultValidatorFactory()} exactly as the real checker does, with no classpath
 * overrides — proving the checker's behaviour is unchanged.
 */
class ModelsAnnotationsTest {
  private static final String DOMAIN = "test-domain";
  private final DomainCheck sut = new ModelsAnnotations();

  @Test
  void checkId() {
    assertThat(((ModelsAnnotations) sut).id()).isEqualTo("DOM00001");
  }

  @Test
  void noElementsProducesNoMessages() {
    Collection<Message> result = sut.check(DOMAIN, Map.of(V1Component.class, List.of()));

    assertThat(result).isEmpty();
  }

  @Nested
  class V1BaseSubclass {

    @Test
    void missingApiVersionProducesOneMessage() {
      V1Component element = new V1Component();
      V1Metadata metadata = new V1Metadata();
      metadata.setName("my-component");
      metadata.setScope("my-scope");
      element.setMetadata(metadata);

      Collection<Message> result = sut.check(DOMAIN, Map.of(V1Component.class, List.of(element)));

      assertThat(result)
          .singleElement()
          .satisfies(
              m -> assertThat(m.message().toString()).isEqualTo("apiVersion must not be null"));
    }

    @Test
    void invalidApiVersionProducesOneMessage() {
      V1Component element = new V1Component();
      V1Metadata metadata = new V1Metadata();
      metadata.setName("my-component");
      metadata.setScope("my-scope");
      element.setMetadata(metadata);
      element.setApiVersion("v-1");

      Collection<Message> result = sut.check(DOMAIN, Map.of(V1Component.class, List.of(element)));

      assertThat(result)
          .singleElement()
          .satisfies(
              m -> assertThat(m.message().toString()).isEqualTo("apiVersion must match \"v1\""));
    }

    @Test
    void validMetadataProducesNoMessages() {
      V1Component element = new V1Component();
      V1Metadata metadata = new V1Metadata();
      metadata.setName("my-component");
      metadata.setScope("my-scope");
      element.setApiVersion("v1");
      element.setMetadata(metadata);

      Collection<Message> result = sut.check(DOMAIN, Map.of(V1Component.class, List.of(element)));

      assertThat(result).isEmpty();
    }

    @Test
    void missingNameOnMetadataProducesMessage() {
      V1Component element = new V1Component();
      V1Metadata metadata = new V1Metadata();
      metadata.setName(null); //  — should trigger a violation
      metadata.setScope("my-scope");
      element.setMetadata(metadata);

      Collection<Message> result = sut.check(DOMAIN, Map.of(V1Component.class, List.of(element)));

      assertThat(result)
          .isNotEmpty()
          .anySatisfy(m -> assertThat(m.message().toString()).contains("name"));
    }
  }

  @Nested
  class V1BaseInstanceSubclass {

    @Test
    void validMetadataInstanceProducesNoMessages() {
      V1ComponentInstance element = new V1ComponentInstance();
      V1MetadataInstance metadata = new V1MetadataInstance();
      metadata.setName("my-instance");
      metadata.setScope("my-scope");
      metadata.setLandingzone("my-landingzone"); //  on V1MetadataInstance
      element.setMetadata(metadata);
      element.setApiVersion("v1");

      Collection<Message> result =
          new ModelsAnnotations()
              .check(DOMAIN, Map.of(V1ComponentInstance.class, List.of(element)));

      assertThat(result).isEmpty();
    }

    @Test
    void missingNameOnMetadataInstanceProducesMessage() {
      V1ComponentInstance element = new V1ComponentInstance();
      V1MetadataInstance metadata = new V1MetadataInstance();
      metadata.setName(null); //  — must still be enforced
      metadata.setScope("my-scope");
      metadata.setLandingzone("my-landingzone"); //  on V1MetadataInstance
      element.setMetadata(metadata);

      Collection<Message> result =
          new ModelsAnnotations()
              .check(DOMAIN, Map.of(V1ComponentInstance.class, List.of(element)));

      assertThat(result)
          .isNotEmpty()
          .anySatisfy(m -> assertThat(m.message().toString()).contains("name"));
    }
  }

  @Test
  void missingScopeOnMetadataInstanceProducesMessage() {
    V1DependencyInstance element = new V1DependencyInstance();
    V1MetadataInstance metadata = new V1MetadataInstance();
    metadata.setName(null);
    metadata.setScope(null);
    metadata.setLandingzone(null);
    metadata.setLabels(Map.of("hello", "data"));
    metadata.setComment("my comment is here");
    element.setMetadata(metadata);
    element.setSpec(null);
    element.setKind("DependencyInstance");
    element.setApiVersion("v1");

    Collection<Message> result =
        sut.check(DOMAIN, Map.of(V1DependencyInstance.class, List.of(element)));

    assertThat(result)
        .isNotEmpty()
        .anySatisfy(m -> assertThat(m.message().toString()).contains("scope"));
  }

  @Test
  void missingSpecAttributesProducesMessage() {
    V1Dependency element = new V1Dependency();
    V1MetadataInstance metadata = new V1MetadataInstance();
    metadata.setName("my-name");
    metadata.setScope("my-scope");
    metadata.setLandingzone("my-lz");
    metadata.setLabels(Map.of("hello", "data"));
    metadata.setComment("my comment is here");
    element.setMetadata(metadata);
    V1DependencySpec spec = new V1DependencySpec();
    element.setSpec(spec);
    element.setKind("Dependency");
    element.setApiVersion("v1");

    Collection<Message> result = sut.check(DOMAIN, Map.of(V1Dependency.class, List.of(element)));

    assertThat(result)
        .isNotEmpty()
        .anySatisfy(m -> assertThat(m.message().toString()).contains("component"));
  }
}

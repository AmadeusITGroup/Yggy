package com.amadeus.aud.services.apis.services.endpoints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.graph.models.Graph;
import com.amadeus.aud.libraries.engine.graph.services.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("VersionGraphCacheService Tests")
class VersionGraphCacheServiceTest {

  @Mock private GraphService graphService;
  @Mock private Version version;

  private VersionGraphCacheService service;

  @BeforeEach
  void setUp() {
    service = new VersionGraphCacheService(graphService);
  }

  @Test
  @DisplayName("should build graph with correct version number")
  void testBuildVersionGraph() {
    when(version.getVersionNumber()).thenReturn("1.0.0");

    Graph graph = service.buildVersionGraph(version);

    assertThat(graph).isNotNull();
    assertThat(graph.getVersionNumber()).isEqualTo("1.0.0");

    verify(graphService).populateGraphFromDependencies(any(), eq(version));
  }

  @Test
  @DisplayName("should handle different version numbers")
  void testBuildVersionGraphWithDifferentVersions() {
    Version version1 = mock(Version.class);
    when(version1.getVersionNumber()).thenReturn("1.0.0");

    Version version2 = mock(Version.class);
    when(version2.getVersionNumber()).thenReturn("2.0.0");

    Graph graph1 = service.buildVersionGraph(version1);
    Graph graph2 = service.buildVersionGraph(version2);

    assertThat(graph1).isNotNull();
    assertThat(graph2).isNotNull();
    assertThat(graph1.getVersionNumber()).isEqualTo("1.0.0");
    assertThat(graph2.getVersionNumber()).isEqualTo("2.0.0");

    verify(graphService).populateGraphFromDependencies(any(), eq(version1));
    verify(graphService).populateGraphFromDependencies(any(), eq(version2));
  }

  @Test
  @DisplayName("should call GraphService.populateGraphFromDependencies with correct Version")
  void testGraphServiceIsCalledWithCorrectVersion() {
    when(version.getVersionNumber()).thenReturn("3.0.0");
    ArgumentCaptor<Version> versionCaptor = ArgumentCaptor.forClass(Version.class);

    service.buildVersionGraph(version);

    verify(graphService).populateGraphFromDependencies(any(), versionCaptor.capture());
    assertThat(versionCaptor.getValue()).isEqualTo(version);
  }
}

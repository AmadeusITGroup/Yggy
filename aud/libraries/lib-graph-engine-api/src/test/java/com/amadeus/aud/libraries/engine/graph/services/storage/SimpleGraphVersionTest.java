package com.amadeus.aud.libraries.engine.graph.services.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.engine.graph.models.GraphStatistics;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SimpleGraphVersionTest {

  @Test
  void exposesVersionDataAndDefaultsRankToZero() {
    LocalDateTime now = LocalDateTime.now();
    GraphStatistics stats = new GraphStatistics(Map.of("s", 1L), Map.of("s", 2L), 2L, 1L);

    SimpleGraphVersion withDefaultRank = new SimpleGraphVersion("1.0.0", now, stats);
    SimpleGraphVersion withRank = new SimpleGraphVersion("1.0.1", now, stats, 9);

    assertThat(withDefaultRank.getVersionRank()).isZero();
    assertThat(withRank.getVersion()).isEqualTo("1.0.1");
    assertThat(withRank.getCreatedAt()).isEqualTo(now);
    assertThat(withRank.toStatistics()).isEqualTo(stats);
    assertThat(withRank.getVersionRank()).isEqualTo(9);
  }
}

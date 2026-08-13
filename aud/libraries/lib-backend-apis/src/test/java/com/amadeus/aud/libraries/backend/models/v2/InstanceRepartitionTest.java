package com.amadeus.aud.libraries.backend.models.v2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class InstanceRepartitionTest {

  @Test
  void testInstanceRepartition() {
    InstanceRepartition response =
        new InstanceRepartition(
            1,
            new InstanceBreakdowns(
                List.of(new BreakdownCount("continent-1", 1)),
                List.of(new BreakdownCount("lz-1", 1)),
                List.of(new BreakdownCount("phase-1", 1)),
                List.of(new BreakdownCount("ns-1", 1)),
                List.of(new BreakdownCount("paas-1", 1)),
                List.of(new BreakdownCount("sz-1", 1)),
                List.of(new BreakdownCount("target-1", 1))));

    assertThat(response.total()).isEqualTo(1);
    assertThat(response.breakdowns().macrophase())
        .containsExactly(new BreakdownCount("phase-1", 1));
    assertThat(response.breakdowns().securityZone()).containsExactly(new BreakdownCount("sz-1", 1));
    assertThat(response.breakdowns().continent())
        .containsExactly(new BreakdownCount("continent-1", 1));
    assertThat(response.breakdowns().paas()).containsExactly(new BreakdownCount("paas-1", 1));
    assertThat(response.breakdowns().namespace()).containsExactly(new BreakdownCount("ns-1", 1));
    assertThat(response.breakdowns().landingZone()).containsExactly(new BreakdownCount("lz-1", 1));
    assertThat(response.breakdowns().target()).containsExactly(new BreakdownCount("target-1", 1));
  }
}

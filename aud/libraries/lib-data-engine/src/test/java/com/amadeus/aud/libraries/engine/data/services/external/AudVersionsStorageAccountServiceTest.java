package com.amadeus.aud.libraries.engine.data.services.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.engine.data.services.external.storage.SingleVersion;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AudVersionsStorageAccountServiceTest {

  @Nested
  class ParseVersionLine {
    @Test
    void fullDataPresent() {
      String data =
          """
              {"version": "1.0.0", "timestamp": "2026-04-08T08:43:32Z", "links": 5}""";

      SingleVersion results = AudVersionsStorageAccountService.parseVersionLine(data);

      assertThat(results).isNotNull();
      assertThat(results.version()).isEqualTo("1.0.0");
      assertThat(results.timestamp()).isEqualTo("2026-04-08T08:43:32Z");
      assertThat(results.links()).isEqualTo(5);
    }

    @Test
    void missingLinksIsOk() {
      String data =
          """
              {"version": "1.0.0", "timestamp": "2026-04-08T08:43:32Z"}""";

      SingleVersion results = AudVersionsStorageAccountService.parseVersionLine(data);

      assertThat(results).isNotNull();
      assertThat(results.version()).isEqualTo("1.0.0");
      assertThat(results.timestamp()).isEqualTo("2026-04-08T08:43:32Z");
      assertThat(results.links()).isEqualTo(0);
    }
  }
}

package com.amadeus.aud.libraries.checker;

import java.nio.file.Path;

public record InfraDiffCsvRow(
    String name, String scope, String type, String changeType, String landingZone, String target) {
  public static final Path CSV_FILENAME = Path.of("/tmp/scopes_infra_diff.csv");
}

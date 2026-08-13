package com.amadeus.aud.libraries.models.core.utils;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import java.util.UUID;

public final class UUIDUtils {
  private UUIDUtils() {}

  public static String generateNameBasedUUID(String name) {
    NameBasedGenerator generator = Generators.nameBasedGenerator(StringUtils.rootUUID);
    UUID uuid = generator.generate(name);
    return uuid.toString().replace("-", "");
  }
}

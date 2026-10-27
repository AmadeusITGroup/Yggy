package com.amadeus.aud.libraries.engine.data;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import jakarta.annotation.Nullable;

public interface VersionBuilder {
  Version buildVersion(String key, @Nullable AudDataRepository overrideDataRepository);
}

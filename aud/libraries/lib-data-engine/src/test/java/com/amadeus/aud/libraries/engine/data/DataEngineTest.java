package com.amadeus.aud.libraries.engine.data;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.amadeus.aud.libraries.engine.data.exceptions.InvalidVersionException;
import com.amadeus.aud.libraries.engine.data.services.VersionService;
import org.junit.jupiter.api.Test;

class DataEngineTest {
  @Test
  void rejectsInvalidVersionBeforeCreatingUni() {
    VersionService versionService = mock(VersionService.class);
    DataEngine sut = new DataEngine(versionService);

    assertThatThrownBy(() -> sut.getVersion("NOT_SET"))
        .isInstanceOf(InvalidVersionException.class)
        .hasMessage("Invalid version format: NOT_SET");

    verifyNoInteractions(versionService);
  }
}

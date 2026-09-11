package com.amadeus.aud.libraries.engine.data.services.core.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.BlacklistsRepository;
import org.junit.jupiter.api.Test;

class BlacklistServiceTest {

  private static final class TestableBlacklistService extends BlacklistService {
    BlacklistsRepository exposeGetRepository(Version version) {
      return super.getRepository(version);
    }
  }

  @Test
  void getRepository_registersRepositoryInVersion() {
    Version version = new Version();
    TestableBlacklistService service = new TestableBlacklistService();

    BlacklistsRepository repository = service.exposeGetRepository(version);

    assertThat(repository).isNotNull();
    assertThat(version.getBlacklistsRepository()).isSameAs(repository);
  }

  @Test
  void init_withoutDataRepository_doesNotThrow() {
    Version version = new Version();
    BlacklistService service = new BlacklistService();

    assertDoesNotThrow(() -> service.init(version));
  }
}

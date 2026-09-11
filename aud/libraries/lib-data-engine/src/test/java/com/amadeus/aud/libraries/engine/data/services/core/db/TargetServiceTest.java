package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.named;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ContinentsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ProvidersRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.models.core.db.TargetType;
import com.amadeus.aud.libraries.yaml.models.V1Target;
import com.amadeus.aud.libraries.yaml.models.V1TargetSpec;
import java.util.List;
import org.junit.jupiter.api.Test;

class TargetServiceTest {
  @Test
  void initStopsAtEachMissingPrerequisite() {
    // Arrange
    TargetService sut = configure(new TargetService());
    Version version = new Version();

    // Act and assert
    sut.init(version);
    assertThat(version.getTargetsRepository()).isNull();

    version.setScopesRepository(new ScopesRepository());
    sut.init(version);
    assertThat(version.getTargetsRepository()).isNull();

    version.setLandingZoneFunctionalTypesRepository(new LandingZoneFunctionalTypesRepository());
    sut.init(version);
    assertThat(version.getTargetsRepository()).isNull();

    version.setProvidersRepository(new ProvidersRepository());
    sut.init(version);
    assertThat(version.getTargetsRepository()).isNull();

    version.setTargetTypesRepository(new TargetTypesRepository());
    sut.init(version);
    assertThat(version.getTargetsRepository()).isNull();

    version.setMacroPhasesRepository(new MacroPhasesRepository());
    sut.init(version);
    assertThat(version.getTargetsRepository()).isNull();

    version.setContinentsRepository(new ContinentsRepository());
    version.setAudDataRepository(new AudDataRepository());
    sut.init(version);
    assertThat(version.getTargetsRepository()).isNotNull();
    assertThat(version.getTargetsRepository().getData()).isEmpty();
  }

  @Test
  void initLinksReferencesPopsScopeAndGovernanceObjects() {
    // Arrange
    CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
    Provider provider = named(new Provider(), "provider");
    TargetType targetType = named(new TargetType(), "target-type");
    MacroPhase macroPhase = named(new MacroPhase(), "macro-phase");
    Continent continent = named(new Continent(), "continent");
    ProvidersRepository providersRepository = new ProvidersRepository();
    TargetTypesRepository targetTypesRepository = new TargetTypesRepository();
    MacroPhasesRepository macroPhasesRepository = new MacroPhasesRepository();
    ContinentsRepository continentsRepository = new ContinentsRepository();
    providersRepository.put(provider.id(), provider);
    targetTypesRepository.put(targetType.id(), targetType);
    macroPhasesRepository.put(macroPhase.id(), macroPhase);
    continentsRepository.put(continent.id(), continent);
    fixture.version.setProvidersRepository(providersRepository);
    fixture.version.setTargetTypesRepository(targetTypesRepository);
    fixture.version.setMacroPhasesRepository(macroPhasesRepository);
    fixture.version.setContinentsRepository(continentsRepository);
    fixture.version.setLandingZoneFunctionalTypesRepository(
        new LandingZoneFunctionalTypesRepository());
    fixture.addData(
        V1Target.class,
        List.of(
            target(
                "target",
                CoreDbServiceTestFixture.SCOPE_NAME,
                new V1TargetSpec()
                    .provider(provider.getName())
                    .targettype(targetType.getName())
                    .macroPhase(macroPhase.getName())
                    .continent(continent.getName())
                    .cidrs(List.of("10.0.0.0/24"))
                    .popPrimary("primary-pop")
                    .popSecondary("secondary-pop")),
            target("primary-pop", CoreDbServiceTestFixture.SCOPE_NAME, emptySpec()),
            target("secondary-pop", CoreDbServiceTestFixture.SCOPE_NAME, emptySpec()),
            target("orphan", "missing-scope", emptySpec())));
    TargetService sut = configure(new TargetService());

    // Act
    sut.init(fixture.version);
    sut.initInternals(fixture.version);

    // Assert
    Target target = fixture.version.getTargetsRepository().get("target");
    assertThat(target.getScope()).isSameAs(fixture.scope);
    assertThat(fixture.scope.getTargets()).contains(target);
    assertThat(target.getProvider()).isSameAs(provider);
    assertThat(provider.getTargets()).contains(target);
    assertThat(target.getTargetType()).isSameAs(targetType);
    assertThat(targetType.getTargets()).contains(target);
    assertThat(target.getMacroPhase()).isSameAs(macroPhase);
    assertThat(macroPhase.getTargets()).contains(target);
    assertThat(target.getContinent()).isSameAs(continent);
    assertThat(continent.getTargets()).contains(target);
    assertThat(target.getPopPrimary())
        .isSameAs(fixture.version.getTargetsRepository().get("primary-pop"));
    assertThat(target.getPopSecondary())
        .isSameAs(fixture.version.getTargetsRepository().get("secondary-pop"));
    assertThat(target.getInternalEndpoints(true)).isNotEmpty();
    assertThat(fixture.version.getLandingZoneFunctionalTypesRepository().get("target")).isNotNull();
    assertThat(fixture.version.getTargetsRepository().get("orphan").getScope()).isNull();
  }

  @Test
  void initLeavesMissingAndBlankReferencesUnlinked() {
    // Arrange
    CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
    fixture.version.setProvidersRepository(new ProvidersRepository());
    fixture.version.setTargetTypesRepository(new TargetTypesRepository());
    fixture.version.setMacroPhasesRepository(new MacroPhasesRepository());
    fixture.version.setContinentsRepository(new ContinentsRepository());
    fixture.version.setLandingZoneFunctionalTypesRepository(
        new LandingZoneFunctionalTypesRepository());
    V1TargetSpec missingSpec =
        new V1TargetSpec()
            .provider("missing-provider")
            .targettype("missing-type")
            .macroPhase("missing-phase")
            .continent("missing-continent")
            .popPrimary("missing-primary")
            .popSecondary("missing-secondary");
    fixture.addData(
        V1Target.class,
        List.of(
            target("missing", CoreDbServiceTestFixture.SCOPE_NAME, missingSpec),
            target("empty", CoreDbServiceTestFixture.SCOPE_NAME, emptySpec()),
            target("null", CoreDbServiceTestFixture.SCOPE_NAME, new V1TargetSpec())));
    TargetService sut = configure(new TargetService());

    // Act
    sut.init(fixture.version);

    // Assert
    Target missing = fixture.version.getTargetsRepository().get("missing");
    Target empty = fixture.version.getTargetsRepository().get("empty");
    Target nullReferences = fixture.version.getTargetsRepository().get("null");
    assertThat(missing.getProvider()).isNull();
    assertThat(missing.getTargetType()).isNull();
    assertThat(missing.getMacroPhase()).isNull();
    assertThat(missing.getContinent()).isNull();
    assertThat(missing.getPopPrimary()).isNull();
    assertThat(missing.getPopSecondary()).isNull();
    assertThat(empty.getProvider()).isNull();
    assertThat(nullReferences.getProvider()).isNull();
  }

  @Test
  void initInternalsHandlesMissingAndDataLessRepositories() {
    // Arrange
    TargetService sut = configure(new TargetService());
    Version missingRepositoryVersion = new Version();
    Version missingDataVersion = new Version();
    TargetsRepository targetsRepository = new TargetsRepository();
    targetsRepository.setData(null);
    missingDataVersion.setTargetsRepository(targetsRepository);

    // Act and assert
    assertThatCode(() -> sut.initInternals(missingRepositoryVersion)).doesNotThrowAnyException();
    assertThatCode(() -> sut.initInternals(missingDataVersion)).doesNotThrowAnyException();
  }

  private static V1Target target(String name, String scopeName, V1TargetSpec spec) {
    return new V1Target().metadata(metadata(name, scopeName)).spec(spec);
  }

  private static V1TargetSpec emptySpec() {
    return new V1TargetSpec()
        .provider("")
        .targettype("")
        .macroPhase("")
        .continent("")
        .cidrs(List.of());
  }
}

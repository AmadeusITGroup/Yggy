package com.amadeus.aud.libraries.engine.data.services.core.db;

import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.metadata;
import static com.amadeus.aud.libraries.engine.data.services.core.db.CoreDbServiceTestFixture.named;
import static com.amadeus.aud.libraries.engine.data.services.core.db.base.BaseLogServiceTestSupport.configure;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.amadeus.aud.libraries.engine.data.models.core.internals.Version;
import com.amadeus.aud.libraries.engine.data.repositories.core.AudDataRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneFunctionalTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZoneTypesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.LandingZonesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.MacroPhasesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.RegionsRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.ScopesRepository;
import com.amadeus.aud.libraries.engine.data.repositories.core.db.TargetsRepository;
import com.amadeus.aud.libraries.models.core.db.Continent;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import com.amadeus.aud.libraries.models.core.db.MacroPhase;
import com.amadeus.aud.libraries.models.core.db.Provider;
import com.amadeus.aud.libraries.models.core.db.Region;
import com.amadeus.aud.libraries.models.core.db.Target;
import com.amadeus.aud.libraries.yaml.models.V1LandingZone;
import com.amadeus.aud.libraries.yaml.models.V1LandingZoneSpec;
import com.amadeus.aud.libraries.yaml.models.V1MappingLandingzoneFunctionaltype;
import com.amadeus.aud.libraries.yaml.models.V1MappingLandingzoneFunctionaltypeSpec;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LandingZoneServiceTest {
  @Test
  void initStopsAtEachMissingPrerequisite() {
    // Arrange
    LandingZoneService sut = configure(new LandingZoneService());
    Version version = new Version();

    // Act and assert
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNull();

    version.setTargetsRepository(new TargetsRepository());
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNull();

    version.setLandingZoneFunctionalTypesRepository(new LandingZoneFunctionalTypesRepository());
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNull();

    version.setLandingZoneTypesRepository(new LandingZoneTypesRepository());
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNull();

    version.setMacroPhasesRepository(new MacroPhasesRepository());
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNull();

    version.setRegionsRepository(new RegionsRepository());
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNull();

    version.setScopesRepository(new ScopesRepository());
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNull();

    AudDataRepository dataRepository = new AudDataRepository();
    dataRepository
        .getData()
        .put(
            V1MappingLandingzoneFunctionaltype.class,
            List.<V1MappingLandingzoneFunctionaltype>of());
    version.setAudDataRepository(dataRepository);
    sut.init(version);
    assertThat(version.getLandingZonesRepository()).isNotNull();
    assertThat(version.getLandingZonesRepository().getData()).isEmpty();
  }

  @Test
  void initLinksCloudAndOnPremTargetRelationshipsAndMappings() {
    // Arrange
    CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
    LandingZoneFunctionalTypesRepository functionalTypesRepository =
        new LandingZoneFunctionalTypesRepository();
    LandingZoneTypesRepository landingZoneTypesRepository = new LandingZoneTypesRepository();
    MacroPhasesRepository macroPhasesRepository = new MacroPhasesRepository();
    RegionsRepository regionsRepository = new RegionsRepository();
    TargetsRepository targetsRepository = new TargetsRepository();
    fixture.version.setLandingZoneFunctionalTypesRepository(functionalTypesRepository);
    fixture.version.setLandingZoneTypesRepository(landingZoneTypesRepository);
    fixture.version.setMacroPhasesRepository(macroPhasesRepository);
    fixture.version.setRegionsRepository(regionsRepository);
    fixture.version.setTargetsRepository(targetsRepository);

    MacroPhase macroPhase = named(new MacroPhase(), "Run");
    Continent continent = named(new Continent(), "Europe");
    Provider cloudProvider = named(new Provider(), "Azure");
    Provider onPremProvider = named(new Provider(), "Amadeus");
    Target cloudTarget = target("cloud-target", cloudProvider, continent, macroPhase);
    Target onPremTarget = target("on-prem-target", onPremProvider, continent, macroPhase);
    Region region = named(new Region(), "West Europe");
    LandingZoneType landingZoneType = named(new LandingZoneType(), "Spoke");
    macroPhasesRepository.put(macroPhase.id(), macroPhase);
    regionsRepository.put(region.id(), region);
    landingZoneTypesRepository.put(landingZoneType.id(), landingZoneType);
    targetsRepository.put(cloudTarget.id(), cloudTarget);
    targetsRepository.put(onPremTarget.id(), onPremTarget);
    for (String name :
        List.of(
            "cloud-target",
            "on-prem-target",
            "Run",
            "Europe",
            "Azure",
            "Amadeus",
            "Cloud",
            "Legacy",
            "explicit",
            "mapped")) {
      LandingZoneFunctionalType functionalType = named(new LandingZoneFunctionalType(), name);
      functionalTypesRepository.put(functionalType.id(), functionalType);
    }

    fixture.addData(
        V1LandingZone.class,
        List.of(
            landingZone(
                "cloud-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "cloud-target",
                region.getName(),
                "",
                landingZoneType.getName(),
                List.of("explicit"),
                List.of("10.10.0.0/24")),
            landingZone(
                "on-prem-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "on-prem-target",
                region.getName(),
                macroPhase.getName(),
                landingZoneType.getName(),
                null,
                List.of("10.20.0.0/24"))));
    fixture.addData(
        V1MappingLandingzoneFunctionaltype.class,
        List.of(
            mapping("valid-mapping", CoreDbServiceTestFixture.SCOPE_NAME, "cloud-zone", "mapped"),
            mapping("missing-scope", "unknown-scope", "cloud-zone", "mapped")));
    LandingZoneService sut = configure(new LandingZoneService());

    // Act
    sut.init(fixture.version);
    sut.initInternals(fixture.version);

    // Assert
    LandingZone cloudZone = fixture.version.getLandingZonesRepository().get("cloud-zone");
    LandingZone onPremZone = fixture.version.getLandingZonesRepository().get("on-prem-zone");
    assertThat(cloudZone.getScope()).isSameAs(fixture.scope);
    assertThat(fixture.scope.getLandingZones()).contains(cloudZone, onPremZone);
    assertThat(cloudZone.getTarget()).isSameAs(cloudTarget);
    assertThat(cloudTarget.getLandingZones()).contains(cloudZone);
    assertThat(cloudZone.getRegion()).isSameAs(region);
    assertThat(region.getLandingZones()).contains(cloudZone, onPremZone);
    assertThat(cloudZone.getLandingZoneType()).isSameAs(landingZoneType);
    assertThat(landingZoneType.getLandingZones()).contains(cloudZone, onPremZone);
    assertThat(cloudZone.getMacroPhase()).isSameAs(macroPhase);
    assertThat(onPremZone.getMacroPhase()).isSameAs(macroPhase);
    assertThat(names(cloudZone))
        .containsExactlyInAnyOrder(
            "cloud-target", "Run", "Europe", "Azure", "Cloud", "explicit", "mapped");
    assertThat(names(onPremZone))
        .containsExactlyInAnyOrder("on-prem-target", "Run", "Europe", "Amadeus", "Legacy");
    assertThat(fixture.scope.getLandingZoneFunctionalTypes())
        .contains(functionalTypesRepository.get("mapped"));
    assertThat(cloudZone.getInternalEndpoints(true)).isNotEmpty();
  }

  @Test
  void initLeavesMissingEmptyAndNullReferencesUnlinked() {
    // Arrange
    CoreDbServiceTestFixture fixture = configuredFixture();
    Target blankTarget = named(new Target(), "blank-target");
    blankTarget.setMacroPhase(new MacroPhase());
    blankTarget.setContinent(new Continent());
    blankTarget.setProvider(new Provider());
    fixture.version.getTargetsRepository().put(blankTarget.id(), blankTarget);
    MacroPhase missingMacroPhase = named(new MacroPhase(), "missing-derived-phase");
    Continent missingContinent = named(new Continent(), "missing-derived-continent");
    Target cloudTarget =
        target(
            "cloud-missing-types",
            named(new Provider(), "AWS"),
            missingContinent,
            missingMacroPhase);
    Target onPremTarget =
        target(
            "on-prem-missing-types",
            named(new Provider(), "Amadeus"),
            missingContinent,
            missingMacroPhase);
    fixture.version.getTargetsRepository().put(cloudTarget.id(), cloudTarget);
    fixture.version.getTargetsRepository().put(onPremTarget.id(), onPremTarget);
    fixture.addData(
        V1LandingZone.class,
        List.of(
            landingZone(
                "missing-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "missing-target",
                "missing-region",
                "missing-phase",
                "missing-type",
                List.of("missing-functional-type"),
                List.of()),
            landingZone(
                "empty-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "",
                "",
                null,
                "",
                List.of(),
                List.of()),
            landingZone(
                "blank-target-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "blank-target",
                "",
                "",
                "",
                null,
                List.of()),
            landingZone(
                "cloud-missing-types-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "cloud-missing-types",
                "",
                "",
                null,
                null,
                List.of()),
            landingZone(
                "on-prem-missing-types-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "on-prem-missing-types",
                "",
                "",
                null,
                null,
                List.of()),
            landingZone(
                "null-target-zone",
                CoreDbServiceTestFixture.SCOPE_NAME,
                null,
                "",
                "",
                null,
                null,
                List.of()),
            landingZone("orphan-zone", "unknown-scope", "", "", null, "", null, List.of())));
    fixture.addData(
        V1MappingLandingzoneFunctionaltype.class,
        List.of(
            mapping(
                "missing-zone-mapping",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "unknown-zone",
                "known"),
            mapping(
                "missing-type-mapping",
                CoreDbServiceTestFixture.SCOPE_NAME,
                "empty-zone",
                "unknown-type"),
            mapping("empty-mapping", CoreDbServiceTestFixture.SCOPE_NAME, "", ""),
            mapping("null-mapping", CoreDbServiceTestFixture.SCOPE_NAME, null, null),
            mapping("empty-scope-mapping", "", "empty-zone", "known"),
            mapping("null-scope-mapping", null, "empty-zone", "known")));
    LandingZoneFunctionalType known = named(new LandingZoneFunctionalType(), "known");
    fixture.version.getLandingZoneFunctionalTypesRepository().put(known.id(), known);
    LandingZoneService sut = configure(new LandingZoneService());

    // Act
    sut.init(fixture.version);

    // Assert
    LandingZone missing = fixture.version.getLandingZonesRepository().get("missing-zone");
    LandingZone empty = fixture.version.getLandingZonesRepository().get("empty-zone");
    assertThat(missing.getTarget()).isNull();
    assertThat(missing.getRegion()).isNull();
    assertThat(missing.getMacroPhase()).isNull();
    assertThat(missing.getLandingZoneType()).isNull();
    assertThat(missing.getLandingZoneFunctionalTypes()).isNull();
    assertThat(empty.getTarget()).isNull();
    assertThat(empty.getLandingZoneFunctionalTypes())
        .containsExactly(fixture.version.getLandingZoneFunctionalTypesRepository().get("known"));
    assertThat(fixture.version.getLandingZonesRepository().get("orphan-zone").getScope()).isNull();
  }

  @Test
  void initInternalsHandlesMissingAndDataLessRepositories() {
    // Arrange
    LandingZoneService sut = configure(new LandingZoneService());
    Version missingRepositoryVersion = new Version();
    Version missingDataVersion = new Version();
    LandingZonesRepository landingZonesRepository = new LandingZonesRepository();
    landingZonesRepository.setData(null);
    missingDataVersion.setLandingZonesRepository(landingZonesRepository);

    // Act and assert
    assertThatCode(() -> sut.initInternals(missingRepositoryVersion)).doesNotThrowAnyException();
    assertThatCode(() -> sut.initInternals(missingDataVersion)).doesNotThrowAnyException();
  }

  private static CoreDbServiceTestFixture configuredFixture() {
    CoreDbServiceTestFixture fixture = new CoreDbServiceTestFixture();
    fixture.version.setTargetsRepository(new TargetsRepository());
    fixture.version.setLandingZoneFunctionalTypesRepository(
        new LandingZoneFunctionalTypesRepository());
    fixture.version.setLandingZoneTypesRepository(new LandingZoneTypesRepository());
    fixture.version.setMacroPhasesRepository(new MacroPhasesRepository());
    fixture.version.setRegionsRepository(new RegionsRepository());
    return fixture;
  }

  private static Target target(
      String name, Provider provider, Continent continent, MacroPhase macroPhase) {
    Target target = named(new Target(), name);
    target.setProvider(provider);
    target.setContinent(continent);
    target.setMacroPhase(macroPhase);
    return target;
  }

  private static V1LandingZone landingZone(
      String name,
      String scopeName,
      String target,
      String region,
      String macroPhase,
      String landingZoneType,
      List<String> functionalTypes,
      List<String> cidrs) {
    V1LandingZoneSpec spec =
        new V1LandingZoneSpec()
            .target(target)
            .region(region)
            .macrophase(macroPhase)
            .landingzonetype(landingZoneType)
            .functionaltypes(functionalTypes)
            .cidrs(cidrs);
    return new V1LandingZone().metadata(metadata(name, scopeName)).spec(spec);
  }

  private static V1MappingLandingzoneFunctionaltype mapping(
      String name, String scopeName, String landingZone, String functionalType) {
    V1MappingLandingzoneFunctionaltypeSpec spec =
        new V1MappingLandingzoneFunctionaltypeSpec()
            .landingzone(landingZone)
            .functionaltype(functionalType);
    return new V1MappingLandingzoneFunctionaltype().metadata(metadata(name, scopeName)).spec(spec);
  }

  private static Set<String> names(LandingZone landingZone) {
    return landingZone.getFunctionalTypeJsonRepresentation();
  }
}

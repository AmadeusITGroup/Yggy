package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.LandingZone;
import com.amadeus.aud.libraries.models.core.db.LandingZoneFunctionalType;
import com.amadeus.aud.libraries.models.core.db.LandingZoneType;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LandingZoneTypeAndFunctionalTypeFilterTest {

  // ─── LandingZoneTypeFilter ───────────────────────────────────────────────

  private LandingZoneType lzt(String name) {
    LandingZoneType lzt = mock(LandingZoneType.class);
    when(lzt.getName()).thenReturn(name);
    when(lzt.id()).thenReturn("lzt-" + name.hashCode());

    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn("lz-ams");
    when(lz.getRegionName()).thenReturn(null);
    when(lz.getLandingZoneTypeName()).thenReturn(null);
    when(lz.getMacroPhase()).thenReturn(null);
    when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(null);
    when(lz.getTarget()).thenReturn(null);
    when(lzt.getLandingZones()).thenReturn(Set.of(lz));

    return lzt;
  }

  @Test
  void lztMatchesNull_returnsFalse() {
    assertThat(new LandingZoneTypeFilter().matches(null)).isFalse();
  }

  @Test
  void lztEmptyFilter_matchesAll() {
    assertThat(new LandingZoneTypeFilter().matches(lzt("prod-type"))).isTrue();
  }

  @Test
  void lztFilterByName_match() {
    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("prod");
    f.setName(stringFilter);
    assertThat(f.matches(lzt("prod-type"))).isTrue();
  }

  @Test
  void lztFilterByName_noMatch() {
    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("dev");
    f.setName(stringFilter);
    assertThat(f.matches(lzt("prod-type"))).isFalse();
  }

  @Test
  void lztFilterByLandingZones_match() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ams");
    lzf.setName(stringFilter);

    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    f.setLandingZones(List.of(lzf));
    assertThat(f.matches(lzt("prod-type"))).isTrue();
  }

  @Test
  void lztFilterByLandingZones_noMatch() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("lon");
    lzf.setName(stringFilter);

    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    f.setLandingZones(List.of(lzf));
    assertThat(f.matches(lzt("prod-type"))).isFalse();
  }

  @Test
  void lztFilterByLandingZones_nullOnEntity_noMatch() {
    LandingZoneType lzt = mock(LandingZoneType.class);
    when(lzt.getName()).thenReturn("t");
    when(lzt.getLandingZones()).thenReturn(null);

    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ams");
    lzf.setName(stringFilter);

    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    f.setLandingZones(List.of(lzf));
    assertThat(f.matches(lzt)).isFalse();
  }

  @Test
  void lztFilterByLandingZones_emptyList_matchesAll() {
    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    f.setLandingZones(List.of());
    assertThat(f.matches(lzt("prod-type"))).isTrue();
  }

  @Test
  void lztGetters_returnSetValues() {
    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    f.setName(stringFilter);
    f.setLandingZones(List.of());
    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getLandingZones()).isEmpty();
  }

  // ─── LandingZoneFunctionalTypeFilter ─────────────────────────────────────

  private LandingZoneFunctionalType lzft(String name) {
    LandingZoneFunctionalType lzft = mock(LandingZoneFunctionalType.class);
    when(lzft.getName()).thenReturn(name);
    when(lzft.id()).thenReturn("lzft-" + name.hashCode());
    when(lzft.isGenerated()).thenReturn(false);

    LandingZone lz = mock(LandingZone.class);
    when(lz.getName()).thenReturn("lz-ams");
    when(lz.getRegionName()).thenReturn(null);
    when(lz.getLandingZoneTypeName()).thenReturn(null);
    when(lz.getMacroPhase()).thenReturn(null);
    when(lz.getFunctionalTypeJsonRepresentation()).thenReturn(null);
    when(lz.getTarget()).thenReturn(null);
    when(lzft.getLandingZones()).thenReturn(Set.of(lz));

    return lzft;
  }

  @Test
  void lzftMatchesNull_returnsFalse() {
    assertThat(new LandingZoneFunctionalTypeFilter().matches(null)).isFalse();
  }

  @Test
  void lzftEmptyFilter_matchesAll() {
    assertThat(new LandingZoneFunctionalTypeFilter().matches(lzft("standard"))).isTrue();
  }

  @Test
  void lzftFilterByName_match() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("stand");
    f.setName(stringFilter);
    assertThat(f.matches(lzft("standard"))).isTrue();
  }

  @Test
  void lzftFilterByName_noMatch() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("xyz");
    f.setName(stringFilter);
    assertThat(f.matches(lzft("standard"))).isFalse();
  }

  @Test
  void lzftFilterByIsGenerated_match() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    f.setIsGenerated(false);
    assertThat(f.matches(lzft("standard"))).isTrue();
  }

  @Test
  void lzftFilterByIsGenerated_noMatch() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    f.setIsGenerated(true);
    assertThat(f.matches(lzft("standard"))).isFalse();
  }

  @Test
  void lzftFilterByLandingZones_match() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ams");
    lzf.setName(stringFilter);

    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    f.setLandingZones(List.of(lzf));
    assertThat(f.matches(lzft("standard"))).isTrue();
  }

  @Test
  void lzftFilterByLandingZones_noMatch() {
    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("lon");
    lzf.setName(stringFilter);

    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    f.setLandingZones(List.of(lzf));
    assertThat(f.matches(lzft("standard"))).isFalse();
  }

  @Test
  void lzftFilterByLandingZones_nullOnEntity_noMatch() {
    LandingZoneFunctionalType lzft = mock(LandingZoneFunctionalType.class);
    when(lzft.getName()).thenReturn("t");
    when(lzft.getLandingZones()).thenReturn(null);

    LandingZoneFilter lzf = new LandingZoneFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("ams");
    lzf.setName(stringFilter);

    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    f.setLandingZones(List.of(lzf));
    assertThat(f.matches(lzft)).isFalse();
  }

  @Test
  void lzftFilterByLandingZones_emptyList_matchesAll() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    f.setLandingZones(List.of());
    assertThat(f.matches(lzft("standard"))).isTrue();
  }

  @Test
  void lzftGetters_returnSetValues() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("n");
    f.setName(stringFilter);
    f.setIsGenerated(true);
    f.setLandingZones(List.of());

    assertThat(f.getName()).isEqualTo(stringFilter);
    assertThat(f.getIsGenerated()).isTrue();
    assertThat(f.getLandingZones()).isEmpty();
  }

  @Test
  void landingZoneType_id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void landingZoneFunctionalType_id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("456");

    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }

  @Test
  void lztFilterById_match() {
    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("lzt-");
    f.setId(idFilter);
    assertThat(f.matches(lzt("prod-type"))).isTrue();
  }

  @Test
  void lztFilterById_noMatch() {
    LandingZoneTypeFilter f = new LandingZoneTypeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(lzt("prod-type"))).isFalse();
  }

  @Test
  void lzftFilterById_match() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("lzft-");
    f.setId(idFilter);
    assertThat(f.matches(lzft("standard"))).isTrue();
  }

  @Test
  void lzftFilterById_noMatch() {
    LandingZoneFunctionalTypeFilter f = new LandingZoneFunctionalTypeFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(lzft("standard"))).isFalse();
  }
}

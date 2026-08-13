package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.models.core.db.Continent;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContinentFilterTest {

  private Continent continent(String name) {
    Continent c = mock(Continent.class);
    when(c.id()).thenReturn("continent-" + name.hashCode());
    when(c.getName()).thenReturn(name);
    when(c.getRegionNames()).thenReturn(List.of("north-europe", "west-europe"));
    when(c.getTargetNames()).thenReturn(List.of("target-ams", "target-par"));
    return c;
  }

  @Test
  void matchesNull_returnsFalse() {
    assertThat(new ContinentFilter().matches(null)).isFalse();
  }

  @Test
  void emptyFilter_matchesAll() {
    assertThat(new ContinentFilter().matches(continent("europe"))).isTrue();
  }

  @Test
  void filterByName_match() {
    ContinentFilter f = new ContinentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("euro");
    f.setName(stringFilter);
    assertThat(f.matches(continent("europe"))).isTrue();
  }

  @Test
  void filterByName_noMatch() {
    ContinentFilter f = new ContinentFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("asia");
    f.setName(stringFilter);
    assertThat(f.matches(continent("europe"))).isFalse();
  }

  @Test
  void filterByRegionName_match() {
    ContinentFilter f = new ContinentFilter();
    StringFilter regionNameFilter = new StringFilter();
    regionNameFilter.setLike("north-europe");
    f.setRegionName(regionNameFilter);
    assertThat(f.matches(continent("europe"))).isTrue();
  }

  @Test
  void filterByRegionName_noMatch() {
    ContinentFilter f = new ContinentFilter();
    StringFilter regionNameFilter = new StringFilter();
    regionNameFilter.setLike("east-us");
    f.setRegionName(regionNameFilter);
    assertThat(f.matches(continent("europe"))).isFalse();
  }

  @Test
  void filterByRegionName_nullRegions_noMatch() {
    ContinentFilter f = new ContinentFilter();
    StringFilter regionNameFilter = new StringFilter();
    regionNameFilter.setLike("north-europe");
    f.setRegionName(regionNameFilter);
    Continent c = continent("europe");
    when(c.getRegionNames()).thenReturn(null);
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterByTargetName_match() {
    ContinentFilter f = new ContinentFilter();
    StringFilter targetNameFilter = new StringFilter();
    targetNameFilter.setLike("target-ams");
    f.setTargetName(targetNameFilter);
    assertThat(f.matches(continent("europe"))).isTrue();
  }

  @Test
  void filterByTargetName_noMatch() {
    ContinentFilter f = new ContinentFilter();
    StringFilter targetNameFilter = new StringFilter();
    targetNameFilter.setLike("target-nyc");
    f.setTargetName(targetNameFilter);
    assertThat(f.matches(continent("europe"))).isFalse();
  }

  @Test
  void filterByTargetName_nullTargets_noMatch() {
    ContinentFilter f = new ContinentFilter();
    StringFilter targetNameFilter = new StringFilter();
    targetNameFilter.setLike("target-ams");
    f.setTargetName(targetNameFilter);
    Continent c = continent("europe");
    when(c.getTargetNames()).thenReturn(null);
    assertThat(f.matches(c)).isFalse();
  }

  @Test
  void filterById_match() {
    ContinentFilter f = new ContinentFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("continent-");
    f.setId(idFilter);
    assertThat(f.matches(continent("europe"))).isTrue();
  }

  @Test
  void filterById_noMatch() {
    ContinentFilter f = new ContinentFilter();
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("xyz");
    f.setId(idFilter);
    assertThat(f.matches(continent("europe"))).isFalse();
  }

  @Test
  void id_getterSetter() {
    StringFilter idFilter = new StringFilter();
    idFilter.setLike("123");

    ContinentFilter f = new ContinentFilter();
    f.setId(idFilter);

    assertThat(f.getId()).isEqualTo(idFilter);
  }
}

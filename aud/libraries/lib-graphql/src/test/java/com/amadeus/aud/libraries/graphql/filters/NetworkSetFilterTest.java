package com.amadeus.aud.libraries.graphql.filters;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.graphql.utils.LabelFilter;
import com.amadeus.aud.libraries.graphql.utils.StringFilter;
import com.amadeus.aud.libraries.yaml.models.NetworkSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NetworkSetFilterTest {

  @Test
  void matchesReturnsTrueWhenAllFiltersAreNull() {
    NetworkSetFilter filter = new NetworkSetFilter();
    NetworkSet networkSet = createNetworkSet("test-network");

    assertThat(filter.matches(networkSet)).isTrue();
  }

  @Test
  void matchesReturnsTrueWhenNameMatches() {
    NetworkSetFilter filter = new NetworkSetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("test");
    filter.setName(stringFilter);

    NetworkSet networkSet = createNetworkSet("test-network");

    assertThat(filter.matches(networkSet)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenNameDoesNotMatch() {
    NetworkSetFilter filter = new NetworkSetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("other");
    filter.setName(stringFilter);

    NetworkSet networkSet = createNetworkSet("test-network");

    assertThat(filter.matches(networkSet)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenLabelsMatch() {
    NetworkSetFilter filter = new NetworkSetFilter();
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("zone");
    labelFilter.setValue("production");
    filter.setLabels(List.of(labelFilter));

    NetworkSet networkSet = createNetworkSet("test-network");
    networkSet.setLabels(Map.of("zone", "production", "region", "us-east"));

    assertThat(filter.matches(networkSet)).isTrue();
  }

  @Test
  void matchesReturnsFalseWhenLabelKeyDoesNotExist() {
    NetworkSetFilter filter = new NetworkSetFilter();
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("missing");
    filter.setLabels(List.of(labelFilter));

    NetworkSet networkSet = createNetworkSet("test-network");
    networkSet.setLabels(Map.of("zone", "production"));

    assertThat(filter.matches(networkSet)).isFalse();
  }

  @Test
  void matchesReturnsFalseWhenLabelValueDoesNotMatch() {
    NetworkSetFilter filter = new NetworkSetFilter();
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("zone");
    labelFilter.setValue("development");
    filter.setLabels(List.of(labelFilter));

    NetworkSet networkSet = createNetworkSet("test-network");
    networkSet.setLabels(Map.of("zone", "production"));

    assertThat(filter.matches(networkSet)).isFalse();
  }

  @Test
  void matchesReturnsTrueWhenLabelKeyExistsAndValueIsNull() {
    NetworkSetFilter filter = new NetworkSetFilter();
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("zone");
    filter.setLabels(List.of(labelFilter));

    NetworkSet networkSet = createNetworkSet("test-network");
    networkSet.setLabels(Map.of("zone", "production"));

    assertThat(filter.matches(networkSet)).isTrue();
  }

  @Test
  void matchesReturnsTrueWhenNameAndLabelsMatch() {
    NetworkSetFilter filter = new NetworkSetFilter();
    StringFilter stringFilter = new StringFilter();
    stringFilter.setLike("test");
    filter.setName(stringFilter);
    LabelFilter labelFilter = new LabelFilter();
    labelFilter.setKey("zone");
    labelFilter.setValue("production");
    filter.setLabels(List.of(labelFilter));

    NetworkSet networkSet = createNetworkSet("test-network");
    networkSet.setLabels(Map.of("zone", "production"));

    assertThat(filter.matches(networkSet)).isTrue();
  }

  private NetworkSet createNetworkSet(String name) {
    NetworkSet networkSet = new NetworkSet();
    networkSet.setName(name);
    return networkSet;
  }
}

package com.amadeus.aud.libraries.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ObjectTypeListNamesEnumTest {

  @Test
  void allEnumValuesHaveRepositoryKey() {
    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      assertThat(enumValue.getRepositoryKey())
          .isNotBlank()
          .as("Repository key should not be blank for %s", enumValue);
    }
  }

  @Test
  void allEnumValuesHaveUri() {
    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      assertThat(enumValue.getUri()).isNotBlank().as("URI should not be blank for %s", enumValue);
    }
  }

  @Test
  void allEnumValuesHaveTitle() {
    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      assertThat(enumValue.getTitle())
          .isNotBlank()
          .as("Title should not be blank for %s", enumValue);
    }
  }

  @Test
  void allEnumValuesHaveCategory() {
    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      assertThat(enumValue.getCategory())
          .isNotBlank()
          .as("Category should not be blank for %s", enumValue);
    }
  }

  @Test
  void allEnumValuesHaveInitialColumns() {
    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      assertThat(enumValue.getInitialColumns())
          .isNotEmpty()
          .as("Initial columns should not be empty for %s", enumValue);
    }
  }

  @ParameterizedTest
  @EnumSource(ObjectTypeListNamesEnum.class)
  void byUriReturnsCorrectEnumValue(ObjectTypeListNamesEnum enumValue) {
    Optional<ObjectTypeListNamesEnum> result = ObjectTypeListNamesEnum.byUri(enumValue.getUri());
    assertThat(result).hasValue(enumValue);
  }

  @Test
  void byUriReturnsEmptyForNonExistentUri() {
    Optional<ObjectTypeListNamesEnum> result = ObjectTypeListNamesEnum.byUri("nonexistent-uri-xyz");
    assertThat(result).isEmpty();
  }

  @Test
  void byUriFindsApplicationIds() {
    Optional<ObjectTypeListNamesEnum> result = ObjectTypeListNamesEnum.byUri("applicationids");
    assertThat(result).hasValue(ObjectTypeListNamesEnum.APPLICATIONIDS);
  }

  @Test
  void byUriFindsScopes() {
    Optional<ObjectTypeListNamesEnum> result = ObjectTypeListNamesEnum.byUri("scopes");
    assertThat(result).hasValue(ObjectTypeListNamesEnum.SCOPES);
  }

  @Test
  void byUriFindsComponents() {
    Optional<ObjectTypeListNamesEnum> result = ObjectTypeListNamesEnum.byUri("components");
    assertThat(result).hasValue(ObjectTypeListNamesEnum.COMPONENTS);
  }

  @Test
  void byUriIsCaseSensitive() {
    Optional<ObjectTypeListNamesEnum> result = ObjectTypeListNamesEnum.byUri("APPLICATIONIDS");
    assertThat(result).isEmpty();
  }

  @Test
  void gettersReturnCorrectValuesForApplicationIds() {
    ObjectTypeListNamesEnum enumValue = ObjectTypeListNamesEnum.APPLICATIONIDS;

    assertThat(enumValue.getRepositoryKey()).isEqualTo("applicationid");
    assertThat(enumValue.getUri()).isEqualTo("applicationids");
    assertThat(enumValue.getTitle()).isEqualTo("Application IDs");
    assertThat(enumValue.getInitialColumns()).contains("name");
    assertThat(enumValue.graphQLQueryName()).isEqualTo("ApplicationId");
  }

  @Test
  void gettersReturnCorrectValuesForScopes() {
    ObjectTypeListNamesEnum enumValue = ObjectTypeListNamesEnum.SCOPES;

    assertThat(enumValue.getRepositoryKey()).isEqualTo("scope");
    assertThat(enumValue.getUri()).isEqualTo("scopes");
    assertThat(enumValue.getTitle()).isEqualTo("Scopes");
    assertThat(enumValue.getInitialColumns()).isNotEmpty();
    assertThat(enumValue.graphQLQueryName()).isEqualTo("Scope");
  }

  @Test
  void someEnumsHaveNullGraphQlQueryName() {
    ObjectTypeListNamesEnum enumValue = ObjectTypeListNamesEnum.CERTIFICATIONS;

    assertThat(enumValue.graphQLQueryName()).isNull();
  }

  @Test
  void outboundSnatsHasNullGraphQlQueryName() {
    assertThat(ObjectTypeListNamesEnum.OUTBOUNDSNATS.graphQLQueryName()).isNull();
  }

  @Test
  void providersHasNullGraphQlQueryName() {
    assertThat(ObjectTypeListNamesEnum.PROVIDERS.graphQLQueryName()).isNull();
  }

  @Test
  void allInitialColumnsAreNonEmpty() {
    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      List<String> columns = enumValue.getInitialColumns();
      assertThat(columns).isNotEmpty().as("Columns should not be empty for %s", enumValue);
      for (String column : columns) {
        assertThat(column).isNotBlank().as("Column should not be blank in %s", enumValue);
      }
    }
  }

  @Test
  void urisAreUnique() {
    var uris =
        java.util.Arrays.stream(ObjectTypeListNamesEnum.values())
            .map(ObjectTypeListNamesEnum::getUri)
            .toList();

    assertThat(uris).doesNotHaveDuplicates();
  }

  @Test
  void repositoryKeysAreUnique() {
    var keys =
        java.util.Arrays.stream(ObjectTypeListNamesEnum.values())
            .map(ObjectTypeListNamesEnum::getRepositoryKey)
            .toList();

    assertThat(keys).doesNotHaveDuplicates();
  }

  @Test
  void titlesAreUnique() {
    var titles =
        java.util.Arrays.stream(ObjectTypeListNamesEnum.values())
            .map(ObjectTypeListNamesEnum::getTitle)
            .toList();

    assertThat(titles).doesNotHaveDuplicates();
  }
}

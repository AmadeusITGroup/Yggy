package com.amadeus.aud.libraries.backend.services.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.backend.ObjectTypeListNamesEnum;
import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Execution(ExecutionMode.SAME_THREAD)
class UriFromObjectTypeTest {

  private static final String URI_PREFIX = "/v2/versions/latest/data/";

  private UriFromObjectType uriFromObjectType;

  @BeforeEach
  void setUp() {
    uriFromObjectType = new UriFromObjectType();
  }

  @Test
  void shouldReturnAllEnumValues() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response).isNotNull();
    assertThat(response.data()).hasSize(ObjectTypeListNamesEnum.values().length - 1);
  }

  @Test
  void shouldHaveCorrectUriFormat() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response.data())
        .isNotEmpty()
        .allMatch(
            item -> item.uri().startsWith(URI_PREFIX), "All URIs should start with " + URI_PREFIX);
  }

  @ParameterizedTest
  @MethodSource("provideEnumValues")
  void shouldMapEachEnumValueCorrectly(ObjectTypeListNamesEnum enumValue) {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    String expectedUri = URI_PREFIX + enumValue.getUri();
    assertThat(response.data())
        .anySatisfy(
            (UriItem item) -> {
              if (item.uri().equals(expectedUri)) {
                assertThat(item.title()).isEqualTo(enumValue.getTitle());
                assertThat(item.category()).contains(enumValue.getCategory());
              }
            });
  }

  static Stream<Arguments> provideEnumValues() {
    return Stream.of(ObjectTypeListNamesEnum.values()).map(Arguments::of);
  }

  @Test
  void responseShouldNotBeNull() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response).isNotNull();
    assertThat(response.data()).isNotNull();
  }

  @Test
  void shouldIncludeSpecificEnumValues() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response.data())
        .extracting(UriItem::title)
        .contains(
            "Application IDs",
            "Application Phases",
            "Components",
            "Scopes",
            "Regions",
            "Landing Zones");
  }

  @Test
  void shouldMapEnumValuesCorrectly() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response.data())
        .anySatisfy(
            (UriItem item) -> {
              if ("Application IDs".equals(item.title())) {
                assertThat(item.uri()).isEqualTo(URI_PREFIX + "applicationids");
                assertThat(item.category()).isEqualTo("Application");
              }
            })
        .anySatisfy(
            (UriItem item) -> {
              if ("Scopes".equals(item.title())) {
                assertThat(item.uri()).isEqualTo(URI_PREFIX + "scopes");
                assertThat(item.category()).isEqualTo("Governance");
              }
            })
        .anySatisfy(
            (UriItem item) -> {
              if ("Landing Zones".equals(item.title())) {
                assertThat(item.uri()).isEqualTo(URI_PREFIX + "landingzones");
                assertThat(item.category()).isEqualTo("Network");
              }
            });
  }

  @Test
  void shouldHaveValidCategories() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response.data())
        .allMatch(item -> item.category() != null && !item.category().isBlank())
        .extracting(UriItem::category)
        .containsAnyOf(
            "01. Governance",
            "02. Security Office",
            "03. Network",
            "04. Firewall",
            "05. Application",
            "06. Links");
  }

  @Test
  void shouldNotContainBlankFields() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response.data())
        .isNotEmpty()
        .allMatch(
            item -> item.title() != null && !item.title().isBlank(), "Title should not be blank")
        .isNotEmpty()
        .allMatch(item -> item.uri() != null && !item.uri().isBlank(), "URI should not be blank")
        .isNotEmpty()
        .allMatch(
            item -> item.category() != null && !item.category().isBlank(),
            "Category should not be blank");
  }

  @Test
  void cachesSameResponse() {
    Response<List<UriItem>> response1 = uriFromObjectType.getUris().await().indefinitely();
    Response<List<UriItem>> response2 = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response1).isSameAs(response2);
  }

  @Test
  void allUriItemsAreUnique() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();
    assertThat(response.data()).doesNotHaveDuplicates();
  }

  @Test
  void allUrisAreUnique() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    long uniqueUris = response.data().stream().map(UriItem::uri).distinct().count();
    assertThat(uniqueUris).isEqualTo(response.data().size());
  }

  @Test
  void allTitlesAreUnique() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    long uniqueTitles = response.data().stream().map(UriItem::title).distinct().count();
    assertThat(uniqueTitles).isEqualTo(response.data().size());
  }

  @Test
  void constructorInitializesCache() {
    UriFromObjectType newInstance = new UriFromObjectType();
    Response<List<UriItem>> response = newInstance.getUris().await().indefinitely();

    assertThat(response).isNotNull();
    assertThat(response.data()).isNotEmpty();
  }

  @Test
  void uriConstructionForAllEnums() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    for (ObjectTypeListNamesEnum enumValue : ObjectTypeListNamesEnum.values()) {
      if (enumValue == ObjectTypeListNamesEnum.SCOPERELEASENOTES) {
        continue;
      }
      String expectedUri = URI_PREFIX + enumValue.getUri();
      assertThat(response.data()).extracting(UriItem::uri).contains(expectedUri);
    }
  }

  @Test
  void categoriesShouldHaveNumericPrefix() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    assertThat(response.data())
        .allMatch(
            item -> item.category().matches("^\\d{2}\\. .+"),
            "All categories should have format 'NN. Category Name'");
  }

  @Test
  void shouldHaveOnlySixCategories() {
    Response<List<UriItem>> response = uriFromObjectType.getUris().await().indefinitely();

    long uniqueCategories = response.data().stream().map(UriItem::category).distinct().count();
    assertThat(uniqueCategories).isEqualTo(6);
  }
}

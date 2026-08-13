package com.amadeus.aud.libraries.backend.services.v2;

import static org.assertj.core.api.Assertions.assertThat;

import com.amadeus.aud.libraries.backend.models.v2.Response;
import com.amadeus.aud.libraries.backend.models.v2.UriItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UriFromFileTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void withValidJsonShouldReturnAllItems() throws Exception {
    String validJson =
        """
        {
            "data": [
                {
                    "title": "Scopes",
                    "uri": "/scopes",
                    "category": "Governance"
                },
                {
                    "title": "Approvals",
                    "uri": "/connection-approvals",
                    "category": "Applications"
                }
            ]
        }
        """;
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(validJson, uriFromFile);

    Response<List<UriItem>> response = uriFromFile.getUris().await().indefinitely();

    assertThat(response).isNotNull();
    assertThat(response.data())
        .hasSize(2)
        .satisfies(
            (List<? extends UriItem> uris) -> {
              assertThat(response.data())
                  .first()
                  .isEqualTo(new UriItem("Scopes", "/scopes", "Governance", null));
              assertThat(response.data().get(1))
                  .isEqualTo(
                      new UriItem("Approvals", "/connection-approvals", "Applications", null));
            });
  }

  @Test
  void withSyntacticallyInvalidUriShouldSkipInvalidItem() throws Exception {
    String jsonWithInvalidUri =
        """
        {
            "data": [
                {
                    "title": "Valid",
                    "uri": "/valid-uri",
                    "category": "Test",
                    "graphqlQueryName": "hello"
                },
                {
                    "title": "Invalid",
                    "uri": "://invalid uri with spaces",
                    "category": "Test"
                },
                {
                    "title": "Another Valid",
                    "uri": "/another-valid",
                    "category": "Test"
                }
            ]
        }
        """;
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(jsonWithInvalidUri, uriFromFile);

    Response<List<UriItem>> response = uriFromFile.getUris().await().indefinitely();

    assertThat(response).isNotNull();
    assertThat(response.data())
        .hasSize(2)
        .satisfies(
            (List<? extends UriItem> uris) -> {
              assertThat(response.data())
                  .first()
                  .isEqualTo(new UriItem("Valid", "/valid-uri", "Test", "hello"));
              assertThat(response.data().get(1))
                  .isEqualTo(new UriItem("Another Valid", "/another-valid", "Test", null));
            });
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      textBlock =
          """
      Blank title should skip item        | ''      | /blank-title | Test  | Valid | /valid | Test
      Blank URI should skip item          | Blank URI | '' | Test  | Valid | /valid | Test
      Null URI should skip item           | Null URI| null         | Test  | Valid | /valid | Test
      Blank category should skip item     | No Category | /no-category | ''  | Valid | /valid | Test
      """)
  void testsWithInvalidFieldsShouldSkipItem(
      String testName,
      String invalidTitle,
      String invalidUri,
      String invalidCategory,
      String validTitle,
      String validUri,
      String validCategory)
      throws Exception {
    String uriValue = "null".equals(invalidUri) ? "null" : "\"" + invalidUri + "\"";
    String json =
        String.format(
            """
        {
            "data": [
                {
                    "title": "%s",
                    "uri": %s,
                    "category": "%s"
                },
                {
                    "title": "%s",
                    "uri": "%s",
                    "category": "%s"
                }
            ]
        }
        """,
            invalidTitle, uriValue, invalidCategory, validTitle, validUri, validCategory);
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(json, uriFromFile);

    Response<List<UriItem>> response = uriFromFile.getUris().await().indefinitely();

    assertThat(response.data())
        .as(testName)
        .singleElement()
        .satisfies(
            (UriItem item) -> {
              assertThat(item.title()).isEqualTo(validTitle);
              assertThat(item.uri()).isEqualTo(validUri);
              assertThat(item.category()).isEqualTo(validCategory);
            });
  }

  @Test
  void withEmptyDataShouldReturnEmptyList() throws Exception {
    String json =
        """
        {
            "data": []
        }
        """;
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(json, uriFromFile);

    Response<List<UriItem>> response = uriFromFile.getUris().await().indefinitely();

    assertThat(response).isNotNull();
    assertThat(response.data()).isEmpty();
  }

  @Test
  void withMalformedUriShouldSkipItem() throws Exception {
    String json =
        """
        {
            "data": [
                {
                    "title": "Valid1",
                    "uri": "/valid-1",
                    "category": "Test"
                },
                {
                    "title": "Malformed",
                    "uri": "ht tp://mal formed",
                    "category": "Test"
                },
                {
                    "title": "Valid2",
                    "uri": "/valid-2",
                    "category": "Test"
                }
            ]
        }
        """;
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(json, uriFromFile);

    Response<List<UriItem>> response = uriFromFile.getUris().await().indefinitely();

    assertThat(response.data()).hasSize(2).extracting("title").containsExactly("Valid1", "Valid2");
  }

  @Test
  void cachesSameResponse() throws Exception {
    String json =
        """
        {
            "data": [
                {
                    "title": "Test",
                    "uri": "/test",
                    "category": "Test"
                }
            ]
        }
        """;
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(json, uriFromFile);

    Response<List<UriItem>> response1 = uriFromFile.getUris().await().indefinitely();
    Response<List<UriItem>> response2 = uriFromFile.getUris().await().indefinitely();

    assertThat(response1).isEqualTo(response2);
  }

  @Test
  void withUriContainingQueryAndFragmentShouldIncludeItem() throws Exception {
    String json =
        """
        {
            "data": [
                {
                    "title": "Complex URI",
                    "uri": "/path?param1=value1&param2=value2#section",
                    "category": "Test"
                }
            ]
        }
        """;
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(json, uriFromFile);

    Response<List<UriItem>> response = uriFromFile.getUris().await().indefinitely();

    assertThat(response.data())
        .singleElement()
        .satisfies(
            item -> assertThat(item.uri()).isEqualTo("/path?param1=value1&param2=value2#section"));
  }

  @Test
  void withEncodedCharactersShouldIncludeItem() throws Exception {
    String json =
        """
        {
            "data": [
                {
                    "title": "Encoded",
                    "uri": "/path%20with%20spaces",
                    "category": "Test"
                }
            ]
        }
        """;
    UriFromFile uriFromFile = new UriFromFile(OBJECT_MAPPER);
    loadJsonAndInitialize(json, uriFromFile);

    Response<List<UriItem>> response = uriFromFile.getUris().await().indefinitely();

    assertThat(response.data())
        .singleElement()
        .satisfies(item -> assertThat(item.uri()).isEqualTo("/path%20with%20spaces"));
  }

  private static void loadJsonAndInitialize(String json, UriFromFile uriFromFile) throws Exception {
    InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    uriFromFile.updateResponse(inputStream);
  }
}

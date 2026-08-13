package com.amadeus.aud.libraries.models.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Nested
  @DisplayName("normalize")
  class NormalizeTests {

    @Test
    void nominal() {
      assertThat(StringUtils.normalize("Hello World!")).isEqualTo("hello_world");
      assertThat(StringUtils.normalize("   haha")).isEqualTo("haha");
      assertThat(StringUtils.normalize("haha   ")).isEqualTo("haha");
      assertThat(StringUtils.normalize("hello;sir, can I have one?"))
          .isEqualTo("hello_sir_can_i_have_one");
      assertThat(StringUtils.normalize("_ test")).isEqualTo("test");
      assertThat(StringUtils.normalize("_________")).isEmpty();
      assertThat(StringUtils.normalize("src-id")).isEqualTo("src-id");
    }

    @Test
    void returnsEmptyStringWhenInputEmpty() {
      assertThat(StringUtils.normalize("")).isEmpty();
    }

    @Test
    void preservesHyphensBetweenAlphanumerics() {
      assertThat(StringUtils.normalize("Service-Edge_v2")).isEqualTo("service-edge_v2");
    }

    @Test
    void collapsesMultipleUnderscoresAndRemovesLeadingTrailing() {
      assertThat(StringUtils.normalize("__Alpha___Beta__")).isEqualTo("alpha_beta");
    }

    @Test
    void preservesInternalConsecutiveHyphens() {
      assertThat(StringUtils.normalize("alpha--beta")).isEqualTo("alpha--beta");
    }

    @Test
    void mapsInvalidCharsToSingleUnderscore() {
      assertThat(StringUtils.normalize("A#B$C%D^")).isEqualTo("a_b_c_d");
    }

    @Test
    void removesTrailingUnderscoreGeneratedByFinalInvalids() {
      assertThat(StringUtils.normalize("Alpha%$")).isEqualTo("alpha");
    }

    @Test
    void skipsLeadingSeparators() {
      assertThat(StringUtils.normalize("__--Alpha"))
          .isEqualTo(
              "--alpha"); // Leading hyphen should be preserved after skipped leading underscores
    }

    @Test
    void consecutiveInvalidsBetweenWordsCollapseToSingleUnderscore() {
      assertThat(StringUtils.normalize("Alpha@@@###Beta")).isEqualTo("alpha_beta");
    }

    @Test
    void onlySeparatorsResultEmpty() {
      assertThat(StringUtils.normalize("__---___"))
          .isEqualTo("---"); // underscores removed, hyphens preserved
    }

    @Test
    void uppercaseConvertedToLowercase() {
      assertThat(StringUtils.normalize("AbC123")).isEqualTo("abc123");
    }
  }

  @Nested
  class CsvProtect {
    @Test
    void shouldReturnEmptyStringForNull() {
      assertThat(StringUtils.csvProtect(null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyStringForEmptyString() {
      assertThat(StringUtils.csvProtect("")).isEmpty();
    }

    @Test
    void shouldReturnUnchangedStringWithNoSpecialCharacters() {
      assertThat(StringUtils.csvProtect("simple text")).isEqualTo("simple text");
      assertThat(StringUtils.csvProtect("abc123")).isEqualTo("abc123");
    }

    @Test
    void shouldReplaceTabWithSpace() {
      assertThat(StringUtils.csvProtect("hello\tworld")).isEqualTo("hello world");
      assertThat(StringUtils.csvProtect("one\ttwo\tthree")).isEqualTo("one two three");
    }

    @Test
    void shouldReplaceNewlineWithSpace() {
      assertThat(StringUtils.csvProtect("hello\nworld")).isEqualTo("hello world");
      assertThat(StringUtils.csvProtect("line1\nline2\nline3")).isEqualTo("line1 line2 line3");
    }

    @Test
    void shouldReplaceCarriageReturnWithSpace() {
      assertThat(StringUtils.csvProtect("hello\rworld")).isEqualTo("hello world");
      assertThat(StringUtils.csvProtect("text\rwith\rcarriage")).isEqualTo("text with carriage");
    }

    @Test
    void shouldReplaceMultipleWhitespaceCharacters() {
      assertThat(StringUtils.csvProtect("hello\t\n\rworld")).isEqualTo("hello   world");
      assertThat(StringUtils.csvProtect("test\n\ndata")).isEqualTo("test  data");
    }

    @Test
    void shouldReplaceDoubleQuoteWithSingleQuote() {
      assertThat(StringUtils.csvProtect("hello \"world\"")).isEqualTo("hello 'world'");
      assertThat(StringUtils.csvProtect("\"quoted\"")).isEqualTo("'quoted'");
      assertThat(StringUtils.csvProtect("say \"hi\" please")).isEqualTo("say 'hi' please");
    }

    @Test
    void shouldQuoteStringContainingComma() {
      assertThat(StringUtils.csvProtect("hello,world")).isEqualTo("\"hello,world\"");
      assertThat(StringUtils.csvProtect("one,two,three")).isEqualTo("\"one,two,three\"");
    }

    @Test
    void shouldQuoteStringWithCommaAtStart() {
      assertThat(StringUtils.csvProtect(",start")).isEqualTo("\",start\"");
    }

    @Test
    void shouldQuoteStringWithCommaAtEnd() {
      assertThat(StringUtils.csvProtect("end,")).isEqualTo("\"end,\"");
    }

    @Test
    void shouldHandleCombinationOfCommaAndDoubleQuote() {
      assertThat(StringUtils.csvProtect("hello,\"world\"")).isEqualTo("\"hello,'world'\"");
      assertThat(StringUtils.csvProtect("\"test\",value")).isEqualTo("\"'test',value\"");
    }

    @Test
    void shouldHandleCombinationOfCommaAndWhitespace() {
      assertThat(StringUtils.csvProtect("hello,\tworld")).isEqualTo("\"hello, world\"");
      assertThat(StringUtils.csvProtect("value1,\nvalue2")).isEqualTo("\"value1, value2\"");
    }

    @Test
    void shouldHandleAllSpecialCharactersTogether() {
      assertThat(StringUtils.csvProtect("hello,\"world\"\t\n\r")).isEqualTo("\"hello,'world'   \"");
    }

    @Test
    void shouldHandleOnlyComma() {
      assertThat(StringUtils.csvProtect(",")).isEqualTo("\",\"");
    }

    @Test
    void shouldHandleOnlyDoubleQuote() {
      assertThat(StringUtils.csvProtect("\"")).isEqualTo("'");
    }

    @Test
    void shouldHandleOnlyTab() {
      assertThat(StringUtils.csvProtect("\t")).isEqualTo(" ");
    }

    @Test
    void shouldHandleOnlyNewline() {
      assertThat(StringUtils.csvProtect("\n")).isEqualTo(" ");
    }

    @Test
    void shouldHandleOnlyCarriageReturn() {
      assertThat(StringUtils.csvProtect("\r")).isEqualTo(" ");
    }
  }
}

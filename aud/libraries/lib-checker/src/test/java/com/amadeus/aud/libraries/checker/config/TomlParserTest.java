package com.amadeus.aud.libraries.checker.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TomlParserTest {
  @Test
  void nominal() throws Exception {
    String data =
        """
            # Example of config file used by the checker to control when
            # errors are reported

            [defaults]
            # all ERRORS are reported by default as WARNING before this date
            limit_date = "2027-02-01"
            # users get a `transitional` message 6 months before
            notice_period_months = 6


            # override parameters for specific errors
            [INV012345]
            limit_date = "2026-09-01"
            notice_period_months = 2
            link = "https://example.com/additional-link"
            exceptional_domains_regexp = "^(scope1|scope2)$"
            exceptional_domains_limit_date = "2026-10-01"
            """;

    Configuration config = TomlParser.parse(data.getBytes(StandardCharsets.UTF_8));

    assertThat(config).isNotNull();
    assertThat(config.defaults().limitDate()).isEqualTo(LocalDate.parse("2027-02-01"));
    assertThat(config.defaults().noticePeriod()).hasDays(6 * 30L);
    assertThat(config.specificErrors()).hasSize(1);
    SpecificError errorSection = config.specificErrors().get("INV012345");
    assertThat(errorSection).isNotNull();
    assertThat(errorSection.limitDate()).isEqualTo(LocalDate.parse("2026-09-01"));
    assertThat(errorSection.noticePeriod()).hasDays(2 * 30L);
    assertThat(errorSection.link()).isEqualTo(URI.create("https://example.com/additional-link"));
    assertThat(errorSection.exceptionalDomainsRegexp()).hasToString("^(scope1|scope2)$");
    assertThat(errorSection.exceptionalDomainsLimitDate()).isEqualTo(LocalDate.parse("2026-10-01"));
  }

  @Test
  void emptyFileIsInvalid() {
    String data = "";

    assertThatThrownBy(() -> TomlParser.parse(data.getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasToString(
            "java.lang.IllegalArgumentException: Missing 'defaults' section in TOML configuration");
  }

  @Test
  void ifDefaultsIsNotAMapItShouldBeInvalid() {
    String data =
        """
          defaults = "hello"
          """;

    assertThatThrownBy(() -> TomlParser.parse(data.getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(MismatchedInputException.class)
        .hasMessageStartingWith(
            "Cannot construct instance of `java.util.LinkedHashMap` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('hello')");
  }

  @Test
  void errorSectionNotBeingAMapShouldBeInvalid() {
    String data =
        """
          defaults = "hello"
          """;
    assertThatThrownBy(() -> TomlParser.parse(data.getBytes(StandardCharsets.UTF_8)))
        .isInstanceOf(MismatchedInputException.class)
        .hasMessageStartingWith(
            "Cannot construct instance of `java.util.LinkedHashMap` (although at least one Creator exists): no String-argument constructor/factory method to deserialize from String value ('hello')");
  }

  @Test
  void onlyDefaultSectionIsValid() throws Exception {
    String data =
        """
            # Example of config file used by the checker to control when
            # errors are reported

            [defaults]
            # all ERRORS are reported by default as WARNING before this date
            limit_date = "2027-02-01"
            # users get a `transitional` message 6 months before
            notice_period_months = 6
            """;

    Configuration config = TomlParser.parse(data.getBytes(StandardCharsets.UTF_8));

    assertThat(config).isNotNull();
    assertThat(config.defaults().limitDate()).isEqualTo(LocalDate.parse("2027-02-01"));
    assertThat(config.defaults().noticePeriod()).hasDays(6 * 30L);
    assertThat(config.specificErrors()).isEmpty();
  }

  @Test
  void specificErrorWithOnlyLimitDate() throws Exception {
    String data =
        """
            # Example of config file used by the checker to control when
            # errors are reported

            [defaults]
            # all ERRORS are reported by default as WARNING before this date
            limit_date = "2027-02-01"
            # users get a `transitional` message 6 months before
            notice_period_months = 6

            # override parameters for specific errors
            [INV012345]
            limit_date = "2026-09-01"
            """;

    Configuration config = TomlParser.parse(data.getBytes(StandardCharsets.UTF_8));

    assertThat(config).isNotNull();
    assertThat(config.defaults().limitDate()).isEqualTo(LocalDate.parse("2027-02-01"));
    assertThat(config.defaults().noticePeriod()).hasDays(6 * 30L);
    assertThat(config.specificErrors()).hasSize(1);
    SpecificError errorSection = config.specificErrors().get("INV012345");
    assertThat(errorSection).isNotNull();
    assertThat(errorSection.limitDate()).isEqualTo(LocalDate.parse("2026-09-01"));
    assertThat(errorSection.noticePeriod()).hasDays(6 * 30L);
    assertThat(errorSection.link()).isNull();
    assertThat(errorSection.exceptionalDomainsRegexp()).isNull();
    assertThat(errorSection.exceptionalDomainsLimitDate()).isNull();
  }

  @Test
  void specificErrorWithOnlyLink() throws Exception {
    String data =
        """
                # Example of config file used by the checker to control when
                # errors are reported

                [defaults]
                # all ERRORS are reported by default as WARNING before this date
                limit_date = "2027-02-01"
                # users get a `transitional` message 6 months before
                notice_period_months = 6

                # override parameters for specific errors
                [INV012345]
                link = "https://example.com/additional-link"
                """;

    Configuration config = TomlParser.parse(data.getBytes(StandardCharsets.UTF_8));

    assertThat(config).isNotNull();
    assertThat(config.defaults().limitDate()).isEqualTo(LocalDate.parse("2027-02-01"));
    assertThat(config.defaults().noticePeriod()).hasDays(6 * 30L);
    assertThat(config.specificErrors()).hasSize(1);
    SpecificError errorSection = config.specificErrors().get("INV012345");
    assertThat(errorSection).isNotNull();
    assertThat(errorSection.limitDate()).isNull();
    assertThat(errorSection.noticePeriod()).isNull();
    assertThat(errorSection.link()).isEqualTo(URI.create("https://example.com/additional-link"));
    assertThat(errorSection.exceptionalDomainsRegexp()).isNull();
    assertThat(errorSection.exceptionalDomainsLimitDate()).isNull();
  }
}

package com.amadeus.aud.libraries.checker.models;

import static com.amadeus.aud.libraries.models.parsing.YamlService.LOCATION_LABEL;

import com.amadeus.aud.libraries.checker.config.SpecificError;
import com.amadeus.aud.libraries.yaml.models.V1Base;
import jakarta.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.hc.core5.net.URIBuilder;

public record Message(
    CharSequence code,
    Level level,
    CharSequence message,
    @Nullable Details details,
    @Nullable Transitional transitional,
    Collection<URI> links) {

  public Message(
      CharSequence code,
      Level level,
      CharSequence message,
      @Nullable Details details,
      @Nullable Transitional transitional,
      Collection<URI> links) {
    this.code = code;
    this.level = level;
    this.message = message;
    this.details = details;
    this.transitional = transitional;
    // store a mutable copy of the given links
    // allowing the checker to add more links depending on its configuration
    this.links = new ArrayList<>(links);
  }

  public static Message error(
      CharSequence code,
      CharSequence message,
      @Nullable Details details,
      @Nullable Transitional transitional,
      Collection<URI> links) {
    return new Message(code, Level.ERROR, message, details, transitional, links);
  }

  public static Message error(CharSequence code, CharSequence message, Collection<URI> links) {
    return Message.error(code, message, null, null, links);
  }

  public static Message errorWithDetails(CharSequence code, CharSequence message, V1Base element) {
    Details details =
        new Details(
            element.getMetadata().getScope(), element.getKind(), element.getMetadata().getName());
    List<URI> links = new ArrayList<>();
    Map<String, String> labels = element.getMetadata().getLabels();
    if (labels != null && labels.get(LOCATION_LABEL) != null) {
      try {
        links.add(new URIBuilder().setScheme("file").setPath(labels.get(LOCATION_LABEL)).build());
      } catch (URISyntaxException e) {
        // unreachable
      }
    }
    return Message.error(code, message, details, null, links);
  }

  public static Message warning(
      CharSequence code,
      CharSequence message,
      @Nullable Details details,
      @Nullable Transitional transitional,
      Collection<URI> links) {
    return new Message(code, Level.WARNING, message, details, transitional, links);
  }

  public static Message warning(CharSequence code, CharSequence message, Collection<URI> links) {
    return Message.warning(code, message, null, null, links);
  }

  public static Message warningWithDetails(
      CharSequence code, CharSequence message, V1Base element) {
    Details details =
        new Details(
            element.getMetadata().getScope(), element.getKind(), element.getMetadata().getName());
    List<URI> links = new ArrayList<>();
    Map<String, String> labels = element.getMetadata().getLabels();
    if (labels != null && labels.get(LOCATION_LABEL) != null) {
      try {
        links.add(new URIBuilder().setScheme("file").setPath(labels.get(LOCATION_LABEL)).build());
      } catch (URISyntaxException e) {
        // unreachable
      }
    }
    return Message.warning(code, message, details, null, links);
  }

  public static Message info(
      CharSequence code,
      CharSequence message,
      @Nullable Details details,
      @Nullable Transitional transitional,
      Collection<URI> links) {
    return new Message(code, Level.INFO, message, details, transitional, links);
  }

  public static Message info(CharSequence code, CharSequence message, Collection<URI> links) {
    return Message.info(code, message, null, null, links);
  }

  public static Message infoWithDetails(CharSequence code, CharSequence message, V1Base element) {
    Details details =
        new Details(
            element.getMetadata().getScope(), element.getKind(), element.getMetadata().getName());
    List<URI> links = new ArrayList<>();
    Map<String, String> labels = element.getMetadata().getLabels();
    if (labels != null && labels.get(LOCATION_LABEL) != null) {
      try {
        links.add(new URIBuilder().setScheme("file").setPath(labels.get(LOCATION_LABEL)).build());
      } catch (URISyntaxException e) {
        // unreachable
      }
    }
    return Message.info(code, message, details, null, links);
  }

  public static Message context(CharSequence message) {
    return new Message("", Level.CONTEXT, message, null, null, List.of());
  }

  public Message withLink(URI link) {
    links.add(link);
    return new Message(code, level, message, details, transitional, links);
  }

  public Message withErrorConfig(@Nullable SpecificError errorConfig) {
    if (errorConfig == null) {
      return this;
    }
    if (errorConfig.link() != null) {
      this.links().add(errorConfig.link());
    }
    if (!errorConfig.isOnlyLink() && this.level() == Level.ERROR) {
      Transitional transitional =
          errorConfig.transitional(
              LocalDate.now(), this.details() != null ? this.details().objectDomain() : null);
      if (transitional == null) {
        return this;
      }
      return Message.warning(
          this.code(), this.message(), this.details(), transitional, this.links());
    }
    return this;
  }
}

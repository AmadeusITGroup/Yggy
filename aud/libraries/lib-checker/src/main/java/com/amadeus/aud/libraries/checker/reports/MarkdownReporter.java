package com.amadeus.aud.libraries.checker.reports;

import com.amadeus.aud.libraries.checker.models.Level;
import com.amadeus.aud.libraries.checker.models.Message;
import com.amadeus.aud.libraries.checker.models.Transitional;
import com.amadeus.aud.libraries.common.SBUtils;
import jakarta.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class MarkdownReporter implements Reporter {
  private static final String ERROR = "ERROR";
  private static final String FUTURE_ERROR = "Future ERROR";
  private static final String WARNING = "WARNING";
  private static final String INFO = "INFO";

  private final LocalDate now;
  private final File errorsFd;
  private final File futureErrorsFd;
  private final File warningsFd;
  private final File infoFd;
  private final OutputStream reportOutputStream;
  private final OutputStream errorsOutputStream;
  private final OutputStream futureErrorsOutputStream;
  private final OutputStream warningsOutputStream;
  private final OutputStream infosOutputStream;
  private final Map<String, Integer> statistics;

  @SuppressWarnings("java:S5443")
  public MarkdownReporter(LocalDate now, OutputStream reportOutputStream) {
    this.now = now;
    try {
      this.errorsFd = Files.createTempFile(".aud-markdown-report-errors", ".md").toFile();
      this.futureErrorsFd =
          Files.createTempFile(".aud-markdown-report-future-errors", ".md").toFile();
      this.warningsFd = Files.createTempFile(".aud-markdown-report-warnings", ".md").toFile();
      this.infoFd = Files.createTempFile(".aud-markdown-report-info", ".md").toFile();
      this.errorsOutputStream = new BufferedOutputStream(new FileOutputStream(this.errorsFd));
      this.futureErrorsOutputStream =
          new BufferedOutputStream(new FileOutputStream(this.futureErrorsFd));
      this.warningsOutputStream = new BufferedOutputStream(new FileOutputStream(this.warningsFd));
      this.infosOutputStream = new BufferedOutputStream(new FileOutputStream(this.infoFd));
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create temporary files for markdown report", e);
    }
    this.reportOutputStream = reportOutputStream;
    this.statistics = new HashMap<>();
    this.statistics.put(ERROR, 0);
    this.statistics.put(FUTURE_ERROR, 0);
    this.statistics.put(WARNING, 0);
    this.statistics.put(INFO, 0);
  }

  @Override
  public void addMessages(Collection<Message> messages) throws IOException {
    if (messages.isEmpty()) {
      return;
    }
    StringBuilder buffer = new StringBuilder(200);
    for (Message message : messages) {
      this.format(buffer, message);
      CharBuffer charBuffer = CharBuffer.wrap(buffer);
      var data = StandardCharsets.UTF_8.encode(charBuffer);
      SBUtils.reset(buffer);
      switch (message.level()) {
        case ERROR -> {
          statistics.put(ERROR, statistics.getOrDefault(ERROR, 0) + 1);
          errorsOutputStream.write(data.array(), data.position(), data.remaining());
        }
        case WARNING -> {
          if (message.transitional() != null) {
            statistics.put(FUTURE_ERROR, statistics.getOrDefault(FUTURE_ERROR, 0) + 1);
            futureErrorsOutputStream.write(data.array(), data.position(), data.remaining());
          } else {
            statistics.put(WARNING, statistics.getOrDefault(WARNING, 0) + 1);
            warningsOutputStream.write(data.array(), data.position(), data.remaining());
          }
        }
        case INFO -> {
          statistics.put(INFO, statistics.getOrDefault(INFO, 0) + 1);
          infosOutputStream.write(data.array(), data.position(), data.remaining());
        }
        case CONTEXT -> infosOutputStream.write(data.array(), data.position(), data.remaining());
      }
    }
    errorsOutputStream.flush();
    futureErrorsOutputStream.flush();
    warningsOutputStream.flush();
    infosOutputStream.flush();
  }

  @Override
  public void close() throws IOException {
    this.errorsOutputStream.close();
    this.futureErrorsOutputStream.close();
    this.warningsOutputStream.close();
    this.infosOutputStream.close();

    StringBuilder sb = new StringBuilder(200);
    sb.append("# AUD Report\n\n");
    appendStatistics(sb);
    BufferedOutputStream bufferedReportOutputStream = new BufferedOutputStream(reportOutputStream);
    bufferedReportOutputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
    Files.copy(this.errorsFd.toPath(), bufferedReportOutputStream);
    Files.copy(this.futureErrorsFd.toPath(), bufferedReportOutputStream);
    Files.copy(this.warningsFd.toPath(), bufferedReportOutputStream);
    Files.copy(this.infoFd.toPath(), bufferedReportOutputStream);
    bufferedReportOutputStream.flush();
    reportOutputStream.flush();

    reportOutputStream.close();
    Files.delete(this.errorsFd.toPath());
    Files.delete(this.futureErrorsFd.toPath());
    Files.delete(this.warningsFd.toPath());
    Files.delete(this.infoFd.toPath());
  }

  private void appendStatistics(StringBuilder sb) {
    sb.append("## Statistics\n\n")
        .append("[comment]: <> (- ERROR: ")
        .append(this.statistics.get(ERROR))
        .append(")\n")
        .append("[comment]: <> (- Future ERROR: ")
        .append(this.statistics.get(FUTURE_ERROR))
        .append(")\n")
        .append("[comment]: <> (- WARNING: ")
        .append(this.statistics.get(WARNING))
        .append(")\n")
        .append("[comment]: <> (- INFO: ")
        .append(this.statistics.get(INFO))
        .append(")\n")
        .append("\n")
        .append("| ERROR | Future ERROR | WARNING | INFO |\n")
        .append("|-------|--------------|---------|------|\n")
        .append("|")
        .append(String.format("%7d", this.statistics.get(ERROR)))
        .append("|")
        .append(String.format("%14d", this.statistics.get(FUTURE_ERROR)))
        .append("|")
        .append(String.format("%9d", this.statistics.get(WARNING)))
        .append("|")
        .append(String.format("%6d", this.statistics.get(INFO)))
        .append("|\n\n");
  }

  private void format(StringBuilder buffer, Message message) {
    buffer
        .append("## ")
        .append(formatLevel(message.level()))
        .append(message.code().isEmpty() ? "" : " ")
        .append(message.code())
        .append(transition(message.transitional()))
        .append("\n\n```text\n");

    details(buffer, message);

    buffer.append(message.message()).append("\n```\n\n");

    appendLinks(buffer, message.links());
  }

  private static CharSequence formatLevel(Level level) {
    return switch (level) {
      case ERROR -> "‼️ ERROR";
      case WARNING -> "⚠️ WARNING";
      case INFO -> "🪧 INFO";
      case CONTEXT -> "CONTEXT";
    };
  }

  private CharSequence transition(@Nullable Transitional transitional) {
    if (transitional != null
        && (transitional.limitDate().isAfter(now) || transitional.limitDate().isEqual(now))) {
      return " **‼️** BLOCKING after " + transitional.limitDate();
    }
    return "";
  }

  private static void details(StringBuilder buffer, Message message) {
    if (message.details() != null) {
      buffer
          .append("In ")
          .append(message.details().objectDomain())
          .append(", on ")
          .append(message.details().objectType())
          .append("/")
          .append(message.details().objectName())
          .append(":\n");
    }
  }

  private static void appendLinks(StringBuilder sb, Collection<URI> links) {
    if (!links.isEmpty()) {
      sb.append("### Links\n\n");
      links.forEach((URI link) -> sb.append("- ").append(link).append("\n"));
      sb.append("\n");
    }
  }
}

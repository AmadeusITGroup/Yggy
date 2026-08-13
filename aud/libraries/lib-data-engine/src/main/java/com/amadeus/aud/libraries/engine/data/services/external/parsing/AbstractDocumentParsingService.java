package com.amadeus.aud.libraries.engine.data.services.external.parsing;

import com.amadeus.aud.libraries.engine.data.utils.LogExecutionTime;
import com.amadeus.aud.libraries.yaml.models.ModelElement;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract base class for document parsing services. Provides common functionality for JSON and
 * YAML parsing with optimizations.
 */
public abstract class AbstractDocumentParsingService implements DocumentParser<ModelElement> {

  private static final long MEMORY_MAPPED_THRESHOLD = 100 * 1024 * 1024L;

  protected final ObjectMapper objectMapper;
  protected final JsonFactory parserFactory;

  protected AbstractDocumentParsingService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.parserFactory = objectMapper.getFactory();
  }

  @Override
  public ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> importFromString(
      String content) throws IOException {
    if (content == null || content.trim().isEmpty()) {
      return new ConcurrentHashMap<>();
    }

    return parseContent(content);
  }

  @Override
  public ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> importFromFile(File file)
      throws IOException {
    if (file == null || !file.exists() || !file.isFile()) {
      throw new IOException("Invalid file: " + (file != null ? file.getAbsolutePath() : "null"));
    }

    final ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> data =
        new ConcurrentHashMap<>();

    importFromFile(file, data);

    return data;
  }

  @Override
  public void importFromFile(
      @Nullable File file, Map<Class<? extends ModelElement>, List<ModelElement>> data)
      throws IOException {
    if (file == null || !file.exists() || !file.isFile()) {
      throw new IOException("Invalid file: " + (file != null ? file.getAbsolutePath() : "null"));
    }

    // Use memory-mapped files for large files
    if (file.length() > MEMORY_MAPPED_THRESHOLD) {
      parseFromMemoryMappedFile(file, data);
    } else {
      parseFromRegularFile(file, data);
    }
  }

  @Override
  @LogExecutionTime
  public void dump(String content, Path path) throws IOException {
    if (content == null) {
      throw new IllegalArgumentException("Content cannot be null");
    }

    Files.createDirectories(path.getParent());

    try (BufferedWriter writer =
        Files.newBufferedWriter(
            path,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      writer.write(content);
    }
  }

  /**
   * Parse content from a string using the appropriate parser.
   *
   * @param content The content to parse
   * @return The parsed data
   * @throws IOException If parsing fails
   */
  protected abstract ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> parseContent(
      String content) throws IOException;

  /**
   * Parse content from a regular file.
   *
   * @param file The file to parse
   * @param data The data structure to populate
   * @throws IOException If parsing fails
   */
  protected abstract void parseFromRegularFile(
      File file, Map<Class<? extends ModelElement>, List<ModelElement>> data) throws IOException;

  /**
   * Parse content from a memory-mapped file for better performance with large files.
   *
   * @param file The file to parse
   * @param data The data structure to populate
   * @throws IOException If parsing fails
   */
  private void parseFromMemoryMappedFile(
      File file, Map<Class<? extends ModelElement>, List<ModelElement>> data) throws IOException {
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        FileChannel fileChannel = randomAccessFile.getChannel()) {

      long fileSize = fileChannel.size();

      MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      String content = new String(bytes, StandardCharsets.UTF_8);

      ConcurrentMap<Class<? extends ModelElement>, List<ModelElement>> parsed =
          parseContent(content);

      parsed.forEach(
          (key, value) -> data.computeIfAbsent(key, k -> new ArrayList<>()).addAll(value));
    }
  }

  /**
   * Helper method to properly add elements to the data structure.
   *
   * @param data The data structure
   * @param element The element to add
   */
  protected void addElementToData(
      Map<Class<? extends ModelElement>, List<ModelElement>> data, ModelElement element) {
    data.computeIfAbsent(element.getClass(), k -> new ArrayList<>()).add(element);
  }

  /**
   * Create a parser from the factory with proper resource management.
   *
   * @param content The content to parse
   * @return A configured parser
   * @throws IOException If parser creation fails
   */
  protected JsonParser createParser(String content) throws IOException {
    return parserFactory.createParser(content);
  }

  /**
   * Create a parser from the factory for files with proper resource management.
   *
   * @param file The file to parse
   * @return A configured parser
   * @throws IOException If parser creation fails
   */
  protected JsonParser createParser(File file) throws IOException {
    return parserFactory.createParser(file);
  }
}

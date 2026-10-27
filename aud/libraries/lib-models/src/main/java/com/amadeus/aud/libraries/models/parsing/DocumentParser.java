package com.amadeus.aud.libraries.models.parsing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Common interface for document parsing services. Provides methods for importing data from strings
 * and files.
 *
 * @param <T> The base type of elements to parse
 */
public interface DocumentParser<T> {

  /**
   * Import content from a string into the appropriate data structure.
   *
   * @param content The content to import
   * @return The imported data grouped by class type
   * @throws IOException If an error occurs during import
   */
  ConcurrentMap<Class<? extends T>, List<T>> importFromString(String content) throws IOException;

  /**
   * Import content from a file into the provided data structure.
   *
   * @param file The file to import from
   * @param data The data structure to populate
   * @throws IOException If an error occurs during import
   */
  void importFromFile(File file, Map<Class<? extends T>, List<T>> data) throws IOException;

  /**
   * Import content from a file path into the appropriate data structure.
   *
   * @param filePath The path to the file to import from
   * @return The imported data grouped by class type
   * @throws IOException If an error occurs during import
   */
  default ConcurrentMap<Class<? extends T>, List<T>> importFromFile(Path filePath)
      throws IOException {
    return importFromFile(filePath.toFile());
  }

  /**
   * Import content from a file into the appropriate data structure.
   *
   * @param file The file to import from
   * @return The imported data grouped by class type
   * @throws IOException If an error occurs during import
   */
  ConcurrentMap<Class<? extends T>, List<T>> importFromFile(File file) throws IOException;

  /**
   * Dump content to a file.
   *
   * @param content The content to write
   * @param path The path to write to
   * @throws IOException If an error occurs during writing
   */
  void dump(String content, Path path) throws IOException;
}

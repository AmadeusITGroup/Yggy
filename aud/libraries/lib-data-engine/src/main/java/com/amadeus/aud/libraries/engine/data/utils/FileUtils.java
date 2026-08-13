package com.amadeus.aud.libraries.engine.data.utils;

import com.azure.core.util.BinaryData;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class FileUtils {

  /**
   * Decode gzipped binary data.
   *
   * @param binaryData The gzipped binary data
   * @return The decoded string
   * @throws IOException If an error occurs during decoding
   */
  public static String decodeGzipped(BinaryData binaryData) throws IOException {
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(binaryData.toStream());
        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8), 16 * 1024)) {
      StringBuilder sb = new StringBuilder();
      char[] buffer = new char[16 * 1024];
      int len;
      while ((len = reader.read(buffer)) > 0) {
        sb.append(buffer, 0, len);
      }
      return sb.toString();
    }
  }

  public static BinaryData encodeGzip(String content) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
      gzipOut.write(content.getBytes(StandardCharsets.UTF_8));
    }
    return BinaryData.fromBytes(baos.toByteArray());
  }

  private FileUtils() {}
}

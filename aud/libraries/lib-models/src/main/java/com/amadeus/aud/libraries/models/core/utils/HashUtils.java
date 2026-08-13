package com.amadeus.aud.libraries.models.core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {

  private HashUtils() {}

  public static String calculateMD5Hash(String input) throws NoSuchAlgorithmException {
    MessageDigest md5Digest = MessageDigest.getInstance("MD5");
    byte[] hashBytes = md5Digest.digest(input.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hashBytes);
  }

  public static String calculateSHA256Hash(String input) throws NoSuchAlgorithmException {
    MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = sha256Digest.digest(input.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hashBytes);
  }

  public static String calculateCombinedMD5Hash(String[] responses)
      throws NoSuchAlgorithmException {
    MessageDigest md5Digest = MessageDigest.getInstance("MD5");

    for (String response : responses) {
      if (response != null) {
        byte[] bytesOfResponse = response.getBytes(StandardCharsets.UTF_8);
        md5Digest.update(bytesOfResponse);
      }
    }

    return bytesToHex(md5Digest.digest());
  }

  public static String calculateCombinedSHA256Hash(String[] responses)
      throws NoSuchAlgorithmException {
    MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");

    for (String response : responses) {
      if (response != null) {
        byte[] bytesOfResponse = response.getBytes(StandardCharsets.UTF_8);
        sha256Digest.update(bytesOfResponse);
      }
    }

    return bytesToHex(sha256Digest.digest());
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder(2 * bytes.length);
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}

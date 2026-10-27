package com.amadeus.aud.libraries.engine.data.exceptions;

public final class InvalidVersionException extends RuntimeException {
  public InvalidVersionException(String versionId) {
    super("Invalid version format: " + versionId);
  }
}

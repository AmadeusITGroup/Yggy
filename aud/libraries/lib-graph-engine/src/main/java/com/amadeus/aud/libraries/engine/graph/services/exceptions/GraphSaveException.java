package com.amadeus.aud.libraries.engine.graph.services.exceptions;

/**
 * Exception thrown when graph saving operations fail. This exception indicates that the graph could
 * not be saved to the database and that any transaction should be rolled back.
 */
public class GraphSaveException extends RuntimeException {

  public GraphSaveException(String message) {
    super(message);
  }

  public GraphSaveException(String message, Throwable cause) {
    super(message, cause);
  }

  public GraphSaveException(Throwable cause) {
    super(cause);
  }
}

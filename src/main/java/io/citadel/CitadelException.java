package io.citadel;

public final class CitadelException extends RuntimeException {
  public CitadelException() {
    super();
  }

  public CitadelException(final String message) {
    super(message);
  }

  public CitadelException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public CitadelException(final Throwable cause) {
    super(cause);
  }

  private CitadelException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

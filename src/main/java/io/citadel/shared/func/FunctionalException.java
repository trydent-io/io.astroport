package io.citadel.shared.func;

public final class FunctionalException extends RuntimeException {
  public FunctionalException() {
    super();
  }

  public FunctionalException(final String message) {
    super(message);
  }

  public FunctionalException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public FunctionalException(final Throwable cause) {
    super(cause);
  }

  private FunctionalException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

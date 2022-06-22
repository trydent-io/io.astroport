package io.citadel.kernel.func;

public final class LambdaException extends RuntimeException {
  public LambdaException() {
    super();
  }

  public LambdaException(final String message) {
    super(message);
  }

  public LambdaException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public LambdaException(final Throwable cause) {
    super(cause);
  }

  private LambdaException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

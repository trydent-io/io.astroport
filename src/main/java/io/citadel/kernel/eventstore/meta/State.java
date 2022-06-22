package io.citadel.kernel.eventstore.meta;

import io.citadel.kernel.func.LambdaException;
import io.citadel.kernel.func.ThrowableFunction;

public record State(String value) {
  static State of(String value) {
    return new State(value);
  }

  public <E extends Enum<E>> E as(ThrowableFunction<? super String, ? extends E> converter) {
    try {
      return value != null ? converter.apply(value) : null;
    } catch (LambdaException e) {
      return null;
    }
  }
}

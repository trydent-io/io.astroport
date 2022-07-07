package io.citadel.kernel.eventstore.metadata;

import io.citadel.kernel.func.LambdaException;
import io.citadel.kernel.func.ThrowableFunction;

public record State(String value) {
  static State of(String value) {
    return new State(value);
  }

  public static <E extends Enum<E>> State from(E enumeration) { return new State(enumeration.name());}

  public <E extends Enum<E>> E as(ThrowableFunction<? super String, ? extends E> converter) {
    try {
      return value != null ? converter.apply(value) : null;
    } catch (LambdaException e) {
      return null;
    }
  }
}

package io.citadel.kernel.func;

import java.util.function.UnaryOperator;

public interface ThrowableUnaryOperator<T> extends ThrowableFunction<T, T>, UnaryOperator<T> {
  static <T> ThrowableUnaryOperator<T> identity() {
    return t -> t;
  }
}

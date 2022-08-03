package io.citadel.kernel.func;

import java.util.function.UnaryOperator;

public interface TryUnaryOperator<T> extends TryFunction<T, T>, UnaryOperator<T> {
  static <T> TryUnaryOperator<T> identity() {
    return t -> t;
  }
}

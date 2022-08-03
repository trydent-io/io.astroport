package io.citadel.kernel.func;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TryFunction<T, R> extends Function<T, R> {
  R tryApply(T t) throws Throwable;

  @Override
  default R apply(T t) {
    try {
      return tryApply(t);
    } catch (Throwable e) {
      throw new LambdaException("Can't apply function", e);
    }
  }

  default <V> TryFunction<T, V> then(TryFunction<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t) -> after.apply(apply(t));
  }

  static <T> TryFunction<T, T> identity() {
    return t -> t;
  }
}

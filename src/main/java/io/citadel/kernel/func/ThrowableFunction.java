package io.citadel.kernel.func;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThrowableFunction<T, R> extends Function<T, R> {
  R tryApply(T t) throws Throwable;

  @Override
  default R apply(T t) {
    try {
      return tryApply(t);
    } catch (Throwable e) {
      throw new LambdaException("Can't apply function", e);
    }
  }

  default <V> ThrowableFunction<T, V> then(ThrowableFunction<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t) -> after.apply(apply(t));
  }

  static <T> ThrowableFunction<T, T> identity() {
    return t -> t;
  }
}

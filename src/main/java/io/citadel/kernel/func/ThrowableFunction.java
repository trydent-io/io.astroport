package io.citadel.kernel.func;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowableFunction<T, R> extends Function<T, R> {
  R tryApply(T t) throws Throwable;

  @Override
  default R apply(T t) {
    try {
      return tryApply(t);
    } catch (Throwable e) {
      throw new FunctionalException("Can't apply function", e);
    }
  }
}

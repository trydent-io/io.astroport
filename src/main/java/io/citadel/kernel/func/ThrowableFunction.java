package io.citadel.kernel.func;

import io.alpenglow.kernel.Maybe;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowableFunction<T, R> extends Function<T, Maybe<R>> {
  R tryApply(T t) throws Throwable;

  @Override
  default Maybe<R> apply(T t) {
    try {
      return Maybe.value(tryApply(t));
    } catch (Throwable e) {
      return Maybe.error(e);
    }
  }
}

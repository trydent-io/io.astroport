package io.citadel.shared.func;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowableFunction<T, R> extends Function<T, Maybe<R>> {
  R tryApply(T t) throws Throwable;

  @Override
  default Maybe<R> apply(T t) {
    try {
      return Maybe.of(tryApply(t));
    } catch (Throwable e) {
      return Maybe.failure(e);
    }
  }
}

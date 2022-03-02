package io.citadel.shared.func;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowableBiFunction<A, B, R> extends BiFunction<A, B, Maybe<R>> {
  R tryApply(A a, B b) throws Throwable;

  @Override
  default Maybe<R> apply(A a, B b) {
    try {
      return Maybe.of(tryApply(a, b));
    } catch (Throwable e) {
      return Maybe.failure(e);
    }
  }
}

package io.citadel.kernel.func;

import io.alpenglow.kernel.Maybe;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowableBiFunction<A, B, R> extends BiFunction<A, B, Maybe<R>> {
  R tryApply(A a, B b) throws Throwable;

  @Override
  default Maybe<R> apply(A a, B b) {
    try {
      return Maybe.value(tryApply(a, b));
    } catch (Throwable e) {
      return Maybe.error(e);
    }
  }
}

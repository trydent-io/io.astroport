package io.citadel.kernel.func;

import io.alpenglow.kernel.Maybe;

@FunctionalInterface
public interface ThrowableTriFunction<A, B, C, R> {
  R tryApply(A a, B b, C c) throws Throwable;

  default Maybe<R> apply(A a, B b, C c) {
    try {
      return Maybe.value(tryApply(a, b, c));
    } catch (Throwable e) {
      return Maybe.error(e);
    }
  }
}

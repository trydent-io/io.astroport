package io.citadel.shared.func;

@FunctionalInterface
public interface ThrowableTriFunction<A, B, C, R> {
  R tryApply(A a, B b, C c) throws Throwable;

  default Maybe<R> apply(A a, B b, C c) {
    try {
      return Maybe.of(tryApply(a, b, c));
    } catch (Throwable e) {
      return Maybe.failure(e);
    }
  }
}

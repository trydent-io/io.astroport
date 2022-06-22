package io.citadel.kernel.func;

@FunctionalInterface
public interface ThrowableTriFunction<A, B, C, R> {
  R tryApply(A a, B b, C c) throws Throwable;

  default R apply(A a, B b, C c) {
    try {
      return tryApply(a, b, c);
    } catch (Throwable e) {
      throw new LambdaException("Can't apply function", e);
    }
  }
}

package io.citadel.kernel.func;

@FunctionalInterface
public interface ThrowableQuadFunction<A, B, C, D, R> {
  R tryApply(A a, B b, C c, D d) throws Throwable;

  default R apply(A a, B b, C c, D d) {
    try {
      return tryApply(a, b, c, d);
    } catch (Throwable e) {
      throw new LambdaException("Can't apply function", e);
    }
  }
}

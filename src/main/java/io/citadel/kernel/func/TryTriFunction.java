package io.citadel.kernel.func;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@FunctionalInterface
public interface TryTriFunction<A, B, C, R> {
  R tryApply(A a, B b, C c) throws Throwable;

  default R apply(A a, B b, C c) {
    try {
      return tryApply(a, b, c);
    } catch (Throwable e) {
      throw new LambdaException("Can't apply function", e);
    }
  }
}

package io.citadel.kernel.func;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

@FunctionalInterface
public interface ThrowableBiFunction<A, B, R> extends BiFunction<A, B, R> {
  R tryApply(A a, B b) throws Throwable;

  @Override
  default R apply(A a, B b) {
    try {
      return tryApply(a, b);
    } catch (Throwable e) {
      throw new LambdaException("Can't apply function", e);
    }
  }

  static <U> BinaryOperator<U> noOp() {
    return (a, b) -> a;
  }
}

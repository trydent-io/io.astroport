package io.citadel.kernel.func;

import io.citadel.kernel.domain.Domain;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ThrowableBiFunction<A, B, R> extends BiFunction<A, B, R> {
  R tryApply(A a, B b) throws Throwable;

  @Override
  default R apply(A a, B b) {
    try {
      return tryApply(a, b);
    } catch (Throwable e) {
      throw new FunctionalException("Can't apply function", e);
    }
  }
}

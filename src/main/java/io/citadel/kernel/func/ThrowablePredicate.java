package io.citadel.kernel.func;

import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowablePredicate<A> extends Predicate<A> {
  @Override
  default boolean test(A a) {
    try {
      return tryTest(a);
    } catch (Throwable e) {
      throw new LambdaException("Can't test predicate", e);
    }
  }

  boolean tryTest(A a) throws Throwable;
}

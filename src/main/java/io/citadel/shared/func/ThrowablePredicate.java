package io.citadel.shared.func;

import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowablePredicate<A> extends Predicate<A> {
  @Override
  default boolean test(A a) {
    try {
      return tryTest(a);
    } catch (Throwable e) {
      throw new FunctionalException("Can't test predicate", e);
    }
  }

  boolean tryTest(A a) throws Throwable;
}

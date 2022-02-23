package io.citadel.kernel.func;

import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowablePredicate<A> extends Predicate<A> {
  @Override
  default boolean test(A a) {
    try {
      return tryTest(a);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  boolean tryTest(A a) throws Throwable;

  static <T> ThrowablePredicate<T> throwable(Predicate<T> predicate) {
    return predicate::test;
  }
}

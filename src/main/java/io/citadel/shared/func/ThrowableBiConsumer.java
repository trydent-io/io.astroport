package io.citadel.shared.func;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface ThrowableBiConsumer<A, B> extends BiConsumer<A, B> {
  @Override
  default void accept(A a, B b) {
    try {
      tryAccept(a, b);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  void tryAccept(A a, B b) throws Throwable;
}

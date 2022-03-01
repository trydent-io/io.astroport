package io.citadel.shared.func;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableConsumer<A> extends Consumer<A> {
  @Override
  default void accept(A a) {
    try {
      tryAccept(a);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  void tryAccept(A a) throws Throwable;

  default <T> T accepting(T $this, A a) {
    accept(a);
    return $this;
  }
}

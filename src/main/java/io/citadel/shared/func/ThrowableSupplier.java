package io.citadel.shared.func;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowableSupplier<A> extends Supplier<Maybe<A>> {
  @Override
  default Maybe<A> get() {
    try {
      return Maybe.of(tryGet());
    } catch (Throwable e) {
      return Maybe.failure(e);
    }
  }

  A tryGet() throws Throwable;
}

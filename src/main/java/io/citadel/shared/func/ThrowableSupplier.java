package io.citadel.shared.func;

import io.alpenglow.kernel.Maybe;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowableSupplier<A> extends Supplier<Maybe<A>> {
  @Override
  default Maybe<A> get() {
    try {
      return Maybe.value(tryGet());
    } catch (Throwable e) {
      return Maybe.error(e);
    }
  }

  A tryGet() throws Throwable;
}

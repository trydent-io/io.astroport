package io.citadel.kernel.func;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowableSupplier<A> extends Supplier<A> {
  @Override
  default A get() {
    try {
      return tryGet();
    } catch (Throwable e) {
      throw new FunctionalException("Can't get from supplier", e);
    }
  }

  A tryGet() throws Throwable;
}

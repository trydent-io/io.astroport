package io.citadel.kernel.func;

import java.util.function.Supplier;

@FunctionalInterface
public interface TrySupplier<A> extends Supplier<A> {
  @Override
  default A get() {
    try {
      return tryGet();
    } catch (Throwable e) {
      throw new LambdaException("Can't get from supplier", e);
    }
  }

  A tryGet() throws Throwable;
}

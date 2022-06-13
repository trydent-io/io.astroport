package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.ID;
import io.citadel.kernel.func.ThrowableFunction;

@FunctionalInterface
public interface Normalize<R extends Record, E> {
  Identity<R, E> identity(ThrowableFunction<? super ID<?>, ? extends R> initializer);
}

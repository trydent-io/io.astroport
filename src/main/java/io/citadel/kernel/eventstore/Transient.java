package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.ID;
import io.citadel.kernel.func.ThrowableFunction;

@FunctionalInterface
public interface Transient<R extends Record, E> {
  Identity<R, E> creates(ThrowableFunction<? super ID<?>, ? extends R> initializer);
}

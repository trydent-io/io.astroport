package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;

@FunctionalInterface
public interface Normalize<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, E extends Domain.Event> {
  Identity<M, E> identity(ThrowableFunction<? super ID, ? extends M> initializer);
}

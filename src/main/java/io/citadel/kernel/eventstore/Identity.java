package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableBiFunction;

public interface Identity<M extends Record & Domain.Model<?>, E extends Domain.Event> {
  default <S extends Enum<S> & Domain.State<S, E>> Finaltype<M, E, S> hydrate(ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
    return hydrate(null, hydrator);
  }

  <S extends Enum<S> & Domain.State<S, E>> Finaltype<M, E, S> hydrate(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator);
}

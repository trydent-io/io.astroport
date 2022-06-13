package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableBiFunction;

public interface Identity<R extends Record, E> {
  default <S extends Enum<S> & Domain.State<S, E>> Context<R, S, E> hydrate(ThrowableBiFunction<? super R, ? super E, ? extends R> hydrator) {
    return hydrate(null, hydrator);
  }

  <S extends Enum<S> & Domain.State<S, E>> Context<R, S, E> hydrate(S initial, ThrowableBiFunction<? super R, ? super E, ? extends R> hydrator);
}

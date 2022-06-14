package io.citadel.kernel.domain;

import java.util.Optional;

public interface State<S extends Enum<S> & State<S, E>, E> {
  @SuppressWarnings("unchecked")
  default boolean is(S... states) {
    var index = 0;
    while (index < states.length && states[index] != this)
      index++;
    return index < states.length;
  }

  Optional<S> next(E event);
}

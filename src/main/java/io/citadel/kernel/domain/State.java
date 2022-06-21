package io.citadel.kernel.domain;

public interface State<S extends Enum<S> & State<S, E>, E> {
  @SuppressWarnings("unchecked")
  default boolean is(S... states) {
    var index = 0;
    while (index < states.length && states[index] != this)
      index++;
    return index < states.length;
  }

  S transit(E event);

  default boolean transitable(E event) {
    return transit(event) != null;
  }
}

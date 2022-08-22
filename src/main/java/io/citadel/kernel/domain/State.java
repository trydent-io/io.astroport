package io.citadel.kernel.domain;

public interface State<STATE extends Enum<STATE> & State<STATE, EVENT>, EVENT> {
  @SuppressWarnings("unchecked")
  default boolean is(STATE... states) {
    var index = 0;
    //noinspection StatementWithEmptyBody
    while (index < states.length && states[index++] != this);
    return index < states.length;
  }

  STATE transit(EVENT event);

  default boolean transitable(EVENT event) {
    try {
      return transit(event) != null;
    } catch (IllegalStateException ignored) {
      return false;
    }
  }
}

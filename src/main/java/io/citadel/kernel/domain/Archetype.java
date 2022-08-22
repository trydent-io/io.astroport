package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.Audit;

public interface Archetype<ID, ENTITY extends Record, EVENT, STATE extends Enum<STATE> & State<STATE, EVENT>> {
  STATE initial();
  ENTITY initialize(ID id);
  EVENT transform(Audit.Event event);
  ENTITY accumulate(ENTITY entity, EVENT event);
}

package io.citadel.forum;

import io.citadel.kernel.eventstore.Event;

import java.util.UUID;

public interface Forums {
  Forum restore(UUID id);
  void persist(Event... events);
}

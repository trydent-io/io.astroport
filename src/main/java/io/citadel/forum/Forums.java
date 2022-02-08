package io.citadel.forum;

import io.citadel.domain.source.Event;

import java.util.UUID;

public interface Forums {
  Forum restore(UUID id);
  void persist(Event... events);
}

package io.citadel.domain.source;

import io.citadel.domain.message.Event;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public enum Events {
  Defaults;

  public Event stored(String name, UUID aggregateId, String aggregate, JsonObject data) { return new StoredEvents.Stored(name, aggregateId, aggregate, data); }
}

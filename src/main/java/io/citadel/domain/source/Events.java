package io.citadel.domain.source;

import io.citadel.domain.message.Event;
import io.citadel.forum.Forum;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.UUID;

public enum Events {
  Defaults;

  public Optional<Event> from(String name, JsonObject data) {
    return Forum.events.from(name, data);
  }

  public Event stored(String name, UUID aggregateId, String aggregate, JsonObject data) { return new EventLogs.Stored(name, aggregateId, aggregate, data); }
}

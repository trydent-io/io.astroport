package io.citadel.domain.message;

import io.citadel.domain.source.EventLog;
import io.citadel.domain.source.EventStore;
import io.citadel.domain.source.EventLogs;
import io.citadel.domain.source.Events;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public interface Event {
  Events defaults = Events.Defaults;

  Command asCommand();

  default EventLog asEventLog(UUID aggregateId, String aggregate) {
    return switch (this) {
      case EventLogs.Stored stored -> stored;
      default -> EventStore.events.stored(this.getClass().getSimpleName(), aggregateId, aggregate, JsonObject.mapFrom(this));
    };
  }
}

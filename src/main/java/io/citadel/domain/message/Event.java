package io.citadel.domain.message;

import io.citadel.domain.source.EventStore;
import io.citadel.domain.source.StoredEvents;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public interface Event {
  default Event asStored(UUID aggregateId, String aggregate) {
    return switch (this) {
      case StoredEvents.Stored stored -> stored;
      default -> EventStore.events.stored(this.getClass().getSimpleName(), aggregateId, aggregate, JsonObject.mapFrom(this));
    };
  }
}

package io.citadel.domain.source;

import io.citadel.domain.message.Event;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public interface StoredEvents extends Iterable<StoredEvents.Stored> {
  record Stored(UUID id, UUID revision, String name, UUID aggregateId, String aggregate, JsonObject data, LocalDateTime persistedAt) implements Event {
    public Stored(String name, UUID aggregateId, String aggregate, JsonObject data) {
      this(UUID.randomUUID(), UUID.randomUUID(), name, aggregateId, aggregate, data, LocalDateTime.now(ZoneId.of("UTC")));
    }
  }

  static StoredEvents of(Stored... events) { return new Array(events); }

  final class Array implements StoredEvents {
    private final Stored[] events;

    private Array(final Stored[] events) {this.events = events;}

    @Override
    public Iterator<Stored> iterator() {
      return List.of(events).iterator();
    }
  }
}

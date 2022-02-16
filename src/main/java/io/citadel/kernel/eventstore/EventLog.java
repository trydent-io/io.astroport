package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface EventLog permits Entry {
  static EventLog of(String eventName, JsonObject eventData, UUID aggregateId, String aggregateName, UUID revision) {
    return of(UUID.randomUUID(), eventName, eventData, aggregateId, aggregateName, revision, LocalDateTime.now());
  }

  static EventLog of(UUID id, String eventName, JsonObject eventData, UUID aggregateId, String aggregateName, UUID revision, LocalDateTime persistedAt) {
    return Entry.with(entry -> {
      entry.id = id;
      entry.event = EventInfo.with(it -> {
        it.name = eventName;
        it.data = eventData;
      });
      entry.aggregate = AggregateInfo.with(it -> {
        it.id = aggregateId;
        it.name = aggregateName;
        it.revision = revision;
      });
      entry.persistedAt = persistedAt;
    });
  }

  Domain.Event asEvent();
}

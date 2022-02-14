package io.citadel.domain.source;

import io.citadel.domain.message.Event;
import io.citadel.domain.source.eventlog.AggregateInfo;
import io.citadel.domain.source.eventlog.EventInfo;
import io.citadel.domain.source.eventlog.Entry;
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

  Event asEvent();
}

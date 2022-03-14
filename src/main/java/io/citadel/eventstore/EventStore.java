package io.citadel.eventstore;

import io.citadel.eventstore.event.Events;
import io.citadel.eventstore.type.Sql;
import io.citadel.eventstore.type.Defaults;
import io.citadel.eventstore.type.Local;
import io.citadel.eventstore.type.Service;
import io.citadel.shared.context.Domain.Version;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

public sealed interface EventStore permits Sql, Local, Service {
  String FIND_EVENTS_BY = "eventStore.findEventsBy";
  String PERSIST_EVENTS = "eventStore.persistEvents";

  Defaults defaults = Defaults.Defaults;
  Types types = Types.Defaults;

  default Verticle asVerticle() {
    return switch (this) {
      case Service service -> service;
      default -> null;
    };
  }
  default Future<Events> findEventsBy(String id, String name) {return findEventsBy(id, name, Version.last().value()); }
  Future<Events> findEventsBy(String id, String name, long version);
  Future<Stream<EventLog>> persist(Raw aggregate, Stream<EventInfo> events);

  sealed interface AggregateInfo {
    String id();
    String name();
    long version();
  }

  record Raw(String id, String name, long version) implements AggregateInfo {
    public Raw(String id, String name) { this(id, name, Long.MAX_VALUE); }
  }
  record EventInfo(String name, JsonObject data) {}
  record EventLog(UUID id, Raw aggregate, EventInfo event, LocalDateTime persistedAt, String persistedBy) {}
}

package io.citadel.eventstore;

import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.eventstore.data.EventLog;
import io.citadel.eventstore.event.Events;
import io.citadel.eventstore.type.Sql;
import io.citadel.eventstore.type.Defaults;
import io.citadel.eventstore.type.Local;
import io.citadel.eventstore.type.Service;
import io.vertx.core.Future;
import io.vertx.core.Verticle;

import java.util.stream.Stream;

public sealed interface EventStore permits Sql, Local, Service {
  String FIND_EVENTS_BY = "eventStore.findEventsBy";
  String PERSIST_EVENTS = "eventStore.persistEvents";

  Defaults defaults = Defaults.Defaults;
  Data data = Data.Defaults;

  default Verticle asVerticle() {
    return switch (this) {
      case Service service -> service;
      default -> null;
    };
  }

  default Future<Events> findEventsBy(String id, String name) {return findEventsBy(id, name, Version.last().value()); }
  Future<Events> findEventsBy(String id, String name, long version);
  Future<Stream<EventLog>> persist(AggregateInfo aggregate, Stream<EventInfo> events);
}

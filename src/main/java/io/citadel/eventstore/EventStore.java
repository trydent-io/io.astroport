package io.citadel.eventstore;

import io.citadel.eventstore.data.MetaAggregate;
import io.citadel.eventstore.data.MetaEvent;
import io.citadel.eventstore.data.EventLog;
import io.citadel.eventstore.event.Events;
import io.citadel.eventstore.type.Defaults;
import io.citadel.eventstore.type.Local;
import io.citadel.eventstore.type.Service;
import io.citadel.eventstore.type.Sql;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface EventStore permits EventStore.Verticle, Local, Sql {
  String FIND_EVENTS_BY = "eventStore.findEventsBy";
  String PERSIST_EVENTS = "eventStore.persistEvents";

  Defaults defaults = Defaults.Companion;

  default EventStore.Verticle asVerticle() {
    return switch (this) {
      case EventStore.Verticle verticle -> verticle;
      default -> null;
    };
  }

  sealed interface Verticle extends EventStore, io.vertx.core.Verticle permits Service {}

  Future<Events> findEventsBy(String id, String name);
  default Future<Stream<EventLog>> persist(String id, String name, long version, Stream<MetaEvent> events) {
    return persist(new MetaAggregate(id, name, version), events);
  }
  Future<Stream<EventLog>> persist(MetaAggregate aggregate, Stream<MetaEvent> events);
}

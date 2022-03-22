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

public sealed interface EventStore permits EventStore.Verticle, Local, Sql {
  String FIND_EVENTS_BY = "eventStore.findEventsBy";
  String PERSIST_EVENTS = "eventStore.persistEvents";

  Defaults defaults = Defaults.Defaults;
  Data data = Data.Defaults;

  default EventStore.Verticle asVerticle() {
    return switch (this) {
      case EventStore.Verticle verticle -> verticle;
      default -> null;
    };
  }

  sealed interface Verticle extends EventStore, io.vertx.core.Verticle permits Service {}

  Future<Events> findEventsBy(String id, String name);
  Future<Stream<EventLog>> persist(AggregateInfo aggregate, Stream<EventInfo> events);
}

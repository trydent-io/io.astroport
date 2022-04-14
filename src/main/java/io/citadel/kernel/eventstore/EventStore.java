package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.data.AggregateInfo;
import io.citadel.kernel.eventstore.data.EventInfo;
import io.citadel.kernel.eventstore.data.EventLog;
import io.citadel.kernel.eventstore.event.Events;
import io.citadel.kernel.eventstore.type.Defaults;
import io.citadel.kernel.eventstore.type.Local;
import io.citadel.kernel.eventstore.type.Service;
import io.citadel.kernel.eventstore.type.Sql;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface EventStore permits EventStore.Verticle, Local, Sql {
  String FIND_EVENTS_BY = "eventStore.findEventsBy";
  String PERSIST = "eventStore.persist";

  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends EventStore, io.vertx.core.Verticle permits Service {}

  Future<Events> findEventsBy(String id, String name);
  Future<Stream<EventLog>> persist(AggregateInfo aggregate, Stream<EventInfo> events, String by);
}

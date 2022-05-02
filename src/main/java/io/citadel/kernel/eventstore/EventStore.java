package io.citadel.kernel.eventstore;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.eventstore.type.Defaults;
import io.citadel.kernel.eventstore.type.Local;
import io.citadel.kernel.eventstore.type.Service;
import io.citadel.kernel.eventstore.type.Sql;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface EventStore permits EventStore.Verticle, Local, Sql {
  String SEEK = "eventStore.seek";
  String PERSIST = "eventStore.persist";

  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends EventStore, io.vertx.core.Verticle permits Service {}

  Future<Feed> seek(Feed.Aggregate aggregate);

  default Future<Feed> feed(Feed.Aggregate aggregate, Stream<Feed.Event> events) {
    return feed(aggregate, events, null);
  }
  Future<Feed> feed(Feed.Aggregate aggregate, Stream<Feed.Event> events, String by);
}

package io.citadel.kernel.eventstore;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;

import java.util.stream.Stream;

public interface EventStore extends Lookup {
  String SEEK = "eventStore.seek";
  String FEED = "eventStore.feed";

  static EventStore sql(EventBus eventBus, SqlClient client) {
    return new Sql(eventBus, client);
  }

  default Future<Prototype> feed(Meta.Aggregate aggregate, Stream<Meta.Event> events) {
    return feed(aggregate, events, null);
  }
  Future<Prototype> feed(Meta.Aggregate aggregate, Stream<Meta.Event> events, String by);
}

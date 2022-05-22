package io.citadel.kernel.eventstore;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.eventstore.type.Sql;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;

import java.util.stream.Stream;

public sealed interface EventStore permits Sql {
  String SEEK = "eventStore.seek";
  String FEED = "eventStore.feed";

  static EventStore sql(EventBus eventBus, SqlClient client) {
    return new Sql(eventBus, client);
  }


  Future<Feed> seek(Feed.Aggregate aggregate);

  default Future<Feed> feed(Feed.Aggregate aggregate, Stream<Feed.Event> events) {
    return feed(aggregate, events, null);
  }
  Future<Feed> feed(Feed.Aggregate aggregate, Stream<Feed.Event> events, String by);
}

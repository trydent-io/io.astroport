package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.Aggregate;
import io.citadel.kernel.eventstore.meta.Event;
import io.citadel.kernel.eventstore.meta.ID;
import io.citadel.kernel.eventstore.meta.Name;
import io.citadel.kernel.eventstore.meta.Version;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.stream.Stream;

public sealed interface EventStore {
  static EventStore client(Vertx vertx, SqlConnectOptions options) {
    return new Client(vertx.eventBus(), PgPool.client(vertx, PgConnectOptions.wrap(options), new PoolOptions()));
  }
  Future<Aggregate> query(ID id, Name name, Version version);
  Future<Void> persist(ID id, Name name, Version version, Stream<Event> events);

  record Client(EventBus eventBus, SqlClient client) implements EventStore, Query, Persist {
    public SqlClient queryClient() { return client; }
    public SqlClient persistClient() { return client; }

    @Override
    public Future<Aggregate> query(ID id, Name name, Version version) {
      return aggregates(id, name, version)
        .map(Stream::findFirst)
        .map(found -> found.orElseGet(() -> Aggregate.identity(id, name)));
    }

    @Override
    public Future<Void> persist(ID id, Name name, Version version, Stream<Event> events) {
      return null;
    }
  }
}

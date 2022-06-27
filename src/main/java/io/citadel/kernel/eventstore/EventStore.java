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

  final class Client implements EventStore, Query {
    private final EventBus eventBus;
    private final SqlClient client;

    private Client(EventBus eventBus, SqlClient client) {
      this.eventBus = eventBus;
      this.client = client;
    }

    public SqlClient sqlClient() { return client; }

    @Override
    public Future<Aggregate> query(ID id, Name name, Version version) {
      return aggregates(id, name, version).map(aggregates -> aggregates.findFirst().orElseGet(() -> Aggregate.identity(id, name)));
    }

    @Override
    public Future<Void> persist(ID id, Name name, Version version, Stream<Event> events) {
      return null;
    }
  }
}

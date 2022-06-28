package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.metadata.Aggregate;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.metadata.ID;
import io.citadel.kernel.eventstore.metadata.Name;
import io.citadel.kernel.eventstore.metadata.State;
import io.citadel.kernel.eventstore.metadata.Version;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.stream.Stream;

public sealed interface EventStorePool permits Client {
  static EventStorePool client(Vertx vertx, SqlConnectOptions options) {
    return new Client(vertx.eventBus(), PgPool.client(vertx, PgConnectOptions.wrap(options), new PoolOptions().setMaxSize(10)));
  }
  Future<Aggregate> query(ID id, Name name, Version version);
  default <T> Future<Aggregate> query(T id, String name, long version) {
    return query(Aggregate.id(id), Aggregate.name(name), Aggregate.version(version));
  }
  default <T> Future<Aggregate> query(T id, String name) {
    return query(Aggregate.id(id), Aggregate.name(name), Version.Last);
  }
  default Future<Aggregate> query(ID id, Name name) {
    return query(id, name, Version.Last);
  }

  Future<Void> update(ID id, Name name, Version version, State state, Stream<Change> changes);

}

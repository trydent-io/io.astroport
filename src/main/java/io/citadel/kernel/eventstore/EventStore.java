package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Audit;
import io.citadel.kernel.eventstore.event.Entity;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.stream.Stream;

public sealed interface EventStore permits Client {
  static EventStore client(Vertx vertx, SqlConnectOptions options) {
    return new Client(vertx.eventBus(), PgPool.client(vertx, PgConnectOptions.wrap(options), new PoolOptions().setMaxSize(10)));
  }
  Future<Stream<Audit>> restore(Entity.ID id, Entity.Name name);

  Future<Void> store(Stream<Audit> entityEvents);

}

package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.EntityEvent;
import io.citadel.kernel.eventstore.event.Event;
import io.citadel.kernel.eventstore.metadata.MetaAggregate;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.event.ID;
import io.citadel.kernel.eventstore.event.Name;
import io.citadel.kernel.eventstore.metadata.State;
import io.citadel.kernel.eventstore.event.Version;
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
  Future<Stream<EntityEvent>> restore(Entity.ID id, Entity.Name name);

  Future<Void> store(Entity.ID id, Entity.Name name, Entity.Version version, Stream<Event> events);

}

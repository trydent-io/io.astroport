package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.metadata.MetaAggregate;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.audit.ID;
import io.citadel.kernel.eventstore.audit.Name;
import io.citadel.kernel.eventstore.metadata.State;
import io.citadel.kernel.eventstore.audit.Version;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnectOptions;

import java.util.stream.Stream;

public sealed interface Entities permits Client {
  static Entities client(Vertx vertx, SqlConnectOptions options) {
    return new Client(vertx.eventBus(), PgPool.client(vertx, PgConnectOptions.wrap(options), new PoolOptions().setMaxSize(10)));
  }
  Future<MetaAggregate> query(ID id, Name name, Version version);
  default <T> Future<MetaAggregate> query(T id, String name, long version) {
    return query(MetaAggregate.id(id), MetaAggregate.name(name), MetaAggregate.version(version));
  }
  default <T> Future<MetaAggregate> query(T id, String name) {
    return query(MetaAggregate.id(id), MetaAggregate.name(name), Version.Last);
  }
  default Future<MetaAggregate> query(ID id, Name name) {
    return query(id, name, Version.Last);
  }

  Future<Void> update(ID id, Name name, Version version, State state, Stream<Change> changes);

}

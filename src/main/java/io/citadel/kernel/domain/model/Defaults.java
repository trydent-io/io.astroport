package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.sql.Database;
import io.citadel.kernel.sql.Migration;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

import java.util.stream.Stream;

public enum Defaults {
  Companion;

  public Domain.Verticle verticle(Vertx vertx, Database database) {
    return new Service(
      Migration.eventStore(vertx, database),
      EventStore.defaults.sql(
        vertx.eventBus(),
        PgPool.pool(vertx, database.asPgOptions(), new PoolOptions().setMaxSize(10))
      )
    );
  }


  public <A extends Domain.Aggregate, I extends Domain.ID<?>, M extends Record & Domain.Model<I>> Domain.Lookup<M, A> lookup(EventStore eventStore, Domain.Snapshot<M, A> snapshot, String name) {
    return new Aggregates<>(eventStore, snapshot, name);
  }

  public Domain.Transaction transaction(EventStore eventStore) {
    return new Changes(eventStore, Stream.empty());
  }
}

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
      EventStore.sql(
        vertx.eventBus(),
        PgPool.pool(vertx, database.asPgOptions(), new PoolOptions().setMaxSize(10))
      )
    );
  }


  public <A extends Domain.Aggregate<?, ?>> Domain.Lookup<A> lookup(EventStore eventStore, Domain.Snapshot<A> snapshot) {
    return new Aggregates<>(eventStore, snapshot);
  }

  public <E extends Domain.Event, L extends Domain.Timeline<E, L>> Domain.Transaction<E> transaction(EventStore eventStore, L lifecycle) {
    return new Changes<>(lifecycle, eventStore, Stream.empty());
  }
}

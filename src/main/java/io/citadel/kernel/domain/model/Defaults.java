package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.Lookup;
import io.citadel.kernel.sql.Database;
import io.citadel.kernel.sql.Migration;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public enum Defaults {
  Companion;

  public Domain.Verticle service(Vertx vertx, Database database) {
    final var client = PgPool.pool(vertx, database.asPgOptions(), new PoolOptions().setMaxSize(10));
    Actors.register(() -> )
    return new Service(
      Migration.eventStore(vertx, database),
      Lookup.create(client),
      Lookup.sql(
        vertx.eventBus(),
        client
      )
    );
  }

  interface Actors {
    static <A extends Actor<
  }
}

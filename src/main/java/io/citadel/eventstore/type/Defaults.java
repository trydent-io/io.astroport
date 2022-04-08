package io.citadel.eventstore.type;

import io.citadel.eventstore.EventStore;
import io.citadel.kernel.sql.Database;
import io.citadel.kernel.sql.Migration;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public enum Defaults {
  Companion;

  public EventStore.Verticle verticle(Vertx vertx, Database database) {
    return new Service(
      Migration.eventStore(vertx, database),
      EventStore.defaults.sql(
        vertx.eventBus(),
        PgPool.pool(vertx, database.asPgOptions(), new PoolOptions().setMaxSize(10))
      )
    );
  }

  public EventStore sql(EventBus eventBus, SqlClient client) {
    return new Sql(eventBus, client);
  }

  public EventStore local(EventBus eventBus) {
    return new Local(eventBus);
  }
}

package io.citadel.eventstore.type;

import io.citadel.eventstore.EventStore;
import io.citadel.shared.sql.Database;
import io.citadel.shared.sql.Migration;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public enum Defaults {
  Defaults;

  public EventStore service(Vertx vertx, Database database) {
    return new Service(
      Migration.eventStore(vertx, database),
      EventStore.defaults.sql(
        PgPool.pool(vertx, database.asPgOptions(), new PoolOptions().setMaxSize(10))
      )
    );
  }

  public EventStore sql(EventBus eventBus, SqlClient client) {
    return new Sql(eventBus, client);
  }

  public EventStore requestor(EventBus eventBus) {
    return new Local(eventBus);
  }
}

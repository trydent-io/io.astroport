package io.citadel.eventstore;

import io.citadel.shared.sql.Database;
import io.citadel.shared.sql.Migration;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public enum Defaults {
  Companions;

  public EventStore service(Vertx vertx, Database database) {
    return new Service(
      Migration.eventStore(vertx, database),
      EventStore.defaults.client(
        PgPool.pool(vertx, database.asPgOptions(), new PoolOptions().setMaxSize(10))
      )
    );
  }

  public EventStore client(SqlClient client) {
    return new Client(client);
  }

  public EventStore requestor(EventBus eventBus) {
    return new Forward(eventBus);
  }
}

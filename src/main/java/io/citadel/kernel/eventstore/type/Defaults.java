package io.citadel.kernel.eventstore.type;

import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.sql.Database;
import io.citadel.kernel.sql.Migration;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

public enum Defaults {
  Companion;

  public EventStore sql(EventBus eventBus, SqlClient client) {
    return new Sql(eventBus, client);
  }

  public EventStore local(EventBus eventBus) {
    return new Local(eventBus);
  }
}

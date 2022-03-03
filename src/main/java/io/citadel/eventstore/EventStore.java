package io.citadel.eventstore;

import io.citadel.shared.sql.Database;
import io.citadel.shared.sql.Migration;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;

import java.util.stream.Stream;

public sealed interface EventStore permits Client, Service {
  Operations operations = Operations.Defaults;

  static EventStore service(Vertx vertx, Database database) {
    return new Service(
      Migration.eventStore(vertx, database),
      EventStore.client(
        PgPool.pool(vertx, database.asPgOptions(), new PoolOptions().setMaxSize(10))
      )
    );
  }

  static EventStore client(SqlClient client) {
    return new Client(client);
  }

  default Verticle asVerticle() {
    return switch (this) {
      case Service service -> service;
      default -> null;
    };
  }

  Future<Stream<EventLog>> findBy(String aggregateId, String aggregateName);
  Future<Void> persist(EventLog.AggregateInfo aggregate, EventLog.EventInfo... events);
}

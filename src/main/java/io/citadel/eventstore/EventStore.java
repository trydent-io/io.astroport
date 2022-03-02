package io.citadel.eventstore;

import io.citadel.shared.db.Database;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;

import java.sql.Driver;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public sealed interface EventStore permits Sql, Service {
  Operations operations = Operations.Defaults;

  static <D extends Driver> EventStore service(Vertx vertx, Database.Info<D> info) {
    return new Service(new Sql(JDBCPool.pool(vertx, info.asOptions(), new PoolOptions().setMaxSize(10))));
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

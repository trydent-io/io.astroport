package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.media.Array;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.internal.jdbc.DriverDataSource;

import java.sql.Driver;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EventStore {
  Operations operations = Operations.Defaults;

  record DatabaseConnection<D extends Driver>(String name, String username, String password, String url, int port, Class<D> driver) {
    JDBCConnectOptions asOptions() {
      return new JDBCConnectOptions()
        .setDatabase(name)
        .setUser(username)
        .setPassword(password)
        .setJdbcUrl(url);
    }
  }

  record EventLog(UUID id, AggregateInfo aggregate, EventInfo event, LocalDateTime persistedAt, String persistedBy) {}
  record AggregateInfo(Domain.ID<?> id, String name, Domain.Version version) {}
  record EventInfo(String name, JsonObject data) {}

  static <D extends Driver> Verticle asVerticle(Vertx vertx, DatabaseConnection<D> connection) {
    return
      new Service(
        new Repository(
          JDBCPool.pool(vertx, connection.asOptions(), new PoolOptions().setMaxSize(10)),
          new DriverDataSource(
            connection.driver.getClassLoader(),
            connection.driver.getName(),
            connection.url,
            connection.username,
            connection.password
          )
        )
      );
  }

  Future<Array<EventLog>> findBy(UUID id);

  void persist(EventLog... events);

}

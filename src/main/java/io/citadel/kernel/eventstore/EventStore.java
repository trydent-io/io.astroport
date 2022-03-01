package io.citadel.kernel.eventstore;

import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import org.flywaydb.core.internal.jdbc.DriverDataSource;

import java.sql.Driver;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public sealed interface EventStore permits Sql, Service {
  Operations operations = Operations.Defaults;

  static <D extends Driver> EventStore service(Vertx vertx, DatabaseConnection<D> connection) {
    return
      new Service(
        new Sql(
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

  default Optional<Verticle> asVerticle() {
    return switch (this) {
      case Service service -> Optional.of(service);
      default -> Optional.empty();
    };
  }

  Future<Stream<EventLog>> findBy(String aggregateId, String aggregateName);
  Future<Void> persist(EventLog.AggregateInfo aggregate, EventLog.EventInfo... events);

  record DatabaseConnection<D extends Driver>(String name, String username, String password, String url, int port, Class<D> driver) {
    JDBCConnectOptions asOptions() {
      return new JDBCConnectOptions()
        .setDatabase(name)
        .setUser(username)
        .setPassword(password)
        .setJdbcUrl(url);
    }
  }


  enum Type {;

  }

}

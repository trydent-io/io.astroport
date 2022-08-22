package io.citadel.kernel.eventstore;

import com.zaxxer.hikari.HikariConfig;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import org.postgresql.Driver;

import java.util.concurrent.atomic.AtomicReference;

interface DatabaseOptions {
  default <DRIVER extends java.sql.Driver> HikariConfig asHikariOptions(SqlConnectOptions options, Class<DRIVER> driver) {
    final var config = new HikariConfig();
    config.setJdbcUrl(options.getHost());
    config.setUsername(options.getUser());
    config.setPassword(options.getPassword());
    config.setDriverClassName(driver.getName());
    return config;
  }
}

public sealed interface Database {
  static Database create(Vertx vertx, SqlConnectOptions options) {
    return new Default(vertx, options);
  }

  <DRIVER extends java.sql.Driver> Database postgres(Class<DRIVER> driver);
  default Database cached() { return new Cached(this); }
  default Database migration(String migrationsFolder) {
    throw new IllegalStateException("Can't setup migration, no specific database selected");
  }
  Future<SqlClient> client();

  final class Default implements Database {
    private final Vertx vertx;
    private final SqlConnectOptions options;
    private Default(Vertx vertx, SqlConnectOptions options) {
      this.vertx = vertx;
      this.options = options;
    }

    @Override
    public <DRIVER extends java.sql.Driver> Database postgres(Class<DRIVER> driver) {
      return new Postgres(vertx, PgConnectOptions.wrap(options), new PoolOptions(), driver);
    }

    @Override
    public Database migration(String migrationsFolder) {
      throw new IllegalStateException("Can't setup migration, no specific database selected");
    }

    @Override
    public Future<SqlClient> client() {
      return null;
    }
  }

  final class Migration implements Database {

    @Override
    public Database postgres() {
      return null;
    }

    @Override
    public Database cached() {
      return null;
    }

    @Override
    public Database migration(String migrationsFolder) {
      return null;
    }

    @Override
    public Future<SqlClient> client() {
      return null;
    }
  }

  final class Postgres implements Database {
    private final Vertx vertx;
    private final PgConnectOptions wrap;
    private final PoolOptions poolOptions;
    private final Class<Driver> driver;
    private Postgres(Vertx vertx, PgConnectOptions wrap, PoolOptions poolOptions, Class<Driver> driver) {
      this.vertx = vertx;
      this.wrap = wrap;
      this.poolOptions = poolOptions;
      this.driver = driver;
    }

    @Override
    public Database postgres() {
      return this;
    }

    @Override
    public Database cached() {
      return null;
    }

    @Override
    public Database migration(String migrationsFolder) {
      return null;
    }

    @Override
    public Future<SqlClient> client() {
      return null;
    }
  }

  final class Cached implements Database {
    private final Database origin;
    private final AtomicReference<Future<SqlClient>> client;
    private Cached(Database origin) {
      this(origin, new AtomicReference<>());
    }
    private Cached(Database origin, AtomicReference<Future<SqlClient>> client) {
      this.origin = origin;
      this.client = client;
    }

    @Override
    public Database postgres() {
      return null;
    }

    @Override
    public Database cached() {
      return this;
    }

    @Override
    public Database migration(String migrationsFolder) {
      return null;
    }

    @Override
    public Future<SqlClient> client() {
      client.compareAndSet(null, origin.client());
      return client.get();
    }
  }
}

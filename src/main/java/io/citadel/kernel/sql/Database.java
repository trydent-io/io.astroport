package io.citadel.kernel.sql;

import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.pgclient.PgConnectOptions;

import javax.sql.DataSource;
import java.sql.Driver;

public sealed interface Database permits Connection {
  static <D extends Driver> Database connection(String protocol, String host, int port, String database, String username, String password, Class<D> driver) {
    return new Connection<>(
      protocol,
      host,
      port,
      database,
      username,
      password,
      driver
    );
  }

  DataSource asDataSource();
  JDBCConnectOptions asJdbcOptions();
  PgConnectOptions asPgOptions();
}

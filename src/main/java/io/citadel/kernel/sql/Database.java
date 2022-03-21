package io.citadel.kernel.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.pgclient.PgConnectOptions;

import javax.sql.DataSource;
import java.sql.Driver;

public sealed interface Database {
  static <D extends Driver> Database connection(String protocol, String host, int port, String database, String username, String password, Class<D> driver) {
    return new Type.Connection<>(
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

  enum Type {;
    private record Connection<D extends Driver>(String protocol, String host, int port, String database, String username, String password, Class<D> driver) implements Database {
      private String url() { return "%s://%s:%d/%s".formatted(protocol, host, port, database); }
      @Override
      public JDBCConnectOptions asJdbcOptions() {
        return new JDBCConnectOptions()
          .setUser(username)
          .setPassword(password)
          .setJdbcUrl(url());
      }

      @Override
      public PgConnectOptions asPgOptions() {
        return new PgConnectOptions()
          .setHost(host)
          .setPort(port)
          .setDatabase(database)
          .setUser(username)
          .setPassword(password);
      }

      public DataSource asDataSource() {
        final var config = new HikariConfig();
        config.setJdbcUrl(url());
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver.getName());
        return new HikariDataSource(config);
      }
    }
  }
}

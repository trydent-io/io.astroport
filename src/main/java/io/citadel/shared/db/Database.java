package io.citadel.shared.db;

import io.vertx.jdbcclient.JDBCConnectOptions;
import org.flywaydb.core.internal.jdbc.DriverDataSource;

import javax.sql.DataSource;
import java.sql.Driver;

public sealed interface Database {
  enum Namespace implements Database {}

  record Info<D extends Driver>(String name, String username, String password, String url, int port, Class<D> driver) {
    public JDBCConnectOptions asOptions() {
      return new JDBCConnectOptions()
        .setDatabase(name)
        .setUser(username)
        .setPassword(password)
        .setJdbcUrl(url);
    }

    public DataSource toDataSource() {
      return new DriverDataSource(
        driver.getClassLoader(),
        driver.getName(),
        url,
        username,
        password
      );
    }
  }
}

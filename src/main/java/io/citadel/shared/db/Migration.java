package io.citadel.shared.db;


import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.flywaydb.core.Flyway;

import java.sql.Driver;

public sealed interface Migration permits Sql {
  static <D extends Driver> Migration sql(Vertx vertx, Database.Info<D> info) {
    return new Sql<>(vertx, info, Flyway.configure());
  }

  Future<Void> apply();
}

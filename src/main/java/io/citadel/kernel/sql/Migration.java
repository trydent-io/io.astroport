package io.citadel.kernel.sql;


import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.flywaydb.core.Flyway;

public sealed interface Migration permits Sql {
  static Migration eventStore(Vertx vertx, Database database) {
    return sql(vertx, database, "eventstore");
  }

  static Migration projections(Vertx vertx, Database database) {
    return sql(vertx, database, "projections");
  }

  private static Migration sql(Vertx vertx, Database database, String migration) {
    return new Sql(vertx, database, migration, (dataSource, location) ->
      Flyway
        .configure()
        .dataSource(database.asDataSource())
        .locations("migration/%s".formatted(migration))
        .outOfOrder(true)
        .load()
    );
  }

  Future<Void> apply();
}

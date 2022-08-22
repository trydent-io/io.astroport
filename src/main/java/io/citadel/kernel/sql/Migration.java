package io.citadel.kernel.sql;


import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.flywaydb.core.Flyway;

public sealed interface Migration permits Sql {
  static Migration eventStore(Vertx vertx, Database database) {
    return sql(vertx, database, "eventstore");
  }

  private static Migration sql(Vertx vertx, Database database, String migration) {
    return new Sql(vertx, database, migration, (dataSource, location) ->
      Flyway
        .configure()
        .dataSource(dataSource)
        .locations("migration/%s".formatted(location))
        .outOfOrder(true)
        .load()
    );
  }

  Future<Void> apply();
}

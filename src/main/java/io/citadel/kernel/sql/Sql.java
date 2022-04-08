package io.citadel.kernel.sql;

import io.citadel.CitadelException;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.Location;

import javax.sql.DataSource;

final class Sql implements Migration {
  private final Vertx vertx;
  private final Database database;
  private final String migration;
  private final ThrowableBiFunction<DataSource, Location, Flyway> flyway;

  Sql(Vertx vertx, Database database, final String migration, final ThrowableBiFunction<DataSource, Location, Flyway> flyway) {
    this.vertx = vertx;
    this.database = database;
    this.migration = migration;
    this.flyway = flyway;
  }

  @Override
  public Future<Void> apply() {
    return vertx.executeBlocking(migrate -> {
      final var migrated = flyway.apply(database.asDataSource(), new Location(migration)).migrate();
      if (migrated.success) {
        migrate.complete();
      } else {
        migrate.fail(new CitadelException("Migration for %s failed since %s".formatted(migration, migrated.warnings)));
      }
    });
  }
}

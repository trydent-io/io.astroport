package io.citadel.shared.db;

import io.citadel.CitadelException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import java.sql.Driver;

final class Sql<D extends Driver> implements Migration {
  private final Vertx vertx;
  private final Database.Info<D> info;
  private final FluentConfiguration flyway;

  Sql(Vertx vertx, Database.Info<D> info, FluentConfiguration flyway) {
    this.vertx = vertx;
    this.info = info;
    this.flyway = flyway;
  }

  @Override
  public Future<Void> apply() {
    return vertx.executeBlocking(migrate -> {
      final var migrated = flyway
        .dataSource(info.toDataSource())
        .load()
        .migrate();

      if (migrated.success) {
        migrate.complete();
      } else {
        migrate.fail(new CitadelException("Can't migrate %s".formatted(migrated.warnings)));
      }
    });
  }
}

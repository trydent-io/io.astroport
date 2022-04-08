package io.citadel;

import io.citadel.domain.Domain;
import io.citadel.eventstore.EventStore;
import io.citadel.kernel.sql.Database;
import io.vertx.core.Vertx;

import static io.vertx.core.Vertx.vertx;

public sealed interface Citadel permits Citadel.Verticle {
  static Citadel.Verticle verticle(Vertx vertx) {
    return new Service(
      EventStore.defaults.verticle(
        vertx,
        Database.connection(
          "jdbc:postgresql",
          "localhost",
          5433,
          "citadel",
          "citadel",
          "docker",
          org.postgresql.Driver.class
        )
      ),
      Domain.verticle()
    );
  }

  sealed interface Verticle extends Citadel, io.vertx.core.Verticle permits Service {}
}

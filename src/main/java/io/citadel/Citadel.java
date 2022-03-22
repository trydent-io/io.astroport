package io.citadel;

import io.citadel.domain.Domain;
import io.citadel.eventstore.EventStore;
import io.citadel.kernel.sql.Database;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

import static io.vertx.core.Vertx.vertx;

public sealed interface Citadel permits Service {
  static Citadel service(Vertx vertx) {
    return new Service(
      EventStore.defaults.service(
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
      Domain.service()
    );
  }

  default Verticle asVerticle() {
    return switch (this) { case Service service -> service; };
  }
}

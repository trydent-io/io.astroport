package io.citadel;

import io.citadel.kernel.domain.eventstore.EventStore;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.sql.Database;
import io.vertx.core.Vertx;

public sealed interface Citadel permits Citadel.Verticle {
  static Citadel.Verticle verticle(Vertx vertx) {
    return new Service(
      EventStore.defaults.verticle(vertx, Database.postgresql("localhost", 5433, "citadel", "citadel", "docker")),
      Domain.defaults.verticle()
    );
  }

  sealed interface Verticle extends Citadel, io.vertx.core.Verticle permits Service {}
}

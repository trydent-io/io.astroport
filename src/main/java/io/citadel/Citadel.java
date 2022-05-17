package io.citadel;

import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.sql.Database;
import io.vertx.core.Vertx;

public sealed interface Citadel permits Citadel.Verticle {
  static Citadel.Verticle verticle(Vertx vertx) {
    return new Service(
      Domain.defaults.verticle(vertx, Database.postgresql("localhost", 5433, "citadel", "citadel", "docker"))
    );
  }

  sealed interface Verticle extends Citadel, io.vertx.core.Verticle permits Service {}
}

package io.citadel;

import io.citadel.context.Context;
import io.citadel.eventstore.EventStore;
import io.citadel.shared.sql.Database;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

import static io.vertx.core.Vertx.vertx;

public sealed interface Citadel permits Service {
  static Citadel service(Vertx vertx) {
    return new Service(
      EventStore.service(vertx, Database.connection("jdbc:postgresql", "localhost", 5433, "citadel", "citadel", "docker", org.postgresql.Driver.class)),
      Context.service()
    );
  }

  default Verticle asVerticle() {
    return switch (this) { case Service service -> service; };
  }
}

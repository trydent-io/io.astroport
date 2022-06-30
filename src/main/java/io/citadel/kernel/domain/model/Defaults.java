package io.citadel.kernel.domain.model;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventPool;
import io.citadel.kernel.eventstore.Metadata;
import io.citadel.kernel.sql.Database;
import io.citadel.kernel.sql.Migration;
import io.vertx.core.Vertx;

public enum Defaults {
  Companion;

  public Domain.Verticle service(Vertx vertx, Database database) {
    return new Service(
      Migration.eventStore(vertx, database),
      EventPool.client(vertx, database.asPgOptions()),
      Domain.<Forum.ID, Forum.Entity, Forum.Event, Forum.State>model(Forum.NAME)
        .aggregate(
          Forum::id,
          Forum::entity,
          () -> Forum.State.Registered,
          Forum::state
        )
        .handle()
    );
  }
}

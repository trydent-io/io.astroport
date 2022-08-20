package io.citadel.kernel.domain.model;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public final class Service extends AbstractVerticle implements Domain.Verticle {
  private final Migration migration;
  private final EventStore pool;
  private final Aggregator<Forum.ID, Forum.Entity, Forum.Event, Forum.State> forum;

  public Service(Migration migration, EventStore pool, Aggregator<Forum.ID, Forum.Entity, Forum.Event, Forum.State> forum) {
    this.migration = migration;
    this.pool = pool;
    this.forum = forum;
  }

  @Override
  public void start(final Promise<Void> start) {
    migration.migrate()
      .map(pool)
      .map(pool -> forum.bind(pool))
      .onSuccess(start::complete)
      .onFailure(start::fail);
  }
}

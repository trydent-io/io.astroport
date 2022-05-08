package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

record Repository(Domain.Aggregates<Forum.ID, Forum.Model, Forum.Aggregate> aggregates) implements Forums {
  @Override
  public Future<Forum.Aggregate> forum(final Forum.ID id) {
    return aggregates.lookup(id);
  }

  @Override
  public Future<Forum.Aggregate> forum(Forum.ID id, Forum.Name name) {
    return aggregates.lookup(id, it -> it.details().name().equals(name));
  }
}

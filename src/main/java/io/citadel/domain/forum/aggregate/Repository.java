package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain.Aggregates;
import io.vertx.core.Future;

record Repository(Aggregates<Forum.Aggregate, Forum.ID, Forum.Model> aggregates) implements Forums {
  @Override
  public Future<Forum.Aggregate> lookup(final Forum.ID id) {
    return aggregates.lookup(id);
  }

  @Override
  public Future<Forum.Aggregate> lookup(Forum.ID id, Forum.Name name) {
    return aggregates.lookup(id, it -> it.details().name().equals(name));
  }
}

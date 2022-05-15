package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

record Repository(Domain.Lookup<Forum.ID, Model, Forum.Aggregate> lookup) implements Forums {
  @Override
  public Future<Forum.Aggregate> forum(final Forum.ID id) {
    return lookup.findAggregate(id);
  }

  @Override
  public Future<Forum.Aggregate> forum(Forum.ID id, Forum.Name name) {
    return lookup.findAggregate(id, it -> it.details().name().equals(name));
  }
}

package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

record Repository(Domain.Models<Forum.ID, Forum.Model> models) implements Forums {
  @Override
  public Future<Forum.Aggregate> forum(final Forum.ID id) {
    return models.lookup(id).map(it -> Forum.defaults.aggregate(it, ));
  }

  @Override
  public Future<Forum.Aggregate> forum(Forum.ID id, Forum.Name name) {
    return models.lookup(id, it -> it.details().name().equals(name));
  }
}

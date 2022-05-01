package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain.Aggregates;
import io.vertx.core.Future;

import java.util.stream.Stream;

record Repository(Aggregates<Aggregate, Forum.ID, Forum.Event, Forum.Model> aggregates) implements Forums {
  @Override
  public Future<Aggregate> lookup(final Forum.ID id) {
    return aggregates.lookup(id);
  }

  @Override
  public Future<Aggregate> lookup(Forum.ID id, Forum.Name name) {
    return aggregates.lookup(id, it -> it.details().name().equals(name));
  }
}

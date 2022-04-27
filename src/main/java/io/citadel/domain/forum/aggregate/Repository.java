package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain.Aggregates;
import io.vertx.core.Future;

import java.util.stream.Stream;

record Repository(Aggregates<Aggregate, Forum.ID, Forum.Event> aggregates) implements Forums {
  @Override
  public Future<Aggregate> lookup(final Forum.ID id) {
    return aggregates.lookup(id);
  }

  @Override
  public Future<Aggregate> persist(final Forum.ID id, final long version, final Stream<Forum.Event> events, final String by) {
    return aggregates.persist(id, version, events, by);
  }
}

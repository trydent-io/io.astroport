package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.eventstore.data.AggregateInfo;
import io.citadel.kernel.domain.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

import java.util.stream.Stream;

record Repository(Domain.Aggregates<Aggregate, Forum.ID> aggregates) implements Forums {
  @Override
  public Future<Aggregate> findBy(final Forum.ID id) {
    return aggregates.findBy(id);
  }

  @Override
  public Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events) {
    return aggregates.persist(aggregate, events);
  }

  @Override
  public Future<Void> persist(final AggregateInfo aggregate, final Stream<EventInfo> events, final String by) {
    return null;
  }

  @Override
  public Future<Aggregate> findBy(final Forum.ID forumId, final Forum.State state) {
    return findBy(forumId).;
  }
}

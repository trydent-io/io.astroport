package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain.Aggregates;
import io.vertx.core.Future;

import java.util.stream.Stream;

record Repository(Aggregates<Aggregate, Forum.ID> aggregates) implements Forums {
  @Override
  public Future<Aggregate> lookup(final Forum.ID id) {
    return aggregates.lookup(id);
  }

  @Override
  public Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events, String user) {
    return aggregates.persist(aggregate, events, user);
  }
}

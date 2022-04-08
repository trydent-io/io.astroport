package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

import java.util.stream.Stream;

record Repository(Domain.Aggregates<Aggregate, Forum.ID> aggregates) implements Forums {
  @Override
  public Future<Aggregate> load(final Forum.ID id) {
    return aggregates.load(id);
  }

  @Override
  public Future<Void> save(AggregateInfo aggregate, Stream<EventInfo> events) {
    return aggregates.save(aggregate, events);
  }
}

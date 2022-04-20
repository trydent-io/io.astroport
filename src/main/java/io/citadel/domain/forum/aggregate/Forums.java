package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.EventStore;
import io.citadel.kernel.domain.Domain.Aggregates;

public sealed interface Forums extends Aggregates<Aggregate, Forum.ID> permits Repository {
  static Forums repository(EventStore eventStore, Snapshot snapshot) {
    return new Repository(Aggregates.repository(eventStore, snapshot, Forum.AGGREGATE_NAME));
  }
}

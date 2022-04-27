package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain.Aggregates;
import io.citadel.kernel.eventstore.EventStore;

public sealed interface Forums extends Aggregates<Aggregate, Forum.ID, Forum.Event> permits Repository {
  static Forums repository(EventStore eventStore, Snapshot snapshot) {
    return new Repository(Aggregates.repository(eventStore, snapshot, Forum.AGGREGATE_NAME, Forum.attributes::id));
  }
}

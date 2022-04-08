package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.EventStore;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;

public sealed interface Forums extends Domain.Aggregates<Aggregate, Forum.ID> permits Repository {
  static Forums repository(EventStore eventStore, Snapshot snapshot) {
    return new Repository(Domain.Aggregates.repository(eventStore, snapshot, Forum.AGGREGATE_NAME));
  }
}

package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Aggregate;
import io.citadel.domain.forum.repository.Repository;
import io.citadel.eventstore.EventStore;
import io.citadel.kernel.domain.Domain;

public sealed interface Forums extends Domain.Aggregates<Aggregate, Forum.ID> permits Repository {
  static Forums repository(EventStore eventStore, Forum.Snapshot hydration) {
    return new Repository(Domain.Aggregates.repository(eventStore, hydration, Forum.AGGREGATE_NAME));
  }
}

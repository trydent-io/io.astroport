package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Aggregate;
import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Snapshot;
import io.citadel.eventstore.EventStore;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.repository.Repository;

public sealed interface Forums extends Domain.Aggregates<Aggregate, Forum.ID> permits Repository {
  Defaults defaults = Defaults.Companion;
  static Forums repository(EventStore eventStore, Snapshot snapshot) {
    return new Repository(Domain.Aggregates.repository(eventStore, snapshot, Forum.AGGREGATE_NAME));
  }
}

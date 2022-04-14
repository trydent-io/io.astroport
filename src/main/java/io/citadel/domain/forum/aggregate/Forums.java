package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

public sealed interface Forums extends Domain.Aggregates<Aggregate, Forum.ID> permits Repository {
  static Forums repository(EventStore eventStore, Snapshot snapshot) {
    return new Repository(Domain.Aggregates.repository(eventStore, snapshot, Forum.AGGREGATE_NAME));
  }

  Future<Aggregate> findBy(Forum.ID forumId, Forum.State state);
}

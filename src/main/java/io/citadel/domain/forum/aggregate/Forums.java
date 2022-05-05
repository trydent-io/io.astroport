package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Domain.Aggregates;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.Future;

public sealed interface Forums permits Repository {
  static Forums repository(EventStore eventStore, Forum.Snapshot snapshot) {
    return new Repository(Domain.defaults.aggregates(eventStore, snapshot, Forum.AGGREGATE_NAME));
  }

  Future<Forum.Aggregate> lookup(Forum.ID id);

  Future<Forum.Aggregate> lookup(Forum.ID id, Forum.Name name);
}

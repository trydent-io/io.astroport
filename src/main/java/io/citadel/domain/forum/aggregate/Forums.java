package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain.Aggregates;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface Forums permits Repository {
  static Forums repository(EventStore eventStore, Snapshot snapshot) {
    return new Repository(Aggregates.repository(eventStore, snapshot, Forum.AGGREGATE_NAME, Forum.attributes::id));
  }

  Future<Aggregate> lookup(Forum.ID id);

  Future<Aggregate> lookup(Forum.ID id, Forum.Name name);
}

package io.citadel.domain.forum.repository;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.Forums;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

import java.util.stream.Stream;

public record Repository(Domain.Aggregates<Forum, Forum.ID, Forum.Event> aggregates) implements Forums {
  @Override
  public Future<Forum> load(final Forum.ID id) {
    return aggregates.load(id);
  }

  @Override
  public Future<Void> save(final Forum.ID id, final long version, final Stream<Forum.Event> events) {
    return aggregates.save(id, version, events);
  }
}

package io.citadel.context.forum.repository;

import io.citadel.context.forum.Forum;
import io.citadel.context.forum.Forums;
import io.citadel.shared.context.Domain;
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

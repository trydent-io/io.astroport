package io.citadel.kernel.domain.repository;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.Future;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.core.Future.failedFuture;
import static java.util.stream.Collectors.*;

public final class Repository<A extends Domain.Aggregate, I extends Domain.ID> implements Domain.Aggregates<A, I> {
  private final EventStore eventStore;
  private final Domain.Snapshot<A> snapshot;
  private final String name;

  public Repository(final EventStore eventStore, final Domain.Snapshot<A> snapshot, final String name) {
    this.eventStore = eventStore;
    this.snapshot = snapshot;
    this.name = name;
  }

  @Override
  public Future<A> lookup(final I id) {
    return eventStore.seek(new Feed.Aggregate(id.value(), name))
      .map(Feed::stream)
      .map(entries -> entries
        .findFirst()
        .map(it -> ))
      .map(it -> )
      .compose(aggregate -> aggregate
        .map(Future::succeededFuture)
        .orElse(failedFuture("Can't hydrate root %s with id %s".formatted(name, id)))
      );
  }

  @Override
  public Future<Void> persist(final AggregateInfo aggregate, final Stream<EventInfo> events, final String by) {
    return eventStore
      .persist(aggregate, events, by)
      .mapEmpty();
  }
}

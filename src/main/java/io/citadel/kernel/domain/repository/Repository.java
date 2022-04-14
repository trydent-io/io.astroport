package io.citadel.kernel.domain.repository;

import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.eventstore.data.AggregateInfo;
import io.citadel.kernel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

import java.util.stream.Stream;

import static io.vertx.core.Future.failedFuture;

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
  public Future<A> findBy(final I id) {
    return eventStore.findEventsBy(id.toString(), name)
      .map(events -> events.aggregateFrom(snapshot))
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

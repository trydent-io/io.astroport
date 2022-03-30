package io.citadel.kernel.domain.repository;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

import java.util.stream.Stream;

import static io.vertx.core.Future.failedFuture;

public final class Repository<A extends Domain.Aggregate<?>, I extends Domain.ID<?>, E extends Domain.Event> implements Domain.Aggregates<A, I> {
  private final EventStore eventStore;
  private final Domain.Hydration<A> hydration;
  private final String name;

  public Repository(final EventStore eventStore, final Domain.Hydration<A> hydration, final String name) {
    this.eventStore = eventStore;
    this.hydration = hydration;
    this.name = name;
  }

  @Override
  public Future<A> load(final I id) {
    return eventStore.findEventsBy(id.toString(), name)
      .map(events -> events.aggregateFrom(hydration))
      .compose(aggregate -> aggregate
        .map(Future::succeededFuture)
        .orElse(failedFuture("Can't hydrate root %s with id %s".formatted(name, id)))
      );
  }

  @Override
  public Future<Void> save(AggregateInfo aggregate, Stream<EventInfo> events) {
    return eventStore
      .persist(aggregate.id(), aggregate.name(), aggregate.version(), events)
      .mapEmpty();
  }
}

package io.citadel.kernel.domain.model;

import io.citadel.kernel.eventstore.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.Future;

import java.util.stream.Stream;

public record Changes<E extends Domain.Event, L extends Domain.Timeline<E, L>>(L lifecycle, EventStore eventStore, Stream<E> events) implements Domain.Transaction<E> {
  public Domain.Transaction<E> log(E event) {
    return lifecycle
      .stage(event)
      .map(l -> new Changes<>(
        lifecycle,
        eventStore,
        this.events != null
          ? Stream.concat(this.events, Stream.of(event))
          : Stream.of(event))
      )
      .orElseThrow(() -> new IllegalStateException("Can't log event %s, lifecycle is %s".formatted(event, lifecycle)));
  }

  @Override
  public Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion, String by) {
    return eventStore.feed(
      new Feed.Aggregate(aggregateId, aggregateName, aggregateVersion),
      events.map(Domain.Event::asFeed),
      by
    ).mapEmpty();
  }
}

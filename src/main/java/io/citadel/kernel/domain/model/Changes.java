package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.Future;

import java.util.stream.Stream;

public record Changes(EventStore eventStore, Stream<Domain.Event> events) implements Domain.Transaction {
  public Domain.Transaction log(Domain.Event... events) {
    return new Changes(eventStore, this.events != null
      ? Stream.concat(this.events, Stream.of(events))
      : Stream.of(events)
    );
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

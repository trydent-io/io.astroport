package io.citadel.eventstore;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import java.util.stream.Stream;

import static io.citadel.eventstore.Operations.FIND_BY_AGGREGATE;
import static io.citadel.eventstore.Operations.PERSIST_AGGREGATE_EVENTS;

public record Forward(EventBus eventBus) implements EventStore {
  @Override
  public Future<Operations.FoundEvents> findEventsBy(final Entries.Aggregate aggregate) {
    return eventBus.<Stream<Entries.EventLog>>request(FIND_BY_AGGREGATE, EventStore.operations.findBy(aggregate)).map(Message::body);
  }

  @Override
  public Future<Stream<Entries.EventLog>> persist(final Entries.Aggregate aggregate, final Stream<Entries.Event> events) {
    return eventBus.<Stream<Entries.EventLog>>request(PERSIST_AGGREGATE_EVENTS, EventStore.operations.persist(aggregate, events)).map(Message::body);
  }
}

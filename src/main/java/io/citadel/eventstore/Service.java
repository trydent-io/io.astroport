package io.citadel.eventstore;

import io.citadel.eventstore.Entries.Aggregate;
import io.citadel.eventstore.Entries.Event;
import io.citadel.eventstore.Entries.Entry;
import io.citadel.eventstore.Operations.FindBy;
import io.citadel.eventstore.Operations.Persist;
import io.citadel.shared.sql.Migration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.stream.Stream;

import static io.citadel.eventstore.Operations.FIND_BY_AGGREGATE;
import static io.citadel.eventstore.Operations.PERSIST_AGGREGATE_EVENTS;

final class Service extends AbstractVerticle implements EventStore {
  private final Migration migration;
  private final EventStore eventStore;

  Service(final Migration migration, final EventStore eventStore) {
    this.migration = migration;
    this.eventStore = eventStore;
  }

  @Override
  public void start(final Promise<Void> start) {
    migration.apply()
      .map(this::operations)
      .onSuccess(start::complete)
      .onFailure(start::fail);
  }

  private Void operations(Void unused) {
    vertx.eventBus().<FindBy>localConsumer(FIND_BY_AGGREGATE, message ->
      findEventsBy(message.body().aggregate())
        .onSuccess(message::reply)
        .onFailure(it -> message.fail(500, it.getMessage()))
    );

    vertx.eventBus().<Persist>localConsumer(PERSIST_AGGREGATE_EVENTS, message ->
      persist(message.body().aggregate(), message.body().events())
        .onSuccess(message::reply)
        .onFailure(it -> message.fail(500, it.getMessage()))
    );
    return null;
  }

  @Override
  public Future<Operations.FoundEvents> findEventsBy(final Aggregate aggregate) {
    return eventStore.findEventsBy(aggregate);
  }

  @Override
  public Future<Stream<Entry>> persist(final Aggregate aggregate, final Stream<Event> events) {
    return eventStore.persist(aggregate, events);
  }
}

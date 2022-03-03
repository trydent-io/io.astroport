package io.citadel.eventstore;

import io.citadel.shared.sql.Migration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

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
    vertx.eventBus().<String>localConsumer(operations.FIND_BY, message ->
      findBy(message.body(), message.headers().get("aggregateName"))
        .onSuccess(message::reply)
        .onFailure(it -> message.fail(500, it.getMessage()))
    );

    vertx.eventBus().<JsonObject>localConsumer(operations.PERSIST, message ->
      persist(
        message.body().getJsonObject("aggregate").mapTo(EventLog.AggregateInfo.class),
        message.body().getJsonArray("events")
          .stream()
          .map(it -> (JsonObject) it)
          .map(it -> it.mapTo(EventLog.EventInfo.class))
          .toArray(EventLog.EventInfo[]::new)
      )
    );
    return null;
  }

  @Override
  public Future<Stream<EventLog>> findBy(final String aggregateId, final String aggregateName) {
    return eventStore.findBy(aggregateId, aggregateName);
  }

  @Override
  public Future<Void> persist(final EventLog.AggregateInfo aggregate, final EventLog.EventInfo... events) {
    return eventStore.persist(aggregate, events);
  }
}

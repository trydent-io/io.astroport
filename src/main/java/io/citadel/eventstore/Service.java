package io.citadel.eventstore;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

final class Service extends AbstractVerticle implements EventStore {
  final EventStore eventStore;

  Service(final EventStore eventStore) {this.eventStore = eventStore;}

  @Override
  public void start(final Promise<Void> start) {
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

    start.complete();
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

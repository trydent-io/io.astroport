package io.citadel.eventstore.type;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.eventstore.data.EventLog;
import io.citadel.eventstore.event.Events;
import io.citadel.kernel.sql.Migration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public final class Service extends AbstractVerticle implements EventStore.Verticle {
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
    vertx.eventBus().<JsonObject>localConsumer(FIND_EVENTS_BY,
      message -> findEventsBy(message.body())
        .onSuccess(message::reply)
        .onFailure(it -> message.fail(500, it.getMessage()))
    );

    vertx.eventBus().<JsonObject>localConsumer(PERSIST_EVENTS,
      message -> persistEvents(message.body(), message.body().getJsonArray("events"))
          .onSuccess(message::reply)
          .onFailure(it -> message.fail(500, it.getMessage()))
    );
    return null;
  }

  private Future<Stream<EventLog>> persistEvents(final JsonObject aggregate, final JsonArray events) {
    return persist(AggregateInfo.from(aggregate), EventInfo.fromJsonArray(events));
  }

  private Future<Events> findEventsBy(final JsonObject aggregate) {
    return findEventsBy(aggregate.getString("id"), aggregate.getString("name"), aggregate.getLong("version"));
  }

  @Override
  public Future<Events> findEventsBy(String id, String name, long version) {
    return eventStore.findEventsBy(id, name, version);
  }

  @Override
  public Future<Stream<EventLog>> persist(AggregateInfo aggregate, Stream<EventInfo> events) {
    return eventStore.persist(aggregate, events);
  }
}

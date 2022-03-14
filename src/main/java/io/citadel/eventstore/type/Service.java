package io.citadel.eventstore.type;

import io.citadel.eventstore.EventLogs;
import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.event.Events;
import io.citadel.shared.sql.Migration;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public final class Service extends AbstractVerticle implements EventStore {
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
      message -> findEventsBy(
        message.body().getString("id"),
        message.body().getString("name"),
        message.body().getLong("version")
      )
        .onSuccess(message::reply)
        .onFailure(it -> message.fail(500, it.getMessage()))
    );

    vertx.eventBus().<JsonObject>localConsumer(PERSIST_EVENTS,
      message ->
        persist(
          new Raw(
            message.body().getString("id"),
            message.body().getString("name"),
            message.body().getLong("version")
          ),
          EventLogs.asEventInfos(message.body().getJsonArray("events"))
        )
          .onSuccess(message::reply)
          .onFailure(it -> message.fail(500, it.getMessage()))
    );
    return null;
  }

  @Override
  public Future<Events> findEventsBy(String id, String name, long version) {
    return eventStore.findEventsBy(id, name, version);
  }

  @Override
  public Future<Stream<EventLog>> persist(Raw aggregate, Stream<EventInfo> events) {
    return eventStore.persist(aggregate, events);
  }
}

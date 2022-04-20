package io.citadel.eventstore.type;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.eventstore.data.EventLog;
import io.citadel.eventstore.data.Feed;
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
    vertx.eventBus().<JsonObject>localConsumer(SEEK,
      message -> seek(message.body())
        .onSuccess(message::reply)
        .onFailure(it -> message.fail(500, it.getMessage()))
    );

    vertx.eventBus().<JsonArray>localConsumer(PERSIST,
      message -> persist(message.body())
          .onSuccess(message::reply)
          .onFailure(it -> message.fail(500, it.getMessage()))
    );
    return null;
  }

  private Future<Feed> persist(final JsonArray entries) {
    return persist(Feed.from(entries).stream());
  }

  private Future<Feed> seek(final JsonObject aggregate) {
    return seek(aggregate.getString("id"), aggregate.getString("name"));
  }

  @Override
  public Future<Feed> seek(String id, String name) {
    return eventStore.seek(id, name);
  }

  @Override
  public Future<Feed> persist(Stream<Feed.Entry> entries) {
    return eventStore.persist(entries);
  }
}

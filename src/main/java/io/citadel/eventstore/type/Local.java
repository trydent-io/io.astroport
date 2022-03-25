package io.citadel.eventstore.type;

import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.MetaAggregate;
import io.citadel.eventstore.data.MetaEvent;
import io.citadel.eventstore.data.EventLog;
import io.citadel.eventstore.event.Events;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public record Local(EventBus eventBus) implements EventStore {
  @Override
  public Future<Events> findEventsBy(String id, String name, long version) {
    return eventBus
      .<JsonObject>request(
        FIND_EVENTS_BY,
        Json.of(
          "id", id,
          "name", name,
          "version", version
        )
      )
      .map(Message::body)
      .map(Events::fromJson);
  }

  @Override
  public Future<Stream<EventLog>> persist(MetaAggregate aggregate, Stream<MetaEvent> events) {
    return eventBus
      .<JsonArray>request(
        PERSIST_EVENTS,
        Json.of(
          "root", Json.fromAny(aggregate),
          "events", Json.array(events)
        )
      )
      .map(Message::body)
      .map(EventLog::fromJsonArray);
  }
}

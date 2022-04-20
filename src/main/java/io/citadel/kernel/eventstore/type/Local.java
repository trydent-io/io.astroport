package io.citadel.kernel.eventstore.type;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public record Local(EventBus eventBus) implements EventStore {
  @Override
  public Future<Feed> seek(String id, String name) {
    return eventBus
      .<JsonObject>request(
        SEEK,
        Json.of(
          "id", id,
          "name", name
        )
      )
      .map(Message::body)
      .map(Feed::fromJson);
  }

  @Override
  public Future<Feed> persist(final Stream<Feed.Entry> entries) {
    return eventBus
      .<JsonObject>request(PERSIST, Json.array(entries))
      .map(Message::body)
      .map(Feed::fromJson);
  }
}

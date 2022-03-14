package io.citadel.eventstore.event;

import io.citadel.shared.context.Domain;
import io.citadel.shared.func.Maybe;
import io.citadel.shared.media.Json;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

import static io.citadel.eventstore.EventStore.*;

public sealed interface Events permits Empty, Found {
  static Events found(long version, Stream<EventInfo> events) {
    return new Found(version, events);
  }

  static Events empty() {
    return Empty.Default;
  }

  default <A extends Domain.Aggregate<?>> Maybe<A> aggregate(Domain.Hydration<A> hydration) {
    return Maybe.empty();
  }

  static Events fromJson(JsonObject json) {
    return new Found(
      json.getLong("version"),
      json.getJsonArray("events")
        .stream()
        .map(Json::fromAny)
        .map(it ->
          new EventInfo(
            it.getString("name"),
            it.getJsonObject("data")
          )
        )
    );
  }
}

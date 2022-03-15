package io.citadel.eventstore.event;

import io.citadel.eventstore.data.EventInfo;
import io.citadel.shared.context.Domain;
import io.citadel.shared.media.Json;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.stream.Stream;

public sealed interface Events permits Empty, Found {
  static Events found(long version, Stream<EventInfo> events) {
    return new Found(version, events);
  }

  static Events empty() {
    return Empty.Default;
  }

  default <A extends Domain.Aggregate<?>> Optional<A> aggregateFrom(Domain.Hydration<A> hydration) {
    return Optional.empty();
  }

  static Events fromJson(JsonObject json) {
    return new Found(
      json.getLong("version"),
      json.getJsonArray("events")
        .stream()
        .map(Json::fromAny)
        .map(EventInfo::fromJson)
    );
  }
}

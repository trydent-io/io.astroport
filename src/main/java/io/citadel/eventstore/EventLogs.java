package io.citadel.eventstore;

import io.citadel.eventstore.EventStore.EventInfo;
import io.citadel.eventstore.EventStore.EventLog;
import io.citadel.shared.media.Json;
import io.vertx.core.json.JsonArray;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

public sealed interface EventLogs {
  enum Namespace implements EventLogs {}

  static Stream<EventLog> fromJsonArray(JsonArray array) {
    return array.stream()
      .map(Json::fromAny)
      .map(json ->
        new EventLog(
          UUID.fromString(json.getString("id")),
          new EventStore.Raw(
            json.getJsonObject("aggregate").getString("id"),
            json.getJsonObject("aggregate").getString("name"),
            json.getJsonObject("aggregate").getLong("version")
          ),
          new EventInfo(
            json.getJsonObject("event").getString("name"),
            json.getJsonObject("event").getJsonObject("data")
          ),
          LocalDateTime.from(json.getInstant("persistedAt")),
          "none"
        )
      );
  }

  static Stream<EventInfo> asEventInfos(JsonArray array) {
    return array.stream()
      .map(Json::fromAny)
      .map(json ->
        new EventInfo(
          json.getString("name"),
          json.getJsonObject("data")
        )
      );
  }
}

package io.citadel.eventstore.data;

import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.media.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

public record EventLog(UUID id, AggregateInfo aggregate, EventInfo event, LocalDateTime persistedAt, String persistedBy) {
  public static Stream<EventLog> fromJsonArray(JsonArray array) {
    return array.stream()
      .map(Json::fromAny)
      .map(json ->
        new EventLog(
          UUID.fromString(json.getString("id")),
          AggregateInfo.from(json),
          EventInfo.from(json),
          LocalDateTime.from(json.getInstant("persistedAt")),
          "none"
        )
      );
  }

  public static EventLog fromRow(Row row) {
    return new EventLog(
      row.getUUID("id"),
      AggregateInfo.from(row),
      EventInfo.from(row),
      row.getLocalDateTime("persisted_at"),
      Maybe.right(row.getString("persisted_by")).otherwise("none")
    );
  }

}

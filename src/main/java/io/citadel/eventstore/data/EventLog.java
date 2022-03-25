package io.citadel.eventstore.data;

import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.media.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

public record EventLog(UUID id, MetaAggregate aggregate, MetaEvent event, LocalDateTime persistedAt, String persistedBy) {
  public static Stream<EventLog> fromJsonArray(JsonArray array) {
    return array.stream()
      .map(Json::fromAny)
      .map(json ->
        new EventLog(
          UUID.fromString(json.getString("id")),
          MetaAggregate.from(json),
          MetaEvent.from(json),
          LocalDateTime.from(json.getInstant("persistedAt")),
          "none"
        )
      );
  }

  public EventLog eventLog(Row row) {
    return new EventLog(
      row.getUUID("id"),
      MetaAggregate.from(row),
      MetaEvent.from(row),
      row.getLocalDateTime("persisted_at"),
      Maybe.of(row.getString("persisted_by")).or("none")
    );
  }

}

package io.citadel.eventstore;

import io.citadel.shared.domain.Domain;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventLog(UUID id, AggregateInfo aggregate, EventInfo event, LocalDateTime persistedAt, String persistedBy) {
  record AggregateInfo(String id, String name, Domain.Version version) {}

  record EventInfo(String name, JsonObject data) {}

  record Entry(EventInfo event, AggregateInfo aggregate) {}

  public static EventLog fromRow(Row row) {
    return new EventLog(
      row.getUUID("id"),
      new AggregateInfo(
        row.getString("aggregate_id"),
        row.getString("aggregate_name"),
        Domain.Version.of(row.getLong("aggregate_version")).orElseThrow()
      ),
      new EventInfo(
        row.getString("event_name"),
        row.getJsonObject("event_data")
      ),
      row.getLocalDateTime("persisted_at"),
      row.getString("persisted_by")
    );
  }
}

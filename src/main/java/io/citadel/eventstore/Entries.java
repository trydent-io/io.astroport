package io.citadel.eventstore;

import io.citadel.shared.func.Maybe;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.UUID;

public enum Entries {
  Defaults;

  public Aggregate aggregate(Row row) {
    return new Aggregate(
      row.getString("aggregate_id"),
      row.getString("aggregate_name"),
      row.getLong("aggregate_version")
    );
  }

  public Event event(Row row) {
    return new Event(
      row.getString("event_name"),
      row.getJsonObject("event_data")
    );
  }

  public Entry storedEvent(Row row) {
    return new Entry(
      row.getUUID("id"),
      aggregate(row),
      event(row),
      row.getLocalDateTime("persisted_at"),
      Maybe.of(row.getString("persisted_by")).or("none")
    );
  }

  public record Aggregate(String id, String name, long version) {
    public Aggregate(String id, String name) { this(id, name, Long.MAX_VALUE); }
  }
  public record Event(String name, JsonObject data) {}
  public record Entry(UUID id, Aggregate aggregate, Event event, LocalDateTime persistedAt, String persistedBy) {}
}

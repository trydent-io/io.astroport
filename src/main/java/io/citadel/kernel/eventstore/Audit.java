package io.citadel.kernel.eventstore;

import io.citadel.kernel.media.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.UUID;

public record Audit(Entity entity, Event event) {
  public Audit { assert entity != null && event != null; }

  public static Audit of(Entity entity, Event event) { return new Audit(entity, event); }

  public static Audit fromRow(Row row) {
    return new Audit(
      Entity.fromRow(row),
      Event.fromRow(row)
    );
  }

  public record Entity(String id, String name, long version) {
    public Entity {
      assert id != null && name != null && version >= 0;
    }

    public static Entity with(String id, String name) {
      return new Entity(id, name, 0);
    }

    static Entity versioned(String id, String name, long version) {
      return new Entity(id, name, version);
    }

    public static Entity fromRow(Row row) {
      return
        new Entity(
          row.getString("entity_id"),
          row.getString("entity_name"),
          row.getLong("entity_version")
        );
    }
  }

  public record Event(UUID id, String name, JsonObject data, LocalDateTime timepoint) {
    public Event {
      assert name != null && data != null;
    }
    public Event(String name, JsonObject data) { this(null, name, data, null); }

    public static Event meta(String name, JsonObject data) {
      return new Event(null, name, data, null);
    }
    public static Event saved(UUID id, String name, JsonObject data, LocalDateTime timepoint) {
      return new Event(id, name, data, timepoint);
    }

    public static Event fromRow(Row row) {
      return
        new Event(
          row.getUUID("event_id"),
          row.getString("event_name"),
          row.getJsonObject("event_data"),
          row.getLocalDateTime("event_timepoint")
        );
    }

    public static <EVENT> Event from(EVENT event) {
      return new Event(null, event.getClass().getSimpleName(), Json.fromAny(event), null);
    }
  }
}
